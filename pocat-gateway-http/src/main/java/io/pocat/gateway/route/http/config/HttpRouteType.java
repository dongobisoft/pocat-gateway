package io.pocat.gateway.route.http.config;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import io.pocat.gateway.route.UpstreamConfigType;

import java.util.List;

public class HttpRouteType {
    private String name;
    private String description;
    private String method;
    private String path;
    @JacksonXmlProperty(localName = "expire-in")
    private long expireIn;
    @JacksonXmlProperty(localName = "success-code")
    private int successCode;
    private UpstreamConfigType upstream;
    @JacksonXmlElementWrapper(localName = "request-filters")
    private List<RouteFilterType> requestFilters;

    @JacksonXmlElementWrapper(localName = "response-filters")
    private List<RouteFilterType> responseFilters;
    @JacksonXmlElementWrapper(localName = "http-error-templates")
    private List<HttpErrorTemplateType> errorTemplates;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public long getExpireIn() {
        return expireIn;
    }

    public void setExpireIn(long expireIn) {
        this.expireIn = expireIn;
    }

    public int getSuccessCode() {
        return successCode;
    }

    public void setSuccessCode(int successCode) {
        this.successCode = successCode;
    }

    public UpstreamConfigType getUpstream() {
        return upstream;
    }

    public void setUpstream(UpstreamConfigType upstream) {
        this.upstream = upstream;
    }

    public List<RouteFilterType> getRequestFilters() {
        return requestFilters;
    }

    public void setRequestFilters(List<RouteFilterType> requestFilters) {
        this.requestFilters = requestFilters;
    }

    public List<RouteFilterType> getResponseFilters() {
        return responseFilters;
    }

    public void setResponseFilters(List<RouteFilterType> responseFilters) {
        this.responseFilters = responseFilters;
    }

    public List<HttpErrorTemplateType> getErrorTemplates() {
        return errorTemplates;
    }

    public void setErrorTemplates(List<HttpErrorTemplateType> errorTemplates) {
        this.errorTemplates = errorTemplates;
    }
}
