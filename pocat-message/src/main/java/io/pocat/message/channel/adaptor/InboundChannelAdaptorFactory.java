package io.pocat.message.channel.adaptor;

public interface InboundChannelAdaptorFactory {
    InboundChannelAdaptor createInboundChannelAdaptor(String consumerGroupName);

}
