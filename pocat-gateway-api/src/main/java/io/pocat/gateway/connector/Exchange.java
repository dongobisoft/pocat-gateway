package io.pocat.gateway.connector;

import java.util.Collection;
import java.util.Set;

public interface Exchange {
    String getCorrelationId();
    String getTxId();
    long getCreatedAt();

    ExchangeContext getContext();
    void setContext(ExchangeContext ctx);

    String getRequestHeader(String headerName);
    Collection<String> getRequestHeaderNames();
    byte[] getRequestContents();

    Set<String> getResponseHeaderNames();
    String	getResponseHeader(String headerName);
    void setResponseHeader(String headerName, String headerValue);
    byte[] getResponseContents();
    void setResponseContents(byte[] contents);

    boolean isClosed();
    void close();

    void setAttribute(String attrName, Object attrValue);
    Object getAttribute(String attrName);
}
