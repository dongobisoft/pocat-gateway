package io.pocat.gateway.route;

import java.util.List;

public interface Route {
    String getName();
    long getExpireIn();

    List<RouteTask> getBeforeFilterTasks();
    List<RouteFilterConfig> getRequestFilterConfigs();

    UpstreamConfig getUpstreamConfig();

    List<RouteFilterConfig> getResponseFilterConfigs();
    List<RouteTask> getAfterFilterTasks();

    List<ErrorTemplate> getErrorTemplates();
}
