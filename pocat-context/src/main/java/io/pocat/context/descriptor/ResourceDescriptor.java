package io.pocat.context.descriptor;

import java.util.List;

public interface ResourceDescriptor {
    String getName();
    String getType();
    List<Argument> getArguments();
}
