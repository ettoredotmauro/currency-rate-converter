package pl.cleankod.exchange.provider;

import pl.cleankod.exchange.core.domain.Money;
import pl.cleankod.exchange.core.gateway.CurrencyConversionService;
import pl.cleankod.exchange.provider.nbp.CurrencyConversionServiceException;
import pl.cleankod.exchange.provider.nbp.ExchangeRatesNbpClient;
import pl.cleankod.exchange.provider.nbp.model.RateWrapper;
import pl.cleankod.util.CurrencyConversions;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.Currency;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class CurrencyConversionNbpService implements CurrencyConversionService {
    private final ExchangeRatesNbpClient exchangeRatesNbpClient;
    private final Map<String, CachedData> exchangeRateCache = new ConcurrentHashMap<>();
    private final Long cacheRefresh;

    public CurrencyConversionNbpService(ExchangeRatesNbpClient exchangeRatesNbpClient, Long cacheRefresh) {
        this.exchangeRatesNbpClient = exchangeRatesNbpClient;
        this.cacheRefresh = cacheRefresh;
    }

    @Override
    public Money convert(Money money, Currency targetCurrency) {
        if (money == null || targetCurrency == null) {
            throw new CurrencyConversionServiceException("Money and target currency must not be null");
        }

        try {
            CachedData cachedData = exchangeRateCache.get(targetCurrency.getCurrencyCode());
            BigDecimal midRate;

            // If the cached rate is null or is more then 10 minutes old, then fetch it
            if (cachedData == null || Instant.now().isAfter(cachedData.fetchedTime.plusSeconds(cacheRefresh))) {
                RateWrapper rateWrapper = exchangeRatesNbpClient.fetch("A", targetCurrency.getCurrencyCode());
                if (rateWrapper == null || rateWrapper.rates().isEmpty()) {
                    throw new CurrencyConversionServiceException("No exchange rate available for currency: " + targetCurrency.getCurrencyCode());
                }
                midRate = rateWrapper.rates().get(0).mid();
                exchangeRateCache.put(targetCurrency.getCurrencyCode(), new CachedData(midRate, Instant.now()));
            } else {
                midRate = cachedData.rate;
            }

            BigDecimal calculatedRate = CurrencyConversions.convert(money.amount(), midRate, RoundingMode.HALF_DOWN);
            return new Money(calculatedRate, targetCurrency);
        } catch (Exception ex) {
            throw new CurrencyConversionServiceException("Failed to convert currency: " + ex.getMessage(), ex);
        }
    }

    private record CachedData(BigDecimal rate, Instant fetchedTime) {}
}
