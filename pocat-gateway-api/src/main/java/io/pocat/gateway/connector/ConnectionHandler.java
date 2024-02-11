package io.pocat.gateway.connector;

import io.netty.channel.Channel;

public interface ConnectionHandler {
    void handleConnection(Channel channel);
}
