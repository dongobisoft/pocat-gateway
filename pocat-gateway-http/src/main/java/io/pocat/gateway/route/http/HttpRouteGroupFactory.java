package io.pocat.gateway.route.http;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import io.pocat.gateway.route.RouteGroupContext;
import io.pocat.gateway.protocol.ProtocolRouteGroupFactory;
import io.pocat.gateway.route.RouteGroup;
import io.pocat.gateway.route.http.config.HttpRouteGroupType;
import io.pocat.gateway.route.http.config.HttpRouteType;

import java.io.IOException;
import java.io.InputStream;
import java.util.Set;

public class HttpRouteGroupFactory implements ProtocolRouteGroupFactory {
    @Override
    public RouteGroup createRouteGroup(RouteGroupContext ctx) throws IOException {
        HttpRouteGroup routeGroup;
        ObjectMapper mapper = new XmlMapper();
        try (InputStream is = ctx.openRouteGroupContextStream()) {
            HttpRouteGroupType descriptor = mapper.readValue(is, HttpRouteGroupType.class);
            routeGroup = new HttpRouteGroup(descriptor);
        }

        Set<String> routeNames = ctx.getChildContextNames();
        for(String routeName:routeNames) {
            try (InputStream is = ctx.openStream(routeName)) {
                HttpRouteType rd = mapper.readValue(is, HttpRouteType.class);
                routeGroup.addRoute(new HttpRoute(rd));
            }
        }

        return routeGroup;
    }
}
