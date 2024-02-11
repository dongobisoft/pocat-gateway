package io.pocat.context.descriptor;

import java.util.List;

public interface MessageChannelDescriptor {
    String getChannelName();
    String getEndpointRef();
    List<Argument> getArguments();
}
