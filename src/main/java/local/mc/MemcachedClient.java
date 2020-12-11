package local.mc;

import java.io.IOException;
import java.io.Serializable;
import java.util.Collections;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.TimeUnit;

public class MemcachedClient implements MemcachedApi {
    private final Map<String, CacheEntry> cacheMap;
    private final Random random = new Random();

    public MemcachedClient(long maxSizeBytes) {
        cacheMap = Collections.synchronizedMap(new CacheMap(maxSizeBytes));
    }

    @Override
    public Serializable get(String key) {
        CacheEntry cacheEntry = cacheMap.get(key);
        return (cacheEntry == null) ? null : cacheEntry.getItem();
    }

    @Override
    public boolean set(String key, int expirationSeconds, Serializable o) throws IOException {
        long eolTime = (expirationSeconds > TimeUnit.DAYS.toSeconds(expirationSeconds)) ?
                expirationSeconds * 1000L : System.currentTimeMillis() + expirationSeconds * 1000L;
        CacheEntry cacheEntry = new CacheEntry(o, eolTime, random.nextLong());
        cacheMap.put(key, cacheEntry);
        return true;
    }

    @Override
    public void flushAll() {
        cacheMap.clear();
    }
}
