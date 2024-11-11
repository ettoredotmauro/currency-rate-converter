package pl.cleankod.util;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ExchangeRateCache {

    private final Map<String, CachedData> exchangeRateCache = new ConcurrentHashMap<>();
    private final Long cacheRefresh;

    public ExchangeRateCache(Long cacheRefresh) {
        this.cacheRefresh = cacheRefresh;
    }

    public BigDecimal getRate(String currencyCode) {
        CachedData cachedData = exchangeRateCache.get(currencyCode);
        if (cachedData != null && Instant.now().isBefore(cachedData.fetchedTime.plusSeconds(cacheRefresh))) {
            return cachedData.rate;
        }
        return null;
    }

    public void putRate(String currencyCode, BigDecimal rate) {
        exchangeRateCache.put(currencyCode, new CachedData(rate, Instant.now()));
    }

    private record CachedData(BigDecimal rate, Instant fetchedTime) {}
}
