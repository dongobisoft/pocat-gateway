package io.pocat.gateway.route;

import io.pocat.gateway.connector.Exchange;
import io.pocat.message.Message;
import io.pocat.message.MessageBuilder;

public interface MessageConverter {
    void convertMessageToExchange(Message message, Exchange exchange);
    void convertExchangeToMessage(Exchange exchange, MessageBuilder message);
}
