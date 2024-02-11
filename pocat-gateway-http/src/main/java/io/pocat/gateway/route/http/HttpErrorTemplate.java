package io.pocat.gateway.route.http;

import io.pocat.gateway.route.ErrorTemplate;
import io.pocat.gateway.route.http.config.HttpErrorTemplateType;
import io.pocat.gateway.route.http.config.HttpResponseFormatType;
import io.pocat.gateway.route.http.config.HttpResponseHeaderType;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class HttpErrorTemplate implements ErrorTemplate {
    private final HttpErrorTemplateType descriptor;
    private final Map<String, HttpResponseFormat> formats = new HashMap<>();

    public HttpErrorTemplate(HttpErrorTemplateType descriptor) {
        this.descriptor = descriptor;

        for(HttpResponseFormatType respFormat:descriptor.getResponseFormats()) {
            Map<String,String>responseHeaders = respFormat.getResponseHeaders().stream().collect(Collectors.toMap(HttpResponseHeaderType::getName, HttpResponseHeaderType::getValue));
            formats.put(respFormat.getContentType(), new HttpResponseFormat() {
                @Override
                public Map<String, String> getResponseHeaders() {
                    return responseHeaders;
                }

                @Override
                public String getResponseBody() {
                    return respFormat.getResponseBody();
                }
            });
        }
    }

    @Override
    public int getErrorCode() {
        return descriptor.getErrorCode();
    }

    public int getStatusCode() {
        return this.descriptor.getResponseCode();
    }

    public Set<String> getMimeTypes() {
        return this.formats.keySet();
    }

    public HttpResponseFormat getResponseFormat(String mimeType) {
        return this.formats.get(mimeType);
    }
}
