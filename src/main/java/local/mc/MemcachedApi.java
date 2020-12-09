package local.mc;

public interface MemcachedApi {
    <T> T get(String key);

    void flushAll();
}
