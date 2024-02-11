package io.pocat.gateway.route.http;

import io.pocat.gateway.route.RouteProcessException;

public class InvalidProtocolException extends RouteProcessException {
    private static final int ERROR_CODE = 40000;
    public InvalidProtocolException(String protocol) {
        super(ERROR_CODE, "Not a [" + protocol + "] message.");
    }
}
