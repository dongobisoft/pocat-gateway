package io.pocat.gateway;

import io.pocat.context.EnvContextProvider;
import io.pocat.context.descriptor.Argument;
import io.pocat.context.descriptor.MessageChannelDescriptor;
import io.pocat.context.descriptor.MessageEndpointDescriptor;
import io.pocat.gateway.config.ConnectorConfigType;
import io.pocat.gateway.config.ServiceConfigType;
import io.pocat.gateway.config.TLSConfigType;
import io.pocat.gateway.connector.*;
import io.pocat.gateway.protocol.*;
import io.pocat.gateway.route.*;
import io.pocat.message.MessageStatusCode;
import io.pocat.message.channel.adaptor.ChannelAdaptorFactoryProvider;
import io.pocat.message.channel.adaptor.ChannelConfigBuilder;
import io.pocat.message.endpoint.MessageEndpoint;
import io.pocat.message.endpoint.MessageEndpointConfigBuilder;
import io.pocat.message.endpoint.MessageEndpointProvider;
import io.pocat.utils.ExpireRegistry;
import io.pocat.utils.clazz.ClassHelper;
import io.pocat.utils.clazz.InstantiationFailedException;
import io.pocat.message.channel.ConsumableMessageChannel;
import io.pocat.message.channel.DirectMessageChannel;
import io.pocat.message.channel.adaptor.OutboundChannelAdaptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.RejectedExecutionException;

import static io.pocat.gateway.Gateway.*;

public class ServiceContainer {
    private static final Logger LOGGER = LoggerFactory.getLogger(ServiceContainer.class);
    private static final String ROUTE_GROUP_CONTEXT_HOME_PATH = "/env/routes";
    private static final String CONTEXT_SEPARATOR = "/";
    private static final String CHANNEL_HOME = "/env/context/channels/";
    private static final String ENDPOINTS_HOME = "/env/context/endpoints/";
    private static final String FILE_HOME = "/env/files";

    private final Gateway gateway;
    private final EnvContextProvider envCtxProvider;
    private final Map<String, RouteProcessor> processors = new HashMap<>();

    private ProtocolErrorHandler routeGroupErrorHandler;
    private ProtocolRouter router;
    private Server server;

    private ProtocolFactory protocolFactory;
    private AccessLogger accessLogger;
    private RouteProcedure responseProcedure;
    private ServiceConfigType serviceConfig;

    public ServiceContainer(Gateway gateway) {
        this.gateway = gateway;
        this.envCtxProvider = gateway.getEnvProvider();
        this.accessLogger = gateway.getAccessLogger();
    }

    public void init(ServiceConfigType serviceConfig) {
        this.serviceConfig = serviceConfig;
        if(serviceConfig.getAccessLogger() != null) {
            accessLogger = AccessLogger.builder().build();
        }
        this.protocolFactory = ProtocolFactoryProvider.getInstance().provide(serviceConfig.getProtocol());
        if(protocolFactory == null) {
            throw new IllegalStateException("Not supported protocol [" + serviceConfig.getProtocol() + "].");
        }
        this.router = protocolFactory.createRouter();

        ProtocolRouteGroupFactory routeGroupFactory = protocolFactory.createRouteGroupFactory();
        RouteGroup routeGroup;
        try {
            routeGroup = routeGroupFactory.createRouteGroup(
                    new RouteGroupContextImpl(envCtxProvider,
                        ROUTE_GROUP_CONTEXT_HOME_PATH + CONTEXT_SEPARATOR + serviceConfig.getRouteGroup()
                    )
            );
        } catch (IOException e) {
            throw new IllegalArgumentException("Route group creation failed.", e);
        }

        ExecutorService responseExecutor = gateway.getExecutorManager().getExecutor(RESPONSE_EXECUTOR_NAME);

        this.responseProcedure = (exchange, chain) -> {
            try {
                responseExecutor.execute(() -> {
                    accessLogger.log(new AccessLogRecord(exchange));
                    exchange.close();
                });
            } catch (RejectedExecutionException e) {
                LOGGER.warn("Exceed thread pool size : Reject to increase response handler.");
            }
        };

        buildRouteProcessors(routeGroup);

        this.server = new Server();
        server.setExecutor(gateway.getExecutor());
        for(ConnectorConfigType connectorConfig :serviceConfig.getConnectors()) {
            ServerConnector serverConnector = buildConnector(connectorConfig);
            server.addConnector(serverConnector);
        }

        ExecutorService dispatcherExecutor = gateway.getExecutorManager().getExecutor(DISPATCHER_EXECUTOR_NAME);
        server.setHandler(exchange -> {
            try {
                dispatcherExecutor.execute(() -> {
                    try {
                        Route route = router.findRoute(exchange);
                        RouteProcessor processor = processors.get(route.getName());
                        processor.process(exchange);
                    } catch (RouteProcessException e) {
                        routeGroupErrorHandler.handleError(exchange, e);
                        responseProcedure.call(exchange, null);
                    }
                });
            } catch (RejectedExecutionException e) {
                LOGGER.warn("Exceed thread pool size : Reject to increase dispatcher handler.");
            }
        });
    }

    public void start() {
        this.server.start();
    }

    public void stop() {
        this.server.stop();
    }

    private void buildRouteProcessors(RouteGroup routeGroup) {
        this.routeGroupErrorHandler = protocolFactory.createErrorHandler();
        for(ErrorTemplate template:routeGroup.getErrorTemplates()) {
            this.routeGroupErrorHandler.addErrorTemplate(template);
        }

        for(String routeName:routeGroup.getRouteNames()) {
            Route route = routeGroup.getRoute(routeName);
            this.router.addRoute(route);

            processors.put(route.getName(), buildRouteProcessor(route));
        }
    }

    private RouteProcessor buildRouteProcessor(Route route) {
        ProtocolErrorHandler errorHandler = protocolFactory.createErrorHandler();
        errorHandler.setParentHandler(this.routeGroupErrorHandler);
        List<ErrorTemplate> errorTemplates = route.getErrorTemplates();
        for(ErrorTemplate errorTemplate:errorTemplates) {
            errorHandler.addErrorTemplate(errorTemplate);
        }

        RouteErrorProcedure errorProcedure = (exchange, e) -> {
            errorHandler.handleError(exchange, e);
            responseProcedure.call(exchange, null);
        };

        List<RouteProcedure> procedureChain = new ArrayList<>();
        ExecutorService filterExecutor = this.gateway.getExecutorManager().getExecutor(FILTER_EXECUTOR_NAME);
        for(RouteTask task:route.getBeforeFilterTasks()) {
            procedureChain.add(createRouteProcedure(route, filterExecutor, task::doTask, errorProcedure));
        }

        for(RouteFilterConfig filterConfig:route.getRequestFilterConfigs()) {
            RouteFilter filter = initFilter(filterConfig);
            procedureChain.add(createRouteProcedure(route, filterExecutor, filter::doFilter, errorProcedure));
        }

        procedureChain.add(createUpstreamProcedure(route, errorProcedure));

        for(RouteFilterConfig filterConfig:route.getResponseFilterConfigs()) {
            RouteFilter filter = initFilter(filterConfig);
            procedureChain.add(createRouteProcedure(route, filterExecutor, filter::doFilter, errorProcedure));
        }
        for(RouteTask task:route.getAfterFilterTasks()) {
            procedureChain.add(createRouteProcedure(route, filterExecutor, task::doTask, errorProcedure));
        }

        procedureChain.add(responseProcedure);

        ExpireRegistry.Builder<String, Exchange> builder = new ExpireRegistry.Builder<>();
        builder.setThreadPoolSize(1).setExpiredEventHandler((txId, exchange) -> {
            DownStreamProcedureRegistry.getInstance().unregister(txId);
            errorProcedure.call(exchange, new RouteProcessException(MessageStatusCode.GATEWAY_TIMEOUT, "Gateway timeout"));
        });

        return new RouteProcessor(route, procedureChain, builder.build());
    }

    private RouteProcedure createUpstreamProcedure(Route route, RouteErrorProcedure errorProcedure) {
        ExecutorService upstreamExecutor = gateway.getExecutorManager().getExecutor(UPSTREAM_EXECUTOR_NAME);

        UpstreamTask upstreamTask = new UpstreamTask();
        upstreamTask.setErrorProcedure(errorProcedure);
        upstreamTask.setMessageConverter(protocolFactory.getMessageConverter());
        upstreamTask.setTopicFormat(route.getUpstreamConfig().getRequestTopic());
        OutboundChannelAdaptor channelAdaptor;
        try {
            channelAdaptor = ChannelAdaptorRegistry.getInstance().getOutboundChannelAdaptor(route.getUpstreamConfig().getRequestChannel());
            if(channelAdaptor == null) {
                channelAdaptor = createOutboundChannelAdaptor(route.getUpstreamConfig().getRequestChannel());
                ChannelAdaptorRegistry.getInstance().registerOutboundChannelAdaptor(route.getUpstreamConfig().getRequestChannel(), channelAdaptor);
            }
            ConsumableMessageChannel channel = new DirectMessageChannel(route.getUpstreamConfig().getRequestChannel());
            channelAdaptor.setChannel(channel);
            upstreamTask.setOutboundChannel(channel);
            upstreamTask.setReplyChannel(gateway.getResponseChannel());
            upstreamTask.setReplyTopic(gateway.getGatewayId());

            return ((exchange, chain) -> {
                try {
                    upstreamExecutor.execute(() -> upstreamTask.doTask(exchange, chain));
                } catch (RejectedExecutionException e) {
                    LOGGER.warn("Exceed thread pool size : Reject to increase upstream executor.");
                }
            });
        } catch (IOException e) {
            throw new IllegalArgumentException(e);
        }
    }

    private OutboundChannelAdaptor createOutboundChannelAdaptor(String channelName) throws IOException {
        MessageChannelDescriptor channelDescriptor = (MessageChannelDescriptor) envCtxProvider.lookup(CHANNEL_HOME + channelName);
        if(channelDescriptor == null) {
            throw new IOException("Channel context [" + channelName + "] does not exist.");
        }

        String endpointName = channelDescriptor.getEndpointRef();
        MessageEndpointDescriptor endpointDescriptor = (MessageEndpointDescriptor) envCtxProvider.lookup(ENDPOINTS_HOME + endpointName);
        if(endpointDescriptor == null) {
            throw new IOException("Endpoint context [" + endpointName + "] does not exist.");
        }

        MessageEndpointConfigBuilder endpointConfigBuilder = new MessageEndpointConfigBuilder(endpointDescriptor.getName(), endpointDescriptor.getType());
        List<Argument> endpointArgs = endpointDescriptor.getArguments();
        for(Argument arg:endpointArgs) {
            endpointConfigBuilder.addArgument(arg.getName(), arg.getValue());
        }

        MessageEndpoint endpoint = MessageEndpointProvider.getInstance().getChannelEndpoint(endpointConfigBuilder.build());
        ChannelConfigBuilder channelConfigBuilder = new ChannelConfigBuilder(channelDescriptor.getChannelName());
        channelConfigBuilder.setEndpoint(endpoint);
        List<Argument> channelArgs = channelDescriptor.getArguments();
        for(Argument arg:channelArgs) {
            channelConfigBuilder.addArgument(arg.getName(), arg.getValue());
        }

        return ChannelAdaptorFactoryProvider.getInstance().getOutboundChannelAdaptorFactory(channelConfigBuilder.build()).createOutboundChannelAdaptor();
    }

    private RouteProcedure createRouteProcedure(Route route, ExecutorService executor, ProcedureTask task, RouteErrorProcedure routeErrorProcedure) {
        return (exchange, chain) -> {
            try {
                executor.execute(() -> {
                    try {
                        task.doTask(exchange);
                        chain.doNext(exchange);
                    } catch (RouteProcessException e) {
                        routeErrorProcedure.call(exchange, e);
                    } catch (Exception e) {
                        LOGGER.error("Unexpected exception is thrown during route [" + route.getName() + "] processing.", e);
                        routeErrorProcedure.call(exchange, new RouteProcessException(50000, e.getMessage() == null ? "Unknown Error" : e.getMessage()));
                    }
                });
            } catch (RejectedExecutionException e) {
                LOGGER.warn("Exceed thread pool size : Reject to increase route procedure executor.");
            }
        };
    }

    private RouteFilter initFilter(RouteFilterConfig filterConfig) {
        RouteFilter filter;
        try {
            filter = (RouteFilter) ClassHelper.createInstance(filterConfig.getFilterType());

            Map<String, Object> resourceRefMap = new HashMap<>();
            for(ResourceRef ref:filterConfig.getResourceRefs()) {
                Object resource = gateway.getResourceManager().getResource(ref.getResourceName());
                if(resource == null) {
                    throw new IllegalArgumentException("Resource ref [" + ref.getRefName() + "] failed. Resource [" + ref.getResourceName() + "] does not exist.");
                }
                resourceRefMap.put(ref.getRefName(), resource);
            }
            RouteFilterContext filterContext = new RouteFilterContextImpl(filterConfig.getInitParams(), resourceRefMap);
            filter.init(filterContext);
            return filter;
        } catch (InstantiationFailedException e) {
            throw new IllegalArgumentException("Class [" + filterConfig.getFilterType() + "] instantiation failed.", e);
        }
    }

    private ServerConnector buildConnector(ConnectorConfigType connectorConfig) {
        try {
            ServerConnector connector = new ServerConnector(connectorConfig.getName());
            connector.setPort(connectorConfig.getPort());
            if (connectorConfig.getOptions() != null) {
                connector.setServerConnectorOptions(connectorConfig.getOptions());
            }
            if (connectorConfig.getTlsConfig() != null) {
                TLSConfigType tlsConfig = connectorConfig.getTlsConfig();
                URL certUrl = (URL) envCtxProvider.lookup(FILE_HOME + tlsConfig.getCertPath());
                if(certUrl == null) {
                    throw new IllegalArgumentException("Failed to create connector [" + connectorConfig.getName() + "]. Cert file [" + tlsConfig.getCertPath() + "] does not exist.");
                }
                URL keyUrl = (URL) envCtxProvider.lookup(FILE_HOME + tlsConfig.getKeyPath());
                if(keyUrl == null) {
                    throw new IllegalArgumentException("Failed to create connector [" + connectorConfig.getName() + "]. Key file [" + tlsConfig.getKeyPath() + "] does not exist.");
                }
                String keyPass = tlsConfig.getKeyPassword();

                try (InputStream certStream = certUrl.openStream();
                     InputStream keyStream = keyUrl.openStream()) {
                    connector.addConnectionHandler(new TlsConnectionHandler(certStream, keyStream, keyPass));
                }
            }
            if (connectorConfig.isEnableWebsocket()) {
                connector.addConnectionHandler(new WebsocketConnectionHandler(this.serviceConfig.getProtocol()));
            }

            connector.addConnectionHandlers(protocolFactory.createConnectionHandlers());
            connector.setAcceptorNum(connectorConfig.getAcceptor());
            connector.setSelectorNum(connectorConfig.getSelector());
            connector.setServerConnectorOptions(connectorConfig.getOptions());

            return connector;
        } catch (IOException e) {
            throw new IllegalArgumentException("Failed to create connector [" + connectorConfig.getName() + "]");
        }
    }

    public static class RouteGroupContextImpl implements RouteGroupContext {
        private final EnvContextProvider envCtxProvider;
        private final String groupHome;

        public RouteGroupContextImpl(EnvContextProvider envCtxProvider, String groupHome) {
            this.envCtxProvider = envCtxProvider;
            this.groupHome = groupHome.endsWith("/")?groupHome.substring(0, groupHome.length()-1):groupHome;
        }

        @Override
        public InputStream openRouteGroupContextStream() throws IOException {
            URL url = (URL) envCtxProvider.lookup(groupHome);
            if(url == null) {
                throw new IOException("Route group context [" + groupHome + "] does not exist.");
            }
            return url.openStream();
        }

        @Override
        public Set<String> getChildContextNames() throws IOException {
            return envCtxProvider.listChildren(groupHome);
        }

        @Override
        public InputStream openStream(String routeCtxPath) throws IOException {
            if(!routeCtxPath.startsWith("/")) {
                routeCtxPath = "/" + routeCtxPath;
            }
            URL url = (URL)(envCtxProvider.lookup(groupHome + routeCtxPath));
            if(url == null) {
                throw new IOException("Route context [" + groupHome + routeCtxPath + "] does not exist.");
            }
            return url.openStream();
        }
    }

    private static class RouteFilterContextImpl implements RouteFilterContext {
        private final Map<String, String> initParams;
        private final Map<String, Object> resourceRefs;

        public RouteFilterContextImpl(Map<String, String> initParams, Map<String,Object> resourceRefs) {
            this.initParams = initParams;
            this.resourceRefs = resourceRefs;
        }

        @Override
        public Object getResource(String resourceName) {
            return resourceRefs.get(resourceName);
        }

        @Override
        public Set<String> getInitParamNames() {
            return Collections.unmodifiableSet(initParams.keySet());
        }

        @Override
        public String getInitParam(String paramName) {
            return initParams.get(paramName);
        }
    }

    private interface ProcedureTask {
        void doTask(Exchange exchange) throws RouteProcessException;
    }
}
