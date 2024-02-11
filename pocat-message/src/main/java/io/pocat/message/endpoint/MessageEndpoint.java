package io.pocat.message.endpoint;

public interface MessageEndpoint {
    String getEndpointName();
    Class<?> getConnectorType();
    Object getConnector();
}
