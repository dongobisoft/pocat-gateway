package io.pocat.message.endpoint;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class MessageEndpointConfigBuilder {
    private final String name;
    private final String type;
    private final Map<String, String> args = new HashMap<>();

    public MessageEndpointConfigBuilder(String name, String type) {
        this.name = name;
        this.type = type;
    }

    public MessageEndpointConfigBuilder addArgument(String name, String value) {
        args.put(name, value);
        return this;
    }

    public MessageEndpointConfig build() {
        return new MessageEndpointConfig() {
            @Override
            public String getEndpointName() {
                return name;
            }

            @Override
            public String getType() {
                return type;
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
