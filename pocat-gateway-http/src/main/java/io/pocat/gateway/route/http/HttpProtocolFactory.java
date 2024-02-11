package io.pocat.gateway.route.http;

import io.pocat.gateway.connector.ConnectionHandler;
import io.pocat.gateway.connector.http.HttpConnectionHandler;
import io.pocat.gateway.protocol.ProtocolFactory;
import io.pocat.gateway.protocol.ProtocolRouteGroupFactory;
import io.pocat.gateway.protocol.ProtocolErrorHandler;
import io.pocat.gateway.protocol.ProtocolRouter;
import io.pocat.gateway.route.MessageConverter;

import java.util.Collections;
import java.util.List;

public class HttpProtocolFactory implements ProtocolFactory {
    private static final String HTTP_PROTOCOL_NAME = "http";
    private static final String HTTPS_PROTOCOL_NAME = "https";

    @Override
    public boolean isSupportedProtocol(String protocol) {
        return HTTP_PROTOCOL_NAME.equalsIgnoreCase(protocol) || HTTPS_PROTOCOL_NAME.equalsIgnoreCase(protocol);
    }

    @Override
    public ProtocolRouteGroupFactory createRouteGroupFactory() {
        return new HttpRouteGroupFactory();
    }

    @Override
    public ProtocolRouter createRouter() {
        return new HttpRouter();
    }

    @Override
    public MessageConverter getMessageConverter() {
        return new HttpMessageConverter();
    }

    @Override
    public ProtocolErrorHandler createErrorHandler() {
        return new HttpProtocolErrorHandler();
    }

    @Override
    public List<ConnectionHandler> createConnectionHandlers() {
        return Collections.singletonList(new HttpConnectionHandler());
    }
}
