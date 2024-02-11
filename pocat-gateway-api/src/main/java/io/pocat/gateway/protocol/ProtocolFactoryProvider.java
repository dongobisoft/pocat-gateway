package io.pocat.gateway.protocol;

import java.util.ArrayList;
import java.util.List;
import java.util.ServiceLoader;

public class ProtocolFactoryProvider {
    private static final ProtocolFactoryProvider INSTANCE = new ProtocolFactoryProvider();

    private final List<ProtocolFactory> factories = new ArrayList<>();

    public static ProtocolFactoryProvider getInstance() {
        return INSTANCE;
    }

    private ProtocolFactoryProvider() {
        reload();
    }

    public ProtocolFactory provide(String protocol) {
        for(ProtocolFactory factory:this.factories) {
            if(factory.isSupportedProtocol(protocol)) {
                return factory;
            }
        }
        return null;
    }

    public synchronized void reload() {
        factories.clear();
        ServiceLoader<ProtocolFactory> protocolFactories = ServiceLoader.load(ProtocolFactory.class);
        for(ProtocolFactory factory:protocolFactories) {
            this.factories.add(factory);
        }
    }
}
