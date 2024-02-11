package io.pocat.context;

import java.lang.reflect.InvocationTargetException;
import java.util.Properties;

public class EnvContextManager {
    public static final EnvContextManager INSTANCE = new EnvContextManager();

    private static final String DEFAULT_CONTEXT_FACTORY_PROPERTY_NAME = "io.pocat.env.context.factory";

    private EnvContextProviderFactory ctxProviderFactory;

    public static EnvContextManager getInstance() {
        return INSTANCE;
    }

    private EnvContextManager() {

    }

    public EnvContextProvider getDefaultContextProvider() {
        return getDefaultContextProvider(System.getProperties());
    }

    public EnvContextProvider getDefaultContextProvider(Properties properties) {
        if(ctxProviderFactory == null) {
            createDefaultContextFactory(properties);
        }
        return ctxProviderFactory.createDefaultContextProvider(properties);
    }

    public synchronized void setContextProviderFactory(EnvContextProviderFactory ctxProviderFactory) {
        if(this.ctxProviderFactory != null) {
            throw new IllegalStateException("ContextFactory already set.");
        }
        this.ctxProviderFactory = ctxProviderFactory;
    }

    private synchronized void createDefaultContextFactory(Properties properties) {
        if(ctxProviderFactory != null) {
            return;
        }
        String defaultCtxProviderFactoryClassName = properties.getProperty(DEFAULT_CONTEXT_FACTORY_PROPERTY_NAME);
        if(defaultCtxProviderFactoryClassName == null) {
            throw new IllegalArgumentException(DEFAULT_CONTEXT_FACTORY_PROPERTY_NAME + " property does not exist.");
        }
        try {
            Class<?> defaultContextFactoryClass = Class.forName(defaultCtxProviderFactoryClassName);
            if(!defaultContextFactoryClass.isAssignableFrom(EnvContextProviderFactory.class)) {
                throw new IllegalArgumentException("Class [" + defaultCtxProviderFactoryClassName + "] does not implement " + EnvContextProviderFactory.class.getName());
            }
            ctxProviderFactory = (EnvContextProviderFactory) defaultContextFactoryClass.getDeclaredConstructor().newInstance();

        } catch (ClassNotFoundException e) {
            throw new IllegalArgumentException("The class [" + defaultCtxProviderFactoryClassName + "] does not found.", e);
        } catch (InvocationTargetException e) {
            throw new IllegalArgumentException("Exception occurred during creating instance of the class [" + defaultCtxProviderFactoryClassName + "].", e);
        } catch (InstantiationException e) {
            throw new IllegalArgumentException("Cannot instantiate the class [" + defaultCtxProviderFactoryClassName + "].", e);
        } catch (IllegalAccessException e) {
            throw new IllegalArgumentException("Cannot access default constructor of the class [" + defaultCtxProviderFactoryClassName + "].", e);
        } catch (NoSuchMethodException e) {
            throw new IllegalArgumentException("No default constructor exist for class [" + defaultCtxProviderFactoryClassName + "].", e);
        }
    }
}
