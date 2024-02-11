package io.pocat.gateway.connector.http;

import io.pocat.gateway.connector.Exchange;

import java.util.Collection;
import java.util.List;
import java.util.Set;

public interface HttpExchange extends Exchange {
    String CORRELATION_ID_HEADER_NAME = "Correlation-ID";

    String getRequestPath();
    String getRequestMethod();

    String getRequestHeader(String headerName);
    Collection<String> getRequestHeaderNames();

    String getRequestParam(String paramName);
    Collection<String> getRequestParamNames();
    List<String> getRequestParams(String paramName);

    String getQueryString();
    String getAcceptableMimeType(Set<String> types);

    int	getResponseStatus();
    void setResponseStatus(int status);

    Set<String> getResponseHeaderNames();
    String	getResponseHeader(String headerName);
    void setResponseHeader(String headerName, String headerValue);
}
