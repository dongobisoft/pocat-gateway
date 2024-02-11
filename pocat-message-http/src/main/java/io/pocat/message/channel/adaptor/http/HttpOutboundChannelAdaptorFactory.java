package io.pocat.message.channel.adaptor.http;

import io.pocat.message.channel.adaptor.*;

public class HttpOutboundChannelAdaptorFactory implements OutboundChannelAdaptorFactory {
    private ChannelConfig channelConfig;

    public HttpOutboundChannelAdaptorFactory(ChannelConfig channelConfig) {
        this.channelConfig = channelConfig;
    }


    @Override
    public OutboundChannelAdaptor createOutboundChannelAdaptor() {
        return new HttpOutboundChannelAdaptor(channelConfig);
    }
}
