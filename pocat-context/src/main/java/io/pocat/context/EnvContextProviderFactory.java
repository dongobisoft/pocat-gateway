package io.pocat.context;

import java.util.Properties;

public interface EnvContextProviderFactory {
    EnvContextProvider createDefaultContextProvider(Properties properties);
}
