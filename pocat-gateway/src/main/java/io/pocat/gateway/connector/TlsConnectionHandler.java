package io.pocat.gateway.connector;

import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.pocat.gateway.config.TLSConfigType;

import javax.net.ssl.SSLException;
import java.io.InputStream;

public class TlsConnectionHandler implements ConnectionHandler {
    public static final String TLS_CONNECTION_FACTORY_IDENTIFIER = "tls-connection-factory";
    private final SslContext sslCtx;

    public TlsConnectionHandler(InputStream keyCertChainInputStream, InputStream keyInputStream, String keyPassword) throws SSLException {
        sslCtx = SslContextBuilder.forServer(keyCertChainInputStream, keyInputStream, keyPassword).build();
    }

    @Override
    public void handleConnection(Channel channel) {
        channel.pipeline().addLast(TLS_CONNECTION_FACTORY_IDENTIFIER, sslCtx.newHandler(channel.alloc()));
    }
}
