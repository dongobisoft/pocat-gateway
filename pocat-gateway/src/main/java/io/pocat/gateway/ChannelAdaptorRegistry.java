package io.pocat.gateway;

import io.pocat.message.channel.adaptor.OutboundChannelAdaptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class ChannelAdaptorRegistry {
    private static final Logger LOGGER = LoggerFactory.getLogger(ChannelAdaptorRegistry.class);
    private static final ChannelAdaptorRegistry INSTANCE = new ChannelAdaptorRegistry();

    private final Map<String, OutboundChannelAdaptor> adaptors = new HashMap<>();

    private final ReentrantReadWriteLock rwLock = new ReentrantReadWriteLock();
    private final Lock writeLock = rwLock.writeLock();
    private final Lock readLock = rwLock.readLock();

    public static ChannelAdaptorRegistry getInstance() {
        return INSTANCE;
    }

    private ChannelAdaptorRegistry() {

    }

    public void registerOutboundChannelAdaptor(String channelName, OutboundChannelAdaptor adaptor) {
        try {
            writeLock.lock();
            adaptors.put(channelName, adaptor);
        } finally {
            writeLock.unlock();
        }
    }

    public OutboundChannelAdaptor getOutboundChannelAdaptor(String channelName) throws IOException {
        try {
            readLock.lock();
            return adaptors.get(channelName);
        } finally {
            readLock.unlock();
        }
    }

    public void clear() {
        writeLock.lock();
        try {
            for (OutboundChannelAdaptor adaptor : adaptors.values()) {
                try {
                    adaptor.close();
                } catch (IOException e) {
                    LOGGER.error("Exception thrown during close adaptor [" + adaptor.getName() + "].", e);
                }
            }
            adaptors.clear();
        } finally {
            writeLock.unlock();
        }
    }
}
