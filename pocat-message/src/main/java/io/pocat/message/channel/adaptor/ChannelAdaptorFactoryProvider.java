package io.pocat.message.channel.adaptor;

import java.util.ServiceLoader;

public class ChannelAdaptorFactoryProvider {
    private static final ChannelAdaptorFactoryProvider INSTANCE = new ChannelAdaptorFactoryProvider();

    public static ChannelAdaptorFactoryProvider getInstance() {
        return INSTANCE;
    }

    private ChannelAdaptorFactoryFactory[] factories;
    private ServiceLoader<ChannelAdaptorFactoryFactory> factoriesLoader;

    private ChannelAdaptorFactoryProvider() {
        reload();
    }

    public InboundChannelAdaptorFactory getInboundChannelAdaptorFactory(ChannelConfig channelConfig) {
        for(ChannelAdaptorFactoryFactory factory: factories) {
            if(factory.isSupportedConnector(channelConfig.getMessageEndpoint().getConnectorType())) {
                return factory.createInboundChannelAdaptorFactory(channelConfig);
            }
        }
        throw new IllegalArgumentException("Not supported connector type [" + channelConfig.getMessageEndpoint().getConnectorType().getName() + "]");
    }

    public OutboundChannelAdaptorFactory getOutboundChannelAdaptorFactory(ChannelConfig channelConfig) {
        for(ChannelAdaptorFactoryFactory factory: factories) {
            if(factory.isSupportedConnector(channelConfig.getMessageEndpoint().getConnectorType())) {
                return factory.createOutboundChannelAdaptorFactory(channelConfig);
            }
        }
        throw new IllegalArgumentException("Not supported connector type [" + channelConfig.getMessageEndpoint().getConnectorType().getName() + "]");
    }

    public void reload() {
        if(factoriesLoader == null) {
            factoriesLoader = ServiceLoader.load(ChannelAdaptorFactoryFactory.class);
        } else {
            factoriesLoader.reload();
        }
        factories = factoriesLoader.stream().map(ServiceLoader.Provider::get).toArray(ChannelAdaptorFactoryFactory[]::new);
    }
}
