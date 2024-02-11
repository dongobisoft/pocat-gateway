package io.pocat.gateway.route.http;

import io.pocat.gateway.route.RouteProcessException;
import io.pocat.gateway.connector.Exchange;
import io.pocat.gateway.connector.http.HttpExchange;
import io.pocat.gateway.protocol.ProtocolRouter;
import io.pocat.gateway.route.Route;
import io.pocat.utils.pathtree.InvalidPathException;
import io.pocat.utils.pathtree.PathTree;

import java.util.HashMap;
import java.util.Map;

public class HttpRouter implements ProtocolRouter {
    private final Map<String, PathTree<String>> routePaths = new HashMap<>();
    private final Map<String, HttpRoute> routes = new HashMap<>();

    @Override
    public void addRoute(Route route) {
        if(!(route instanceof HttpRoute)) {
            throw new IllegalArgumentException("Not a http route.");
        }
        HttpRoute httpRoute = (HttpRoute) route;
        try {
            routes.put(httpRoute.getName(), httpRoute);
            PathTree<String> pathTree = routePaths.computeIfAbsent(httpRoute.getMethod(), k->new PathTree<>());
            pathTree.addItem(httpRoute.getWildcardPath(), httpRoute.getName());
        } catch (InvalidPathException e) {
            throw new IllegalArgumentException("Invalid path [" + httpRoute.getWildcardPath() + "]");
        }
    }

    @Override
    public Route findRoute(Exchange exchange) throws RouteProcessException {
        if(!(exchange instanceof HttpExchange)) {
            throw new RouteProcessException(40000, "Invalid protocol. Not a http message");
        }

        HttpExchange httpExchange = (HttpExchange) exchange;
        String requestPath = httpExchange.getRequestPath();
        PathTree<String> pathTree = routePaths.get(httpExchange.getRequestMethod());
        if(pathTree == null) {
            throw new RouteProcessException(40400, "Not found");
        }
        try {
            String routeName = pathTree.findItem(requestPath);
            if(routeName == null) {
                throw new RouteProcessException(40400, "Not found");
            }
            HttpRoute route = routes.get(routeName);
            if(route == null) {
                throw new RouteProcessException(40400, "Not found");
            }
            return route;
        } catch (InvalidPathException e) {
            throw new RouteProcessException(40001, "Invalid request path [" + requestPath + "]");
        }
    }
}
