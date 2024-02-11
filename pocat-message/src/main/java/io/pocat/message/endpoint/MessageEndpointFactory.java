package io.pocat.message.endpoint;

public interface MessageEndpointFactory {
    MessageEndpoint createEndpoint(MessageEndpointConfig config);

    boolean isSupportedEndpointType(String endpointType);
}
