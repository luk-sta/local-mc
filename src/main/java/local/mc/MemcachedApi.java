package local.mc;

import java.io.Serializable;

public interface MemcachedApi {
    Serializable get(String key);

    void flushAll();
}
