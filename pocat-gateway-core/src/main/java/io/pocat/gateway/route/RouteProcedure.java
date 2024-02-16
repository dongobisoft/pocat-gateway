package io.pocat.gateway.route;

import io.pocat.gateway.connector.Exchange;

public interface RouteProcedure {
    void call(Exchange exchange, RouteProcedureChain chain);
}
