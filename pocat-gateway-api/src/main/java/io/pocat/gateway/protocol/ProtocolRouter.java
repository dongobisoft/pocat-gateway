package io.pocat.gateway.protocol;

import io.pocat.gateway.route.RouteProcessException;
import io.pocat.gateway.connector.Exchange;
import io.pocat.gateway.route.Route;

public interface ProtocolRouter {
    void addRoute(Route route);
    Route findRoute(Exchange exchange) throws RouteProcessException;
}
