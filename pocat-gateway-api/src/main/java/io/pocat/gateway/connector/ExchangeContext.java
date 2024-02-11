package io.pocat.gateway.connector;

public interface ExchangeContext {
    Connection getConnection();
    void write(Exchange exchange);
}
