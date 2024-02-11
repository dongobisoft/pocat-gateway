package io.pocat.gateway.connector.http;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.handler.codec.http.*;
import io.pocat.gateway.connector.ConnectionHandler;

import java.util.List;
import java.util.Map;

import static io.netty.handler.codec.http.HttpResponseStatus.CONTINUE;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;

public class HttpConnectionHandler implements ConnectionHandler {
    @Override
    public void handleConnection(Channel channel) {
        ChannelPipeline cp = channel.pipeline();

        cp.addLast("http-request-decoder", new HttpRequestDecoder());
        cp.addLast("http-response-encoder", new HttpResponseEncoder());
        cp.addLast("http-responder", new HttpResponder());
        cp.addLast("http-request-aggregator", new HttpRequestAggregator());
    }

    private static class HttpRequestAggregator extends ChannelInboundHandlerAdapter {
        private HttpExchangeBuilder exchangeBuilder;

        @Override
        public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
            if(exchangeBuilder == null) {
                exchangeBuilder = new HttpExchangeBuilder();
            }
            if (msg instanceof HttpRequest) {
                HttpRequest request = (HttpRequest) msg;
                exchangeBuilder.setRequestMethod(request.method().name());

                HttpHeaders headers = request.headers();
                for(Map.Entry<String, String> header:headers.entries()) {
                    exchangeBuilder.setRequestHeader(header.getKey(), header.getValue());
                }

                QueryStringDecoder decoderQuery = new QueryStringDecoder(request.uri());

                Map<String, List<String>> uriAttributes = decoderQuery.parameters();
                exchangeBuilder.setQueryString(decoderQuery.rawQuery());
                exchangeBuilder.setQueryParams(uriAttributes);
                exchangeBuilder.setRequestPath(decoderQuery.path());

                if (HttpUtil.is100ContinueExpected(request)) {
                    FullHttpResponse response = new DefaultFullHttpResponse(HTTP_1_1, CONTINUE,
                            Unpooled.EMPTY_BUFFER);
                    ctx.writeAndFlush(response);
                }
            }

            if (msg instanceof HttpContent) {
                ByteBuf buf = ((HttpContent) msg).content();
                try {
                    exchangeBuilder.writeRequestContents(ByteBufUtil.getBytes(buf));
                } finally {
                    buf.release();
                }

                if (msg instanceof LastHttpContent) {
                    ctx.fireChannelRead(this.exchangeBuilder.build());
                    exchangeBuilder = null;
                }
            }
        }
    }

    private static class HttpResponder extends ChannelOutboundHandlerAdapter {
        @Override
        public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
            if(!(msg instanceof HttpExchange)) {
                throw new IllegalStateException("Invalid message [" + msg.getClass().getName() + "]");
            }
            HttpExchange exchange = (HttpExchange) msg;
            DefaultFullHttpResponse resp = new DefaultFullHttpResponse(
                    HttpVersion.HTTP_1_1,
                    HttpResponseStatus.valueOf(exchange.getResponseStatus()), Unpooled.copiedBuffer(exchange.getResponseContents()));

            for(String headerName:exchange.getResponseHeaderNames()) {
                resp.headers().set(headerName, exchange.getResponseHeader(headerName));
            }

            resp.headers().set(HttpHeaderNames.CONTENT_LENGTH, exchange.getResponseContents().length);
            ctx.writeAndFlush(resp);
        }
    }
}
