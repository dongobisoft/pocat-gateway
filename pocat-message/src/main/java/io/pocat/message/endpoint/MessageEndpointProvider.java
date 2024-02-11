package io.pocat.message.endpoint;

import java.util.HashMap;
import java.util.Map;
import java.util.ServiceLoader;

public class MessageEndpointProvider {
    private static final MessageEndpointProvider INSTANCE = new MessageEndpointProvider();

    private final Map<String, MessageEndpoint> endpoints = new HashMap<>();

    private MessageEndpointFactory[] factories;
    private ServiceLoader<MessageEndpointFactory> factoriesLoader;

    public static MessageEndpointProvider getInstance() {
        return INSTANCE;
    }

    private MessageEndpointProvider() {
        reload();
    }

    public MessageEndpoint getChannelEndpoint(MessageEndpointConfig config) {
        if(!endpoints.containsKey(config.getEndpointName())) {
            synchronized (endpoints) {
                if(!endpoints.containsKey(config.getEndpointName())) {
                    endpoints.put(config.getEndpointName(), createEndpoint(config));
                }
            }
        }
        return endpoints.get(config.getEndpointName());
    }

    private MessageEndpoint createEndpoint(MessageEndpointConfig config) {
        for(MessageEndpointFactory factory:factories) {
            if(factory.isSupportedEndpointType(config.getType())) {
                return factory.createEndpoint(config);
            }
        }

        throw new IllegalArgumentException("Not supported protocol [" + config.getType() + "] for endpoint [" + config.getEndpointName() + "]");
    }

    public void reload() {
        if(factoriesLoader == null) {
            factoriesLoader = ServiceLoader.load(MessageEndpointFactory.class);
        } else {
            factoriesLoader.reload();
        }
        factories = factoriesLoader.stream().map(ServiceLoader.Provider::get).toArray(MessageEndpointFactory[]::new);
    }
}
