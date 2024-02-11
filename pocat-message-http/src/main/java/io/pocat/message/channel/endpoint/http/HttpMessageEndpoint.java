package io.pocat.message.channel.endpoint.http;

import io.pocat.message.endpoint.MessageEndpoint;
import io.pocat.message.endpoint.MessageEndpointConfig;

public class HttpMessageEndpoint implements MessageEndpoint {
    private final MessageEndpointConfig config;
    private final HttpConnector connector;

    public HttpMessageEndpoint(MessageEndpointConfig config) {
        this.config = config;
        this.connector = new HttpConnector(config);
    }
    @Override
    public String getEndpointName() {
        return this.config.getEndpointName();
    }

    @Override
    public Class<?> getConnectorType() {
        return HttpConnector.class;
    }

    @Override
    public Object getConnector() {
        return connector;
    }
}
