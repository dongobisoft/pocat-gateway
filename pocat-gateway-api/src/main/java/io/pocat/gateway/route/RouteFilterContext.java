package io.pocat.gateway.route;

import java.util.Set;

public interface RouteFilterContext {
    Object getResource(String resourceName);
    Set<String> getInitParamNames();
    String getInitParam(String paramName);
}
