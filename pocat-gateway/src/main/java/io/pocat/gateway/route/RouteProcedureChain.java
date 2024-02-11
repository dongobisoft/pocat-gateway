package io.pocat.gateway.route;

import io.pocat.gateway.connector.Exchange;

import java.util.Iterator;

public interface RouteProcedureChain {
    void doNext(Exchange exchange);
}
