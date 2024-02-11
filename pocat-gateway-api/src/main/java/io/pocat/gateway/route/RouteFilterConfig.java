package io.pocat.gateway.route;

import java.util.List;
import java.util.Map;

public interface RouteFilterConfig {
    String getFilterType();
    Map<String,String> getInitParams();
    List<ResourceRef> getResourceRefs();
}
