package io.pocat.gateway.context;

import io.pocat.context.EnvContextProvider;
import io.pocat.context.EnvContextProviderFactory;

import java.util.Properties;

public class GatewayContextProviderFactory implements EnvContextProviderFactory {
    @Override
    public EnvContextProvider createDefaultContextProvider(Properties properties) {
        return new GatewayContextProvider(properties);
    }
}
