package io.pocat.message.channel;

import io.pocat.message.Message;
import io.pocat.message.channel.ConsumableMessageChannel;
import io.pocat.message.channel.MessageConsumer;

public class DirectMessageChannel implements ConsumableMessageChannel {
    private final String name;
    private MessageConsumer consumer = new DumpConsumer();

    public DirectMessageChannel(String name) {
        this.name = name;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void publish(Message message) {
        consumer.consume(message);
    }

    @Override
    public void consume(MessageConsumer handler) {
        this.consumer = handler;
    }

    private static class DumpConsumer implements MessageConsumer {
        @Override
        public void consume(Message message) {
            // do nothing
        }
    }
}
