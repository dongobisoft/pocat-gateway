package io.pocat.message.channel.adaptor.http;

import io.pocat.message.channel.adaptor.ChannelAdaptorFactoryFactory;
import io.pocat.message.channel.adaptor.ChannelConfig;
import io.pocat.message.channel.adaptor.InboundChannelAdaptorFactory;
import io.pocat.message.channel.adaptor.OutboundChannelAdaptorFactory;
import io.pocat.message.channel.endpoint.http.HttpConnector;

public class HttpChannelAdaptorFactoryFactory implements ChannelAdaptorFactoryFactory {
    @Override
    public boolean isSupportedConnector(Class<?> connectorType) {
        return HttpConnector.class.isAssignableFrom(connectorType);
    }

    @Override
    public InboundChannelAdaptorFactory createInboundChannelAdaptorFactory(ChannelConfig channelConfig) {
        throw new NotSupportedOperationException("Http Inbound not supported.");
    }

    @Override
    public OutboundChannelAdaptorFactory createOutboundChannelAdaptorFactory(ChannelConfig channelConfig) {

        return new HttpOutboundChannelAdaptorFactory(channelConfig);
    }
}
