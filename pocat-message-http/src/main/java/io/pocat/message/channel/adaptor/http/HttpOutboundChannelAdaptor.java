package io.pocat.message.channel.adaptor.http;

import io.pocat.message.Message;
import io.pocat.message.MessageBuilder;
import io.pocat.message.MessageHeaders;
import io.pocat.message.channel.ConsumableMessageChannel;
import io.pocat.message.channel.MessageChannel;
import io.pocat.message.channel.adaptor.ChannelConfig;
import io.pocat.message.channel.adaptor.OutboundChannelAdaptor;
import io.pocat.message.channel.endpoint.http.HttpConnector;

import java.io.IOException;
import java.net.http.HttpResponse;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;

public class HttpOutboundChannelAdaptor implements OutboundChannelAdaptor {
    public static final String METHOD_ARG_NAME = "method";
    public static final String CONTEXT_PATH_ARG_NAME = "context-path";

    private final String name;
    private final HttpConnector connector;
    private final String method;
    private final String contextPath;

    private MessageChannel channel;
    private ExecutorService executor;

    public HttpOutboundChannelAdaptor(ChannelConfig channelConfig) {
        this.name = channelConfig.getChannelName();
        this.connector = (HttpConnector) channelConfig.getMessageEndpoint().getConnector();
        this.method = channelConfig.getArgument(METHOD_ARG_NAME);
        this.contextPath = channelConfig.getArgument(CONTEXT_PATH_ARG_NAME) == null?"":channelConfig.getArgument(CONTEXT_PATH_ARG_NAME);
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public MessageChannel getChannel() {
        return channel;
    }

    @Override
    public void setChannel(ConsumableMessageChannel channel) {
        this.channel = channel;
        channel.consume(message -> {
            String uri = contextPath + "/" + message.getTopic().replaceAll("\\.", "/");
            CompletableFuture<HttpResponse<byte[]>> cf = connector.send(uri, method, message.getHeaders(), message.getPayload());
            cf.thenAccept(response -> {
                if (executor != null) {
                    executor.submit(() -> processMessage(response, message));
                } else {
                    processMessage(response, message);
                }
            });
        });
    }

    @Override
    public ExecutorService getExecutor() {
        return executor;
    }

    @Override
    public void setExecutor(ExecutorService executor) {
        this.executor = executor;
    }

    @Override
    public void close() {

    }

    private void processMessage(HttpResponse<byte[]> response, Message message) {
        MessageBuilder builder = new MessageBuilder(message.getTxId(), message.getHeaders().getReplyTopic());
        if(response.statusCode() < 100 || response.statusCode() > 399) {
            builder.addHeader(MessageHeaders.STATUS_CODE_HEADER_NAME, response.statusCode() * 100);
        }
        Set<String> headerNames = response.headers().map().keySet();

        for(String name:headerNames) {
            builder.addHeader(name, response.headers().firstValue(name));
        }

        builder.setPayload(response.body());
        Message responseMessage = builder.build();
        MessageChannel replyChannel = (MessageChannel) message.getHeaders().get("Reply-To");
        replyChannel.publish(responseMessage);
    }
}
