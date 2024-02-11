package io.pocat.gateway.route;

import java.io.IOException;
import java.io.InputStream;
import java.util.Set;

public interface RouteGroupContext {
    InputStream openRouteGroupContextStream() throws IOException;
    InputStream openStream(String routeCtxPath) throws IOException;

    Set<String> getChildContextNames() throws IOException;

}
