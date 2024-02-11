package io.pocat.message.channel;

import io.pocat.message.Message;

public interface MessageChannel {
    String getName();
    void publish(Message message);
}
