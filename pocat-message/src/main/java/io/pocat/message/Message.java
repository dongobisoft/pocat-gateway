package io.pocat.message;

public interface Message {
    String getTxId();
    String getMessageId();
    String getTopic();

    MessageHeaders getHeaders();

    byte[] getPayload();
}
