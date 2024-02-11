package io.pocat.context;

import java.io.IOException;
import java.util.Set;

public interface EnvContextProvider {
    Object lookup(String ctxPath) throws IOException;
    Set<String> listChildren(String ctxPath) throws IOException;
    boolean isExist(String ctxPath) throws IOException;
    boolean hasObject(String ctxPath) throws IOException;
    void bind(String ctxPath, Object obj) throws IOException;
    void close();
}
