package io.pocat.gateway.route;

import io.pocat.gateway.connector.Exchange;

public interface RouteFilter {
    void init(RouteFilterContext filterConfig);
    void doFilter(Exchange exchange) throws RouteProcessException;
    void destroy();
}
