package io.pocat.gateway.route;

import java.util.List;
import java.util.Set;

public interface RouteGroup {
    String getName();

    Set<String> getRouteNames();
    Route getRoute(String routeName);

    List<ErrorTemplate> getErrorTemplates();
}
