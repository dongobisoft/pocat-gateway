package io.pocat.message.channel.adaptor;

import io.pocat.message.endpoint.MessageEndpoint;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class ChannelConfigBuilder {
    private final String channelName;
    private final Map<String, String> args = new HashMap<>();

    private MessageEndpoint endpoint;

    public ChannelConfigBuilder(String channelName) {
        this.channelName = channelName;
    }

    public ChannelConfigBuilder addArgument(String name, String value) {
        this.args.put(name, value);
        return this;
    }

    public void setEndpoint(MessageEndpoint endpoint) {
        this.endpoint = endpoint;
    }

    public ChannelConfig build() {
        if(endpoint == null) {
            throw new IllegalArgumentException("Endpoint does not exist.");
        }
        return new ChannelConfig() {
            @Override
            public String getChannelName() {
                return channelName;
            }

            @Override
            public MessageEndpoint getMessageEndpoint() {
                return endpoint;
            }

            @Override
            public Set<String> getArgumentNames() {
                return args.keySet();
            }

            @Override
            public String getArgument(String name) {
                return args.get(name);
            }
        };
    }
}
