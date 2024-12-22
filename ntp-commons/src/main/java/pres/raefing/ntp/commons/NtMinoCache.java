package pres.raefing.ntp.commons;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@Slf4j
public class NtMinoCache {

    private final Cache<String, Object> minoCache;

    public NtMinoCache() {
        minoCache = Caffeine.newBuilder()
                .maximumSize(65535)
                .expireAfterAccess(300, TimeUnit.SECONDS)
                .removalListener((key, value, removalCause) -> {
                    if (removalCause.wasEvicted()) {
                        log.info("ignore evicted key: {}", key);
                    }
                })
                .build();
    }

    public void put(String id, Object obj) {
        minoCache.put(id, obj);
    }

    public Object get(String id) {
        return minoCache.getIfPresent(id);
    }

    public Object get(String id, long timeout, TimeUnit unit) throws TimeoutException {
        long systemTime = System.currentTimeMillis();
        long waitTime = unit.toMillis(timeout);
        while (true) {
            Object o = get(id);
            if (o != null) {
                return o;
            } else {
                long nowTime = System.currentTimeMillis();
                if (nowTime - systemTime > waitTime) {
                    throw new TimeoutException("receive message timeout");
                }
                try {
                    Thread.sleep(unit.toMillis(timeout) / 50);
                } catch (InterruptedException ignored) {
                }
            }
        }
    }
}
