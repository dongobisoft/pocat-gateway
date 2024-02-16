package io.pocat.gateway.connector;

public class ServerConnectorOptions {
    private static final int DEFAULT_RECEIVE_BUFFER_SIZE = 4*1024*1024; // 4M
    private static final int DEFAULT_SEND_BUFFER_SIZE = 4*1024*1024; // 4M
    private static final int DEFAULT_LOW_WATERMARK = 2 * 1024 * 1024;
    private static final int DEFAULT_HIGH_WATERMARK = 4 * 1024 * 1024;
    private static final boolean DEFAULT_TCP_NO_DELAY = true;
    private static final boolean DEFAULT_REUSE_ADDRESS = true;
    private static final int DEFAULT_BACKLOG_SIZE = 1024;
    private static final int DEFAULT_CONNECT_TIMEOUT_MILLIS = 1000;

    private int rcvBuf = DEFAULT_RECEIVE_BUFFER_SIZE;
    private int sndBuf = DEFAULT_SEND_BUFFER_SIZE;
    private int lowWaterMark = DEFAULT_LOW_WATERMARK;
    private int highWaterMark = DEFAULT_HIGH_WATERMARK;
    private boolean tcpNoDelay = DEFAULT_TCP_NO_DELAY;
    private boolean reuseAddress = DEFAULT_REUSE_ADDRESS;
    private int backLog = DEFAULT_BACKLOG_SIZE;
    private int connectTimeOutMillis = DEFAULT_CONNECT_TIMEOUT_MILLIS;

    public int getRcvBuf() {
        return rcvBuf;
    }

    public void setRcvBuf(int rcvBuf) {
        this.rcvBuf = rcvBuf;
    }

    public int getSndBuf() {
        return sndBuf;
    }

    public void setSndBuf(int sndBuf) {
        this.sndBuf = sndBuf;
    }

    public int getLowWaterMark() {
        return lowWaterMark;
    }

    public void setLowWaterMark(int lowWaterMark) {
        this.lowWaterMark = lowWaterMark;
    }

    public int getHighWaterMark() {
        return highWaterMark;
    }

    public void setHighWaterMark(int highWaterMark) {
        this.highWaterMark = highWaterMark;
    }

    public boolean isTcpNoDelay() {
        return tcpNoDelay;
    }

    public void setTcpNoDelay(boolean tcpNoDelay) {
        this.tcpNoDelay = tcpNoDelay;
    }

    public boolean isReuseAddress() {
        return reuseAddress;
    }

    public void setReuseAddress(boolean reuseAddress) {
        this.reuseAddress = reuseAddress;
    }

    public int getBackLog() {
        return backLog;
    }

    public void setBackLog(int backLog) {
        this.backLog = Math.max(DEFAULT_BACKLOG_SIZE, backLog);
    }

    public int getConnectTimeOutMillis() {
        return connectTimeOutMillis;
    }

    public void setConnectTimeOutMillis(int connectTimeOutMillis) {
        this.connectTimeOutMillis = connectTimeOutMillis;
    }
}
