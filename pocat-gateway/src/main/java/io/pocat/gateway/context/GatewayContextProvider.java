package io.pocat.gateway.context;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import io.pocat.context.EnvContext;
import io.pocat.context.EnvContextProvider;
import io.pocat.context.descriptor.Argument;
import io.pocat.context.descriptor.MessageChannelDescriptor;
import io.pocat.context.descriptor.MessageEndpointDescriptor;
import io.pocat.context.descriptor.ResourceDescriptor;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;
import java.util.stream.Collectors;

class GatewayContextProvider implements EnvContextProvider {
    private static final String POCAT_GATEWAY_HOME_PROP_NAME = "io.pocat.gateway.home";
    private static final String DEFAULT_HOME_PROP_NAME = "user.dir";

    private static final String POCAT_CONTEXT_PROP_NAME = "io.pocat.gateway.context";
    private static final String DEFAULT_CONTEXT = "/context/context.xml";

    private static final String POCAT_ROUTE_GROUP_HOME_PROP_NAME = "io.pocat.gateway.routes.home";
    private static final String DEFAULT_ROUTE_GROUP_HOME = "/routes";

    private static final String POCAT_FILES_HOME_PROP_NAME = "io.pocat.gateway.files.home";
    private static final String DEFAULT_FILES_HOME = "/files";

    private static final String ROOT_CONTEXT_PATH = "/env";
    private static final String ENDPOINT_CONTEXT_HOME_PATH = ROOT_CONTEXT_PATH + "/context/endpoints";
    private static final String CHANNEL_CONTEXT_HOME_PATH = ROOT_CONTEXT_PATH + "/context/channels";
    private static final String RESOURCE_CONTEXT_HOME_PATH = ROOT_CONTEXT_PATH + "/context/resources";
    private static final String FILE_CONTEXT_HOME_PATH = ROOT_CONTEXT_PATH + "/files";

    private static final String ROUTE_GROUP_CONTEXT_HOME_PATH = ROOT_CONTEXT_PATH + "/routes";

    private final Properties properties;
    private final Map<String, GatewayEnvContext> contexts = new HashMap<>();

    GatewayContextProvider(Properties properties) {
        this.properties = properties;
        String gatewayHome = properties.getProperty(POCAT_GATEWAY_HOME_PROP_NAME, this.properties.getProperty(DEFAULT_HOME_PROP_NAME));
        if(gatewayHome == null) {
            throw new IllegalArgumentException("Gateway home dir does not exist.");
        }
        File gatewayHomeDir = new File(gatewayHome);
        if((!gatewayHomeDir.exists())) {
            throw new IllegalStateException("Gateway home [" + gatewayHomeDir.getAbsolutePath() + "] does not exist.");
        }
        if(gatewayHomeDir.isFile()) {
            throw new IllegalStateException("Gateway home [" + gatewayHomeDir.getAbsolutePath() + "] is not a directory.");
        }
        GatewayEnvContext rootContext = new GatewayEnvContext(ROOT_CONTEXT_PATH);
        contexts.put(ROOT_CONTEXT_PATH, rootContext);

        loadContext(gatewayHomeDir);
        loadRoute(gatewayHomeDir);
        loadFiles(gatewayHomeDir);
    }

    @Override
    public Object lookup(String ctxPath) {
        return Optional.ofNullable(lookupInternal(ctxPath)).map(GatewayEnvContext::getObject).orElse(null);
    }

    @Override
    public Set<String> listChildren(String ctxPath) {
        return Optional.ofNullable(lookupInternal(ctxPath)).map(GatewayEnvContext::listChildren).orElse(Collections.emptySet());
    }

    @Override
    public boolean isExist(String ctxPath) {
        return lookupInternal(ctxPath) != null;
    }

    @Override
    public boolean hasObject(String ctxPath) {
        return Optional.ofNullable(lookupInternal(ctxPath)).map(GatewayEnvContext::hasObject).orElse(false);
    }

    @Override
    public void bind(String ctxPath, Object obj) {
        bindInternal(ctxPath, obj);
    }

    @Override
    public void close() {
        // do nothing
    }

    private void loadContext(File gatewayHomeDir) {
        String contextPath = this.properties.getProperty(POCAT_CONTEXT_PROP_NAME, DEFAULT_CONTEXT);
        if(contextPath.startsWith(EnvContext.SEPARATOR)) {
            contextPath = contextPath.substring(1);
        }
        File contextFile = new File(gatewayHomeDir, contextPath);

        if(!contextFile.exists()) {
            throw new IllegalStateException("Context home [" + contextFile.getAbsolutePath() + "] does not exist.");
        }
        if(!contextFile.isFile()) {
            throw new IllegalStateException("Context home [" + contextFile.getAbsolutePath() + "] is not a file.");
        }

        GatewayContextVO gatewayContextVO;
        try {
            ObjectMapper mapper = new XmlMapper();
            gatewayContextVO = mapper.readValue(contextFile, GatewayContextVO.class);
        } catch (IOException e) {
            throw new IllegalArgumentException("Invalid context file [" + contextFile.getAbsolutePath() + "]", e);
        }

        if(gatewayContextVO.getEndpoints() != null) {
            for (GatewayContextVO.Endpoint endpoint : gatewayContextVO.getEndpoints()) {
                bindInternal(ENDPOINT_CONTEXT_HOME_PATH + (endpoint.getName().startsWith(EnvContext.SEPARATOR) ? "" : EnvContext.SEPARATOR) + endpoint.getName(),
                        new GatewayEndpointDescriptor(endpoint));
            }
        }
        if(gatewayContextVO.getChannels() != null) {
            for (GatewayContextVO.Channel channel : gatewayContextVO.getChannels()) {
                bindInternal(CHANNEL_CONTEXT_HOME_PATH + (channel.getName().startsWith(EnvContext.SEPARATOR) ? "" : EnvContext.SEPARATOR) + channel.getName(),
                        new GatewayChannelDescriptor(channel));
            }
        }
        if(gatewayContextVO.getResources() != null) {
            for (GatewayContextVO.Resource resource : gatewayContextVO.getResources()) {
                bindInternal(RESOURCE_CONTEXT_HOME_PATH + (resource.getName().startsWith(EnvContext.SEPARATOR) ? "" : EnvContext.SEPARATOR) + resource.getName(),
                        new GatewayResourceDescriptor(resource));
            }
        }
    }

    private void loadRoute(File gatewayHomeDir) {
        String routeGroupHomePath = this.properties.getProperty(POCAT_ROUTE_GROUP_HOME_PROP_NAME, DEFAULT_ROUTE_GROUP_HOME);
        if(routeGroupHomePath.startsWith(EnvContext.SEPARATOR)) {
            routeGroupHomePath = routeGroupHomePath.substring(1);
        }
        File routeGroupHome = new File(gatewayHomeDir, routeGroupHomePath);

        if(!routeGroupHome.exists()) {
            throw new IllegalStateException("Route group home [" + routeGroupHome.getAbsolutePath() + "] does not exist.");
        }
        if(!routeGroupHome.isDirectory()) {
            throw new IllegalStateException("Route group home [" + routeGroupHome.getAbsolutePath() + "] is not a directory.");
        }

        File[] routeGroupDirs = routeGroupHome.listFiles(File::isDirectory);
        if(routeGroupDirs == null) {
            throw new IllegalArgumentException("Route group does not exist.");
        }
        for(File routeGroupDir:routeGroupDirs) {
            File routeGroupConfigFile = new File(routeGroupHome.getAbsolutePath(), routeGroupDir.getName() + ".xml");
            if(routeGroupConfigFile.exists() && routeGroupConfigFile.isFile()) {
                try {
                    bindInternal(ROUTE_GROUP_CONTEXT_HOME_PATH + EnvContext.SEPARATOR + routeGroupDir.getName(), routeGroupConfigFile.toURI().toURL());
                } catch (MalformedURLException e) {
                    // may not be thrown
                    throw new IllegalStateException("Cannot get url of file [" + routeGroupConfigFile.getAbsolutePath() + "].", e);
                }

                File[] routeConfigs = routeGroupDir.listFiles(pathname -> pathname.isFile() && pathname.getName().endsWith(".xml"));
                if (routeConfigs == null) {
                    continue;
                }
                for (File routeConfigFile : routeConfigs) {
                    try {
                        String routeName = routeConfigFile.getName().substring(0, routeConfigFile.getName().length()-".xml".length());
                        bindInternal(ROUTE_GROUP_CONTEXT_HOME_PATH + EnvContext.SEPARATOR +  routeGroupDir.getName() + EnvContext.SEPARATOR + routeName, routeConfigFile.toURI().toURL());
                    } catch (MalformedURLException e) {
                        // may not be thrown
                        throw new IllegalStateException("Cannot get url of file [" + routeGroupConfigFile.getAbsolutePath() + "].", e);
                    }
                }
            }
        }
    }

    private void loadFiles(File gatewayHomeDir) {
        String filesHomePath = this.properties.getProperty(POCAT_FILES_HOME_PROP_NAME, DEFAULT_FILES_HOME);
        if(filesHomePath.startsWith(EnvContext.SEPARATOR)) {
            filesHomePath = filesHomePath.substring(1);
        }
        File filesHome = new File(gatewayHomeDir, filesHomePath);
        if(filesHome.exists()) {
            if(filesHome.isFile()) {
                throw new IllegalStateException("File home [" + filesHome.getAbsolutePath() + "] is not a directory.");
            }
        }

        List<Path> files = new ArrayList<>();
        try {
            Files.walkFileTree(Paths.get(filesHome.toURI()), new FileVisitor<>() {
                @Override
                public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    files.add(file);
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                    return FileVisitResult.CONTINUE;
                }
            });
        } catch (IOException e) {
            throw new IllegalStateException("Cannot get list of files [" + filesHome.getAbsolutePath() + "].", e);
        }

        try {
            String fileHomeUrlStr = filesHome.toURI().toURL().toString();
            for(Path filePath:files) {
                URL url = filePath.toUri().toURL();
                String ctxPath = url.toString().substring(fileHomeUrlStr.length());
                bindInternal(FILE_CONTEXT_HOME_PATH + "/" + ctxPath, url);
            }
        } catch (MalformedURLException ignored) {
            // never happened
        }

    }

    private void bindInternal(String ctxPath, Object obj) {
        GatewayEnvContext ctx = lookupInternal(ctxPath);
        if(ctx == null) {
            ctx = new GatewayEnvContext(ctxPath);
            String parentPath = ctxPath.substring(0, ctxPath.lastIndexOf(EnvContext.SEPARATOR));
            GatewayEnvContext parent = lookupInternal(parentPath);
            if(parent == null) {
                bindInternal(parentPath, null);
                parent = lookupInternal(parentPath);
            }
            parent.addChild(ctx.getName());
            contexts.put(ctxPath, ctx);
        }
        ctx.setObject(obj);
    }

    private GatewayEnvContext lookupInternal(String ctxPath) {
        return contexts.get(ctxPath);
    }

    public static class GatewayResourceDescriptor implements ResourceDescriptor {
        private final GatewayContextVO.Resource resource;

        public GatewayResourceDescriptor(GatewayContextVO.Resource resource) {
            this.resource = resource;
        }

        @Override
        public String getName() {
            return resource.getName();
        }

        @Override
        public String getType() {
            return resource.getType();
        }

        @Override
        public List<Argument> getArguments() {
            return resource.getArgs().stream().map((argument) -> new Argument(){
                @Override
                public String getName() {
                    return argument.getName();
                }
                @Override
                public String getValue() {
                    return argument.getValue();
                }
            }).collect(Collectors.toList());
        }
    }

    public static class GatewayEndpointDescriptor implements MessageEndpointDescriptor {
        private final GatewayContextVO.Endpoint endpoint;

        public GatewayEndpointDescriptor(GatewayContextVO.Endpoint endpoint) {
            this.endpoint = endpoint;
        }

        @Override
        public String getName() {
            return endpoint.getName();
        }

        @Override
        public String getType() {
            return endpoint.getType();
        }

        @Override
        public List<Argument> getArguments() {
            return endpoint.getArgs().stream().map((argument) -> new Argument(){
                @Override
                public String getName() {
                    return argument.getName();
                }
                @Override
                public String getValue() {
                    return argument.getValue();
                }
            }).collect(Collectors.toList());
        }
    }

    public static class GatewayChannelDescriptor implements MessageChannelDescriptor {
        private final GatewayContextVO.Channel channel;

        public GatewayChannelDescriptor(GatewayContextVO.Channel channel) {
            this.channel = channel;
        }

        @Override
        public String getChannelName() {
            return channel.getName();
        }

        @Override
        public String getEndpointRef() {
            return channel.getEndpointRef();
        }

        @Override
        public List<Argument> getArguments() {
            return channel.getArgs().stream().map((argument) -> new Argument(){
                @Override
                public String getName() {
                    return argument.getName();
                }
                @Override
                public String getValue() {
                    return argument.getValue();
                }
            }).collect(Collectors.toList());
        }
    }
}
