package io.pocat.gateway.route.http.config;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;

import java.util.Collections;
import java.util.List;

public class HttpResponseFormatType {
    @JacksonXmlProperty(localName = "content-type")
    private String contentType;
    @JacksonXmlElementWrapper(localName = "response-headers")
    private List<HttpResponseHeaderType> responseHeaders;
    @JacksonXmlProperty(localName = "response-body")
    private String responseBody;

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public List<HttpResponseHeaderType> getResponseHeaders() {
        return responseHeaders==null? Collections.emptyList():responseHeaders;
    }

    public void setResponseHeaders(List<HttpResponseHeaderType> responseHeaders) {
        this.responseHeaders = responseHeaders;
    }

    public String getResponseBody() {
        return responseBody.trim();
    }

    public void setResponseBody(String responseBody) {
        this.responseBody = responseBody;
    }
}
