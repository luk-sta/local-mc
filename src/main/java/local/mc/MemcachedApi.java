package local.mc;

import java.io.IOException;
import java.io.Serializable;

public interface MemcachedApi {
    Serializable get(String key);

    boolean set(String key, int expirationSeconds, Serializable o) throws IOException;

    void flushAll() throws InterruptedException;
}
