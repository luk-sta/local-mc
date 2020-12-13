package local.mc;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

import static java.util.Comparator.comparingLong;

public class Cleaner extends Thread {
    private final AtomicReference<Map<String, CacheEntry>> oldMapRef;
    private final AtomicReference<Map<String, CacheEntry>> currentMapRef;
    private final long maxSize;
    private final AtomicLong totalSize;
    private final AtomicBoolean cleanRunning;

    public Cleaner(AtomicReference<Map<String, CacheEntry>> oldMapRef,
            AtomicReference<Map<String, CacheEntry>> currentMapRef,
            long maxSize, AtomicLong totalSize, AtomicBoolean cleanRunning) {
        this.oldMapRef = oldMapRef;
        this.currentMapRef = currentMapRef;
        this.maxSize = (long) (maxSize * 0.8);
        this.totalSize = totalSize;
        this.cleanRunning = cleanRunning;
    }

    @Override
    public void run() {
        try {
            runInternal();
        } finally {
            cleanRunning.set(false);
        }
    }

    private void runInternal() {
        Map<String, CacheEntry> oldMap = currentMapRef.get();
        Map<String, CacheEntry> currentMap = new HashMap<>();
        oldMapRef.set(oldMap);
        currentMapRef.set(currentMap);

        removeInvalidEntries(oldMap);
        if (totalSize.get() > maxSize) {
            removeLastAccessedEntries(oldMap);
        }
        copy(oldMap, currentMap);

    }

    private void copy(Map<String, CacheEntry> oldMap, Map<String, CacheEntry> currentMap) {
        Iterator<Map.Entry<String, CacheEntry>> oldMapIterator = oldMap.entrySet().iterator();
        while (oldMapIterator.hasNext()) {
            if (isInterrupted()) {
                return;
            }
            Map.Entry<String, CacheEntry> next = oldMapIterator.next();
            String key = next.getKey();
            CacheEntry value = next.getValue();
            long addSize = -1 * CacheEntry.getSize(key, value);
            if (value.isInvalid() || currentMap.containsKey(key)) {
                totalSize.addAndGet(addSize);
            } else {
                if (currentMap.putIfAbsent(key, value) != null) {
                    totalSize.addAndGet(addSize);
                }
            }
            oldMapIterator.remove();
        }
    }

    private void removeLastAccessedEntries(Map<String, CacheEntry> oldMap) {
        List<Map.Entry<String, CacheEntry>> entryList = new ArrayList<>(oldMap.entrySet());
        if (isInterrupted()) {
            return;
        }
        entryList.sort(comparingLong(e -> e.getValue().getLastAccessed().get()));
        Iterator<Map.Entry<String, CacheEntry>> entryIterator = entryList.iterator();
        while (totalSize.get() > maxSize) {
            if (isInterrupted()) {
                return;
            }
            Map.Entry<String, CacheEntry> next = entryIterator.next();
            String key = next.getKey();
            CacheEntry value = next.getValue();
            oldMap.remove(key);
            entryIterator.remove();
            long addSize = -1 * CacheEntry.getSize(key, value);
            totalSize.addAndGet(addSize);
        }
    }

    private void removeInvalidEntries(Map<String, CacheEntry> oldMap) {
        Iterator<Map.Entry<String, CacheEntry>> oldMapIterator = oldMap.entrySet().iterator();
        while (oldMapIterator.hasNext()) {
            if (isInterrupted()) {
                return;
            }
            Map.Entry<String, CacheEntry> next = oldMapIterator.next();
            if (next.getValue().isInvalid()) {
                oldMapIterator.remove();
                long addedSize = -1 * CacheEntry.getSize(next.getKey(), next.getValue());
                totalSize.addAndGet(addedSize);
            }
        }
    }
}
