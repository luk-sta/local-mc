package local.mc;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.zip.DeflaterOutputStream;

class CacheEntry {
    private byte[] itemBytes;
    private long ttl;
    private long cas;
    private long lastAccess;

    CacheEntry(Serializable item) throws IOException {
        this.itemBytes = serialize(item);
        this.ttl = 0;
        this.cas = 0;
        this.lastAccess = System.currentTimeMillis();
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

    boolean isLive() {
        return System.currentTimeMillis() - lastAccess <= ttl;
    }
}
