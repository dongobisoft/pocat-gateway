package io.pocat.message.channel;

import io.pocat.message.Message;

public interface MessageConsumer {
    void consume(Message message);
}
