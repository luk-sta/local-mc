package local.mc;

class CacheEntry {
    private Object value;
    private long ttl;
    private long lastAccess;

    CacheEntry(Object value) {
        this.value = value;
        this.ttl = 0;
        this.lastAccess = System.currentTimeMillis();
    }

    boolean isLive() {
        return System.currentTimeMillis() - lastAccess <= ttl;
    }
}
