package local.mc;

import java.util.LinkedHashMap;
import java.util.Map;

public class MemcachedClient implements MemcachedApi {
    private final CacheMap cacheMap = new CacheMap();
    @Override
    public <T> T get(String key) {
        //TODO
        return null;
    }
}
