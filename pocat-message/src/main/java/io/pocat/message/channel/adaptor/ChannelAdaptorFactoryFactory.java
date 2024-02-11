package io.pocat.message.channel.adaptor;

public interface ChannelAdaptorFactoryFactory {
    boolean isSupportedConnector(Class<?> connectorType);
    InboundChannelAdaptorFactory createInboundChannelAdaptorFactory(ChannelConfig channelConfig);
    OutboundChannelAdaptorFactory createOutboundChannelAdaptorFactory(ChannelConfig channelConfig);
}
