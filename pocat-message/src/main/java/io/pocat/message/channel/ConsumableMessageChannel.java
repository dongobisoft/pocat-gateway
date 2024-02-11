package io.pocat.message.channel;

public interface ConsumableMessageChannel extends MessageChannel {
    void consume(MessageConsumer consumer);
}
