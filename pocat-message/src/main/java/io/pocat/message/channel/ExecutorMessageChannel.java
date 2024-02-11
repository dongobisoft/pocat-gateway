package io.pocat.message.channel;

import io.pocat.message.Message;

import java.util.concurrent.ExecutorService;

public class ExecutorMessageChannel implements ConsumableMessageChannel {
    private final String name;
    private final ExecutorService executor;
    private MessageConsumer consumer;

    public ExecutorMessageChannel(String name, ExecutorService executor) {
        this.name = name;
        this.executor = executor;
    }

    @Override
    public void consume(MessageConsumer consumer) {
        this.consumer = consumer;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void publish(Message message) {
        executor.execute(() -> consumer.consume(message));
    }
}
