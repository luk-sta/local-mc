package local.mc;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

final class Cache {
    private final AtomicReference<Map<String, CacheEntry>> oldMap = new AtomicReference<>(new HashMap<>());
    private final AtomicReference<Map<String, CacheEntry>> currentMapRef = new AtomicReference<>(new HashMap<>());
    private final long maxSize;
    private final long cleanThreshold;
    private final AtomicLong totalSize = new AtomicLong();
    private final AtomicBoolean cleanRunning = new AtomicBoolean(false);

    Cache(long maxSize) {
        this.maxSize = maxSize;
        this.cleanThreshold = (long) (maxSize * 0.95);
    }

    public CacheEntry get(String key) {
        CacheEntry cacheEntry = currentMapRef.get().get(key);
        if (cacheEntry == null) {
            cacheEntry = oldMap.get().get(key);
        }
        if (cacheEntry == null || cacheEntry.isInvalid()) {
            return null;
        }
        cacheEntry.refreshLastAccessed();
        return cacheEntry;
    }

    public void put(String key, CacheEntry value) {
        CacheEntry old = currentMapRef.get().put(key, value);
        long addedSize = CacheEntry.getSize(key, value) - CacheEntry.getSize(key, old);
        totalSize.addAndGet(addedSize);
        cleanCache();
    }

    private void cleanCache() {
        if (cleanRunning.getAndSet(true)) {
            return;
        }
        if (totalSize.get() > cleanThreshold) {
            Cleaner cleaner = new Cleaner(oldMap, currentMapRef, maxSize, totalSize, cleanRunning);
            cleaner.start();
        } else {
            cleanRunning.set(false);
        }
    }

    public void clear() {
        //TODO
    }
}
