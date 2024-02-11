package io.pocat.gateway.connector;

import java.net.InetSocketAddress;

public interface Connection {
    void setAttribute(String attrName, Object attrObj);
    Object getAttribute(String attrName);

    InetSocketAddress getLocalAddress();
    InetSocketAddress getRemoteAddress();

    void close();
}
