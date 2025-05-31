package com.knemis.skyblock.skyblockcoreproject.utils;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

public class TemporaryCache<K, V> {
    private final Map<K, CacheEntry<V>> cache = new ConcurrentHashMap<>();

    private static class CacheEntry<V> {
        final V value;
        final Instant expiryTime;

        CacheEntry(V value, Duration validity) {
            this.value = value;
            this.expiryTime = Instant.now().plus(validity);
        }

        boolean isValid() {
            return Instant.now().isBefore(expiryTime);
        }
    }

    public V get(K key, Duration validity, Supplier<V> valueSupplier) {
        CacheEntry<V> entry = cache.get(key);
        if (entry != null && entry.isValid()) {
            return entry.value;
        }
        V newValue = valueSupplier.get();
        cache.put(key, new CacheEntry<>(newValue, validity));
        return newValue;
    }

    public void invalidate(K key) {
        cache.remove(key);
    }

    public void clear() {
        cache.clear();
    }
}
