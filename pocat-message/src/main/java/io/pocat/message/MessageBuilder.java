package io.pocat.message;

import io.pocat.message.channel.MessageChannel;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class MessageBuilder {
    private final String topic;
    private byte[] payload;
    private final Map<String, Object> rawHeaders = new HashMap<>();


    public MessageBuilder(String txId, String topic) {
        rawHeaders.put(MessageHeaders.TX_ID_HEADER_NAME, txId);
        this.topic = topic;
    }
    public MessageBuilder setPayload(byte[] payload) {
        this.payload = payload;
        return this;
    }

    public MessageBuilder addHeader(String name, Object value) {
        rawHeaders.put(name, value);
        return this;
    }

    public MessageBuilder addHeaders(Map<String, Object> headers) {
        rawHeaders.putAll(headers);
        return this;
    }

    public Message build() {
        return new BuilderMessage(this);
    }

    public MessageBuilder setReply(MessageChannel replyChannel, String replyTopic) {
        rawHeaders.put(MessageHeaders.REPLY_TO_HEADER_NAME, replyChannel);
        rawHeaders.put(MessageHeaders.REPLY_TOPIC_HEADER_NAME, replyTopic);
        return this;
    }

    private static class BuilderMessage implements Message {
        private final MessageBuilder builder;
        private final MessageHeaders headers;

        public BuilderMessage(MessageBuilder builder) {
            this.builder = builder;
            builder.rawHeaders.put(MessageHeaders.MESSAGE_ID_HEADER_NAME, UUID.randomUUID().toString().replaceAll("-", ""));
            this.headers = new MessageHeaders(builder.rawHeaders);
        }

        @Override
        public String getTxId() {
            return headers.getTxId();
        }

        @Override
        public String getMessageId() {
            return headers.getMessageId();
        }

        @Override
        public String getTopic() {
            return builder.topic;
        }

        @Override
        public byte[] getPayload() {
            return builder.payload;
        }

        @Override
        public MessageHeaders getHeaders() {
            return headers;
        }
    }
}
