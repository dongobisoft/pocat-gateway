package io.pocat.context;

public interface EnvContext {
    String SEPARATOR = "/";

    String getName();
    String getPath();
    boolean hasObject();
    Object getObject();
}
