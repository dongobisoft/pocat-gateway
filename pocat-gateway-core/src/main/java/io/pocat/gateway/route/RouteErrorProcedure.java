package io.pocat.gateway.route;

import io.pocat.gateway.connector.Exchange;

public interface RouteErrorProcedure {
    void call(Exchange exchange, RouteProcessException e);
}
