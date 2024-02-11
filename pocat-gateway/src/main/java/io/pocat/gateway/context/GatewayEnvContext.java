package io.pocat.gateway.context;

import io.pocat.context.EnvContext;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

class GatewayEnvContext implements EnvContext {
    private final String name;
    private final String path;
    private final Set<String> children = new HashSet<>();

    private Object obj;

    public GatewayEnvContext(String path) {
        this.path = path;
        this.name = path.substring(path.lastIndexOf(EnvContext.SEPARATOR)+1);
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getPath() {
        return path;
    }

    @Override
    public boolean hasObject() {
        return obj != null;
    }

    @Override
    public Object getObject() {
        return obj;
    }

    public void setObject(Object obj) {
        this.obj = obj;
    }

    public void addChild(String name) {
        children.add(name);
    }

    public Set<String> listChildren() {
        return Collections.unmodifiableSet(children);
    }
}
