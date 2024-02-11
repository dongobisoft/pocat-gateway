package io.pocat.gateway.connector;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelPipeline;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.websocketx.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WebsocketConnectionHandler implements ConnectionHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(WebsocketConnectionHandler.class);

    private static final String WEBSOCKET_HTTP_CODEC_NAME = "websocket-http-codec";
    private static final String WEBSOCKET_UPGRADE_HANDLER_NAME = "websocket-upgrade-handler";
    private static final String WEBSOCKET_HANDLER_NAME = "websocket-upgrade-handler";

    private final String subProtocol;

    public WebsocketConnectionHandler(String subProtocol){
        this.subProtocol = subProtocol;
    }

    @Override
    public void handleConnection(Channel channel) {
        ChannelPipeline cp = channel.pipeline();
        cp.addLast(WEBSOCKET_HTTP_CODEC_NAME, new HttpServerCodec());
        cp.addLast(WEBSOCKET_UPGRADE_HANDLER_NAME, new WebSocketUpgradeHandler(this.subProtocol));
    }

    private static class WebSocketUpgradeHandler extends ChannelInboundHandlerAdapter {
        private static final String CONNECTION_HEADER_VALUE = "Upgrade";
        private static final String UPGRADE_HEADER_VALUE = "WebSocket";
        private static final String HOST_HEADER_NAME = "Host";

        private final String subProtocol;

        public WebSocketUpgradeHandler(String subProtocol) {
            this.subProtocol = subProtocol;
        }

        @Override
        public void channelRead(ChannelHandlerContext ctx, Object msg) {
            if (msg instanceof HttpRequest) {
                HttpRequest httpRequest = (HttpRequest) msg;
                HttpHeaders headers = httpRequest.headers();

                if (CONNECTION_HEADER_VALUE.equalsIgnoreCase(headers.get(HttpHeaderNames.CONNECTION)) &&
                        UPGRADE_HEADER_VALUE.equalsIgnoreCase(headers.get(HttpHeaderNames.UPGRADE))) {
                    // Replace Http handler to Websocket handler
                    ctx.channel().pipeline().replace(this, WEBSOCKET_HANDLER_NAME, new WebSocketHandler());
                    handleHandshake(ctx, httpRequest);
                }
            } else {
                LOGGER.warn("Not a http request. Close connection.");
                ctx.channel().close();
            }
        }

        protected void handleHandshake(ChannelHandlerContext ctx, HttpRequest req) {
            WebSocketServerHandshakerFactory wsFactory =
                    new WebSocketServerHandshakerFactory(req.headers().get(HOST_HEADER_NAME), this.subProtocol, true);
            WebSocketServerHandshaker handShaker = wsFactory.newHandshaker(req);
            if (handShaker == null) {
                WebSocketServerHandshakerFactory.sendUnsupportedVersionResponse(ctx.channel());
            } else {
                handShaker.handshake(ctx.channel(), req);
            }
        }
    }

    private static class WebSocketHandler extends ChannelInboundHandlerAdapter {
        @Override
        public void channelRead(ChannelHandlerContext ctx, Object msg) {
            if (msg instanceof WebSocketFrame) {
                ctx.fireChannelRead(((WebSocketFrame) msg).content());
            }
        }
    }
}
