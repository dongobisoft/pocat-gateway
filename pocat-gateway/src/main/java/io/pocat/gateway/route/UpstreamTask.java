package io.pocat.gateway.route;

import io.pocat.gateway.DownStreamProcedureRegistry;
import io.pocat.gateway.connector.Exchange;
import io.pocat.message.MessageBuilder;
import io.pocat.message.MessageHeaders;
import io.pocat.message.MessageStatusCode;
import io.pocat.message.channel.MessageChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class UpstreamTask {
    private static final Logger LOGGER = LoggerFactory.getLogger(UpstreamTask.class);
    private static final Pattern VARIABLE_PATTERN = Pattern.compile("\\$\\{([^}]+)}");

    private static final String ERROR_CODE_NAME = "error.code";
    private static final String ERROR_MESSAGE_NAME = "error.message";
    private static final String HEADER_PREFIX = "header:";

    private RouteErrorProcedure errorProcedure;
    private MessageConverter messageConverter;
    private MessageChannel outboundChannel;
    private String topicFormat;
    private MessageChannel replyChannel;
    private String replyTopic;

    public UpstreamTask() {

    }

    public void doTask(Exchange exchange, RouteProcedureChain chain) {
        DownStreamProcedure downStreamProcedure = message -> {
            Integer statusCode = (Integer) message.getHeaders().get(MessageHeaders.STATUS_CODE_HEADER_NAME);
            if(statusCode != null && statusCode != 0) {
                errorProcedure.call(exchange, new RouteProcessException(statusCode, new String(message.getPayload(), StandardCharsets.UTF_8)));
                return;
            }

            messageConverter.convertMessageToExchange(message, exchange);
            chain.doNext(exchange);
        };

        DownStreamProcedureRegistry.getInstance().register(exchange.getTxId(), downStreamProcedure);
        try {
            String topic = buildTopic(exchange);
            MessageBuilder builder = new MessageBuilder(exchange.getTxId(), topic);
            builder.setReply(replyChannel, replyTopic);
            messageConverter.convertExchangeToMessage(exchange, builder);
            outboundChannel.publish(builder.build());
        } catch (Exception e) {
            LOGGER.error("Exception thrown during upstream", e);
            errorProcedure.call(exchange, new RouteProcessException(MessageStatusCode.UNKNOWN_ERROR, "UnknownError"));
        }
    }

    private String buildTopic(Exchange exchange) {
        String result = topicFormat;
        Matcher matcher = VARIABLE_PATTERN.matcher(topicFormat);
        while(matcher.find()) {
            if(matcher.group(1).startsWith(HEADER_PREFIX)) {
                String value = exchange.getRequestHeader(matcher.group(1).substring(HEADER_PREFIX.length()));
                if(value != null) {
                    result = result.replace(matcher.group(), value);
                }
            } else {
                if(exchange.getAttribute(matcher.group(1)) != null) {
                    String value = (String) exchange.getAttribute(matcher.group(1));
                    result = result.replace(matcher.group(), value);
                }
            }
        }
        return result;
    }

    public void setOutboundChannel(MessageChannel outboundChannel) {
        this.outboundChannel = outboundChannel;
    }

    public void setMessageConverter(MessageConverter messageConverter) {
        this.messageConverter = messageConverter;
    }

    public void setTopicFormat(String topicFormat) {
        this.topicFormat = topicFormat;
    }

    public void setErrorProcedure(RouteErrorProcedure errorProcedure) {
        this.errorProcedure = errorProcedure;
    }

    public void setReplyChannel(MessageChannel replyChannel) {
        this.replyChannel = replyChannel;
    }

    public void setReplyTopic(String replyTopic) {
        this.replyTopic = replyTopic;
    }
}

