package io.pocat.gateway.protocol;

import io.pocat.gateway.route.RouteGroupContext;
import io.pocat.gateway.route.RouteGroup;

import java.io.IOException;

public interface ProtocolRouteGroupFactory {
    RouteGroup createRouteGroup(RouteGroupContext ctx) throws IOException;
}
