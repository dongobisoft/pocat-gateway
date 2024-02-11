package io.pocat.message.channel.adaptor;

import io.pocat.message.channel.MessageChannel;

import java.io.IOException;
import java.util.concurrent.ExecutorService;

public interface InboundChannelAdaptor {
    String getName();

    MessageChannel getChannel();
    void setChannel(MessageChannel channel);

    ExecutorService getExecutor();
    void setExecutor(ExecutorService executor);

    void bindTopic(String topic) throws IOException;

    void start() throws IOException;
    void stop() throws IOException;
}
