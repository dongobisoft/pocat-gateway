package io.pocat.message.channel.adaptor;

import io.pocat.message.endpoint.MessageEndpoint;

import java.util.Set;

public interface ChannelConfig {
    String getChannelName();
    MessageEndpoint getMessageEndpoint();
    Set<String> getArgumentNames();
    String getArgument(String name);
}
