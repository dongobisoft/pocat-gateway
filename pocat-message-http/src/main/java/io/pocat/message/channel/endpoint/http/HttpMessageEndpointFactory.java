package io.pocat.message.channel.endpoint.http;

import io.pocat.message.endpoint.MessageEndpoint;
import io.pocat.message.endpoint.MessageEndpointConfig;
import io.pocat.message.endpoint.MessageEndpointFactory;

import java.net.http.HttpClient;

public class HttpMessageEndpointFactory implements MessageEndpointFactory {
    private static final String HTTP_TYPE = "http";
    private static final String HTTPS_TYPE = "https";

    @Override
    public boolean isSupportedEndpointType(String endpointType) {
        return HTTP_TYPE.equalsIgnoreCase(endpointType) || HTTPS_TYPE.equalsIgnoreCase(endpointType);
    }
    @Override
    public MessageEndpoint createEndpoint(MessageEndpointConfig config) {
        return new HttpMessageEndpoint(config);
    }
}
