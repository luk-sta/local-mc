package local.mc;

import com.volkhart.memory.MemoryMeasurer;

import javax.annotation.Nullable;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

class CacheMap extends LinkedHashMap<String, CacheEntry> {
    private static final long ENTRY_ADDITIONAL_BYTES = 3 * 8;
    private final long maxSize;
    private AtomicLong byteSize = new AtomicLong();

    CacheMap(long maxSize) {
        super(1000, 0.7f);
        this.maxSize = maxSize;
    }

    @Override
    public CacheEntry put(String key, CacheEntry value) {
        CacheEntry old = super.put(key, value);
        long addedSize = getSize(key, value) - getSize(key, old);
        long newTotalSize = byteSize.addAndGet(addedSize);

        Iterator<Map.Entry<String, CacheEntry>> iterator = entrySet().iterator();
        while (newTotalSize > maxSize && iterator.hasNext()) {
            Map.Entry<String, CacheEntry> next = iterator.next();
            CacheEntry cacheEntry = next.getValue();
            if (!cacheEntry.isLive()) {
                iterator.remove();
                newTotalSize = byteSize.addAndGet(-1 * getSize(next.getKey(), cacheEntry));
            }
        }
        if (newTotalSize > maxSize) {
            //TODO
        }
        return old;
    }

    private long getSize(String key, @Nullable CacheEntry cacheEntry) {
        if (cacheEntry == null) {
            return 0;
        }
        return cacheEntry.getItemSize() + ENTRY_ADDITIONAL_BYTES + key.getBytes().length;
    }

//    @Override
//    protected boolean removeEldestEntry(Map.Entry<String, CacheEntry> entry) {
//        // TODO
//        if (!entry.getValue().isLive()) {
//            return true;
//        }
//        if (MemoryMeasurer.measureBytes(this) > maxSize) {
//            return true;
//        }
//        return false;
//    }
}
