package io.pocat.gateway.protocol;

import io.pocat.gateway.connector.ConnectionHandler;
import io.pocat.gateway.route.MessageConverter;

import java.util.List;

public interface ProtocolFactory {
    boolean isSupportedProtocol(String protocol);

    List<ConnectionHandler> createConnectionHandlers();

    ProtocolRouteGroupFactory createRouteGroupFactory();
    ProtocolErrorHandler createErrorHandler();

    ProtocolRouter createRouter();

    MessageConverter getMessageConverter();
}
