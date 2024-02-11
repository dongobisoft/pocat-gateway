package io.pocat.gateway.route;

public class RouteProcessException extends Exception {
    private int errorCode;

    public RouteProcessException(int errorCode, String msg) {
        super(msg);
        this.errorCode = errorCode;
    }

    public int getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(int errorCode) {
        this.errorCode = errorCode;
    }
}
