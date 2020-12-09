package local.mc;

import java.io.Serializable;
import java.util.Map;

public class MemcachedClient implements MemcachedApi {
    private final Map<String, CacheEntry> cacheMap;

    public MemcachedClient(long maxSizeBytes) {
        cacheMap = new CacheMap(maxSizeBytes);
    }

    @Override
    public <T> T get(String key) {
        //TODO
        return null;
    }

    @Override
    public void flushAll() {
        cacheMap.clear();
    }
}
