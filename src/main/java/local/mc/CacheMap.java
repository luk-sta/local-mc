package local.mc;

import com.volkhart.memory.MemoryMeasurer;

import java.util.LinkedHashMap;
import java.util.Map;

class CacheMap extends LinkedHashMap<String, CacheEntry> {
    private final long maxSize;

    CacheMap(long maxSize) {
        super(1000, 0.7f, true);
        this.maxSize = maxSize;
    }

    @Override
    protected boolean removeEldestEntry(Map.Entry<String, CacheEntry> entry) {
        // TODO
        if (!entry.getValue().isLive()) {
            return true;
        }
        if (MemoryMeasurer.measureBytes(this) > maxSize) {
            return true;
        }
        return false;
    }
}
