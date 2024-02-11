package io.pocat.message.channel.adaptor;

import io.pocat.message.channel.ConsumableMessageChannel;
import io.pocat.message.channel.MessageChannel;

import java.io.IOException;
import java.util.concurrent.ExecutorService;

public interface OutboundChannelAdaptor {
    String getName();
    MessageChannel getChannel();
    void setChannel(ConsumableMessageChannel channel);

    ExecutorService getExecutor();
    void setExecutor(ExecutorService executor);

    void close() throws IOException;


}
