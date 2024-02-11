package io.pocat.utils;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class ExpireRegistry<K, V> {
    private final ExpiredEventHandler<K, V> handler;
    private final ReadWriteLock rwLock = new ReentrantReadWriteLock();
    private final Lock readLock = rwLock.readLock();
    private final Lock writeLock = rwLock.writeLock();

    private final ScheduledExecutorService scheduler;

    private final Map<K, V> entries = new HashMap<>();
    private final Map<K, Future<?>> futureMap = new HashMap<>();

    private ExpireRegistry(Builder<K, V> builder) {
        scheduler = Executors.newScheduledThreadPool(builder.threadPoolSize);
        handler = builder.handler;
    }

    public void register(K key, V value, long expireIn) {
        register(key, value, expireIn, TimeUnit.MILLISECONDS);
    }

    public void register(K key, V value, long expireIn, TimeUnit timeUnit) {
        writeLock.lock();
        try {
            entries.put(key, value);
            Future<?> scheduleFuture = scheduler.schedule(() -> {
                V entryValue = entries.remove(key);
                handler.handle(key, entryValue);
            }, expireIn, timeUnit);
            futureMap.put(key, scheduleFuture);
        } finally {
            writeLock.unlock();
        }
    }

    public V unregister(K key) {
        writeLock.lock();
        try {
            Future<?> scheduleFuture = futureMap.remove(key);
            if(!scheduleFuture.cancel(false)) {
                entries.remove(key);
                return null;
            }
            return entries.remove(key);
        } finally {
            writeLock.unlock();
        }
    }

    public V get(K key) {
        readLock.lock();
        try {
            return entries.get(key);
        } finally {
            readLock.unlock();
        }
    }


    public static final class Builder<K, V> {
        private ExpiredEventHandler<K, V> handler = (key, value) -> {/* do nothing */};
        private int threadPoolSize = 1;

        public ExpireRegistry<K, V> build() {
            return new ExpireRegistry<>( this);
        }

        public Builder<K,V> setExpiredEventHandler(ExpiredEventHandler<K, V> handler) {
            this.handler = handler;
            return this;
        }

        public Builder<K, V> setThreadPoolSize(int size) {
            this.threadPoolSize = size;
            return this;
        }
    }

    public interface ExpiredEventHandler<K, V> {
        void handle(K key, V value);
    }
}
