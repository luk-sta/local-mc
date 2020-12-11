package local.mc;

import lombok.extern.slf4j.Slf4j;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.InflaterInputStream;

@Slf4j
final class CacheEntry {
    private byte[] itemBytes;
    private long eolTime;
    private long cas;


    CacheEntry(Serializable item, long eolTime, long cas) throws IOException {
        this.itemBytes = serialize(item);
        this.eolTime = eolTime;
        this.cas = cas;
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

    boolean isLive() {
        return System.currentTimeMillis() > eolTime;
    }

    boolean isInvalid() {
        return System.currentTimeMillis() <= eolTime;
    }
}
