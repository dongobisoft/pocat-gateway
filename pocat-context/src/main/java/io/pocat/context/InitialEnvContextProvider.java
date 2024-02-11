package io.pocat.context;

import java.io.IOException;
import java.util.Set;

public class InitialEnvContextProvider implements EnvContextProvider {
    private final EnvContextProvider defaultContextProvider;

    public InitialEnvContextProvider() {
        this.defaultContextProvider = EnvContextManager.getInstance().getDefaultContextProvider();
    }
    @Override
    public Object lookup(String ctxPath) throws IOException {
        return this.defaultContextProvider.lookup(ctxPath);
    }

    @Override
    public Set<String> listChildren(String ctxPath) throws IOException {
        return this.defaultContextProvider.listChildren(ctxPath);
    }

    @Override
    public boolean isExist(String ctxPath) throws IOException {
        return this.defaultContextProvider.isExist(ctxPath);
    }

    @Override
    public boolean hasObject(String ctxPath) throws IOException {
        return this.defaultContextProvider.hasObject(ctxPath);
    }

    @Override
    public void bind(String ctxPath, Object obj) throws IOException {
        this.defaultContextProvider.bind(ctxPath, obj);
    }

    @Override
    public void close() {
        defaultContextProvider.close();
    }
}
