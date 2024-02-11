package io.pocat.gateway.route;

import io.pocat.gateway.connector.Exchange;

public interface RouteTask {
    void doTask(Exchange exchange) throws RouteProcessException;
}
