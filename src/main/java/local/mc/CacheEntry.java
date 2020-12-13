package local.mc;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import javax.annotation.Nullable;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.concurrent.atomic.AtomicLong;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.InflaterInputStream;

@Getter
@Slf4j
final class CacheEntry {
    private static final long ENTRY_ADDITIONAL_BYTES = 3 * 8;
    private byte[] itemBytes;
    private final AtomicLong eolTime;
    private long cas;
    private final AtomicLong lastAccessed;

    CacheEntry(Serializable item, long eolTime, long cas) throws IOException {
        this.itemBytes = serialize(item);
        this.eolTime = new AtomicLong(eolTime);
        this.cas = cas;
        this.lastAccessed = new AtomicLong(System.currentTimeMillis());
    }

    private byte[] serialize(Serializable item) throws IOException {
        try (ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
             DeflaterOutputStream deflaterOutputStream = new DeflaterOutputStream(byteArrayOutputStream);
             BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(deflaterOutputStream);
             ObjectOutputStream out = new ObjectOutputStream(bufferedOutputStream)) {
            out.writeObject(item);
            out.flush();
            return byteArrayOutputStream.toByteArray();
        }
    }

    long getItemSize() {
        return itemBytes.length;
    }

    static long getSize(String key, @Nullable CacheEntry cacheEntry) {
        if (cacheEntry == null) {
            return 0;
        }
        return cacheEntry.getItemSize() + ENTRY_ADDITIONAL_BYTES + key.getBytes().length;
    }

    Serializable getItem() {
        try (ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(itemBytes);
             BufferedInputStream bufferedInputStream = new BufferedInputStream(byteArrayInputStream);
             InflaterInputStream inflaterInputStream = new InflaterInputStream(bufferedInputStream);
             ObjectInputStream ois = new ObjectInputStream(inflaterInputStream)) {
            return (Serializable) ois.readObject();
        } catch (IOException | ClassNotFoundException e) {
            log.error(e.getMessage(), e);
        }
        return null;
    }


    boolean isInvalid() {
        return System.currentTimeMillis() <= eolTime.get();
    }

    void refreshLastAccessed() {
        lastAccessed.set(System.currentTimeMillis());
    }
}
