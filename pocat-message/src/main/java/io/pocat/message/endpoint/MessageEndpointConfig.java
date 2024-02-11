package io.pocat.message.endpoint;

import java.util.Set;

public interface MessageEndpointConfig {
    String getEndpointName();
    String getType();
    Set<String> getArgumentNames();
    String getArgument(String name);
}
