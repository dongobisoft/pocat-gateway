package io.pocat.gateway.route.http;

import io.pocat.gateway.route.ErrorTemplate;
import io.pocat.gateway.route.Route;
import io.pocat.gateway.route.RouteGroup;
import io.pocat.gateway.route.http.config.HttpRouteGroupType;

import java.util.*;
import java.util.stream.Collectors;

public class HttpRouteGroup implements RouteGroup {
    private final HttpRouteGroupType descriptor;
    private final Map<String, HttpRoute> routes = new HashMap<>();

    public HttpRouteGroup(HttpRouteGroupType descriptor) {
        this.descriptor = descriptor;
    }

    @Override
    public String getName() {
        return descriptor.getName();
    }

    @Override
    public List<ErrorTemplate> getErrorTemplates() {
        return descriptor.listErrorTemplates().stream().map(HttpErrorTemplate::new).collect(Collectors.toUnmodifiableList());
    }

    @Override
    public Set<String> getRouteNames() {
        return routes.keySet();
    }

    @Override
    public Route getRoute(String routeName) {
        return routes.get(routeName);
    }

    public void addRoute(HttpRoute route) {
        routes.put(route.getName(), route);
    }
}
