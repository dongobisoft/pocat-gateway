package io.pocat.gateway.route.http;

import java.util.Map;

public interface HttpResponseFormat {
    Map<String, String> getResponseHeaders();
    String getResponseBody();
}
