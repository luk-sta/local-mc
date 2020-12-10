package local.mc;

import javax.annotation.Nullable;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Predicate;

final class CacheMap extends LinkedHashMap<String, CacheEntry> {
    private static final long ENTRY_ADDITIONAL_BYTES = 3 * 8;
    private final long maxSize;
    private AtomicLong byteSize = new AtomicLong();

    CacheMap(long maxSize) {
        super(1000, 0.7f);
        this.maxSize = maxSize;
    }

    @Override
    public synchronized CacheEntry get(Object key) {
        CacheEntry cacheEntry = super.get(key);
        if (cacheEntry == null) {
            return null;
        }
        if (cacheEntry.isInvalid()) {
            remove(key);
            cacheEntry = null;
        }
        return cacheEntry;
    }

    @Override
    public synchronized CacheEntry put(String key, CacheEntry value) {
        CacheEntry old = super.put(key, value);
        long addedSize = getSize(key, value) - getSize(key, old);
        byteSize.addAndGet(addedSize);

        cleanCache(CacheEntry::isInvalid);
        cleanCache(c -> true);

        return old;
    }

    private void cleanCache(Predicate<CacheEntry> toRemove) {
        long totalSize = byteSize.get();
        Iterator<Map.Entry<String, CacheEntry>> iterator = entrySet().iterator();
        while (totalSize > maxSize && iterator.hasNext()) {
            Map.Entry<String, CacheEntry> next = iterator.next();
            CacheEntry cacheEntry = next.getValue();
            if (toRemove.test(cacheEntry)) {
                iterator.remove();
                totalSize = byteSize.addAndGet(-1 * getSize(next.getKey(), cacheEntry));
            }
        }
    }

    private long getSize(String key, @Nullable CacheEntry cacheEntry) {
        if (cacheEntry == null) {
            return 0;
        }
        return cacheEntry.getItemSize() + ENTRY_ADDITIONAL_BYTES + key.getBytes().length;
    }
}
