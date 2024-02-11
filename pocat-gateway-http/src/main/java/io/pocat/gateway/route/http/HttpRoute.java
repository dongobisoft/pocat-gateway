package io.pocat.gateway.route.http;

import io.pocat.gateway.connector.http.HttpExchange;
import io.pocat.gateway.route.*;
import io.pocat.gateway.route.http.config.HttpRouteType;
import io.pocat.gateway.route.http.config.RouteFilterType;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class HttpRoute implements Route {
    private static final Pattern VARIABLE_PATTERN = Pattern.compile("\\{([^/]*)}");

    private final HttpRouteType descriptor;
    private String[] pathVariableNames;
    private Pattern pathPattern;
    private String wildcardPath;

    public HttpRoute(HttpRouteType descriptor) {
        this.descriptor = descriptor;
        buildPattern(descriptor);
    }

    @Override
    public String getName() {
        return descriptor.getName();
    }

    @Override
    public long getExpireIn() {
        return descriptor.getExpireIn();
    }

    public int getSuccessCode() {
        return descriptor.getSuccessCode();
    }

    public String getMethod() {
        return descriptor.getMethod();
    }

    public String getPath() {
        return descriptor.getPath();
    }

    public String getWildcardPath() {
        return wildcardPath;
    }

    @Override
    public List<RouteTask> getBeforeFilterTasks() {
        return Collections.singletonList((exchange) -> {
            if(!(exchange instanceof HttpExchange)) {
                throw new InvalidProtocolException("HTTP");
            }
            HttpExchange httpExchange = (HttpExchange) exchange;
            Matcher matcher = pathPattern.matcher(httpExchange.getRequestPath());
            if(matcher.matches()) {
                for(int i = 0; i < getPathVariableNames().length; i++) {
                    String pathVarValue = matcher.group(i+1);
                    exchange.setAttribute("path:" + getPathVariableNames()[i], pathVarValue);
                }
                httpExchange.setResponseStatus(getSuccessCode());
            }
        });
    }

    private String[] getPathVariableNames() {
        return pathVariableNames;
    }

    @Override
    public List<RouteFilterConfig> getRequestFilterConfigs() {
        if(descriptor.getRequestFilters() != null) {
            return descriptor.getRequestFilters().stream().map(RouteFilterConfigImpl::new).collect(Collectors.toList());
        }
        return Collections.emptyList();
    }

    @Override
    public UpstreamConfig getUpstreamConfig() {
        return descriptor.getUpstream();
    }

    @Override
    public List<RouteFilterConfig> getResponseFilterConfigs() {
        if(descriptor.getResponseFilters() != null) {
            return descriptor.getResponseFilters().stream().map(RouteFilterConfigImpl::new).collect(Collectors.toList());
        }
        return Collections.emptyList();
    }

    @Override
    public List<RouteTask> getAfterFilterTasks() {
        return Collections.emptyList();
    }

    @Override
    public List<ErrorTemplate> getErrorTemplates() {
        if(descriptor.getErrorTemplates() != null) {
            return descriptor.getErrorTemplates().stream().map(HttpErrorTemplate::new).collect(Collectors.toList());
        }
        return Collections.emptyList();
    }

    private void buildPattern(HttpRouteType descriptor) {
        String path = descriptor.getPath();
        String wildcardPath = descriptor.getPath();
        Matcher matcher = VARIABLE_PATTERN.matcher(path);
        List<String> varNames = new ArrayList<>();
        while (matcher.find()) {
            for (int i = 0; i < matcher.groupCount(); i++) {
                String varName = matcher.group(i + 1);
                if (varName.endsWith("+")) {
                    path = path.replace(matcher.group(0), "(.*)");
                    wildcardPath = wildcardPath.replace(matcher.group(0), "#");
                    varName = varName.substring(0, varName.length()-1);
                } else {
                    path = path.replace(matcher.group(0), "([^/]*)");
                    wildcardPath = wildcardPath.replace(matcher.group(0), "*");
                }
                if(varNames.contains(varName)) {
                    throw new IllegalArgumentException("Duplicate path variable name [" + varName + "]");
                }
                varNames.add(varName);
            }
        }
        this.wildcardPath = wildcardPath;
        this.pathPattern = Pattern.compile(path);
        this.pathVariableNames = varNames.toArray(new String[0]);
    }

    private static class RouteFilterConfigImpl implements RouteFilterConfig {
        private final RouteFilterType descriptor;

        public RouteFilterConfigImpl(RouteFilterType descriptor) {
            this.descriptor = descriptor;
        }

        @Override
        public String getFilterType() {
            return descriptor.getClazz();
        }

        @Override
        public Map<String,String> getInitParams() {
            return descriptor.getFilterParams()
                             .stream()
                             .collect(Collectors.toUnmodifiableMap(RouteFilterType.FilterParam::getName,
                                                                   RouteFilterType.FilterParam::getValue));
        }

        @Override
        public List<ResourceRef> getResourceRefs() {
            return descriptor.getResourceRefs().stream().map(refType -> new ResourceRef() {
                @Override
                public String getRefName() {
                    return refType.getRefName();
                }

                @Override
                public String getResourceName() {
                    return refType.getResourceName();
                }
            }).collect(Collectors.toUnmodifiableList());
        }
    }
}
