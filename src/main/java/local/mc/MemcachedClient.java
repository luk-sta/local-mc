package local.mc;

public class MemcachedClient implements MemcachedApi {
    private final CacheMap cacheMap;

    public MemcachedClient(long maxSizeBytes) {
        cacheMap = new CacheMap(maxSizeBytes);
    }

    @Override
    public <T> T get(String key) {
        //TODO
        return null;
    }
}
