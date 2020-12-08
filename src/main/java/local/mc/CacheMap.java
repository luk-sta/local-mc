package local.mc;

import java.util.LinkedHashMap;
import java.util.Map;

class CacheMap extends LinkedHashMap<String, CacheEntry> {

    CacheMap() {
        super(1000, 0.7f, true);
    }

    @Override
    protected boolean removeEldestEntry(Map.Entry<String, CacheEntry> entry) {
        // TODO
        return false;
    }
}
