package io.pocat.context.descriptor;

import java.util.List;

public interface MessageEndpointDescriptor {
    String getName();
    String getType();
    List<Argument> getArguments();
}
