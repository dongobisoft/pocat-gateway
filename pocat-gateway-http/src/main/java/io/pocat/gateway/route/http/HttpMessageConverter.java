package io.pocat.gateway.route.http;

import io.pocat.gateway.connector.Exchange;
import io.pocat.gateway.route.MessageConverter;
import io.pocat.message.Message;
import io.pocat.message.MessageBuilder;

public class HttpMessageConverter implements MessageConverter {
    @Override
    public void convertMessageToExchange(Message message, Exchange exchange) {
        // todo Move header to exchange
        exchange.setResponseContents(message.getPayload());
    }

    @Override
    public void convertExchangeToMessage(Exchange exchange, MessageBuilder message) {
        // todo Move header to message
        message.setPayload(exchange.getRequestContents());
    }
}
