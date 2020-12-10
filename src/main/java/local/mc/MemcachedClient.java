package local.mc;

import java.io.Serializable;
import java.util.Map;

public class MemcachedClient implements MemcachedApi {
    private final Map<String, CacheEntry> cacheMap;

    public MemcachedClient(long maxSizeBytes) {
        cacheMap = new CacheMap(maxSizeBytes);
    }

    @Override
    public Serializable get(String key) {
        CacheEntry cacheEntry = cacheMap.get(key);
        return (cacheEntry == null) ? null : cacheEntry.getItem();
    }

    @Override
    public void flushAll() {
        cacheMap.clear();
    }
}
