package io.pocat.gateway.connector;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public abstract class AbstractExchange implements Exchange {
    private final String txId;
    private ExchangeContext ctx;
    private final Map<String, Object> attributes = new HashMap<>();
    private final long createdAt;
    private boolean isClosed = false;

    protected AbstractExchange() {
        this.createdAt = System.currentTimeMillis();
        this.txId = UUID.randomUUID().toString().replace("-", "") + this.createdAt;
    }

    @Override
    public String getTxId() {
        return txId;
    }

    @Override
    public long getCreatedAt() {
        return createdAt;
    }

    @Override
    public void setAttribute(String attrName, Object attrValue) {
        attributes.put(attrName, attrValue);
    }

    @Override
    public Object getAttribute(String attrName) {
        return attributes.get(attrName);
    }

    @Override
    public boolean isClosed() {
        return isClosed;
    }

    @Override
    public void close() {
        isClosed = true;
        ctx.write(this);
    }

    @Override
    public ExchangeContext getContext() {
        return this.ctx;
    }

    @Override
    public void setContext(ExchangeContext ctx) {
        this.ctx = ctx;
    }
}
