package pl.cleankod.exchange.provider;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
    private final Long failureTimeout;
    private final Integer failureThreshold;

    private CircuitState circuitState = CircuitState.CLOSED;
    private int failureCount = 0;
    private Instant lastFailureTime = Instant.now();

    private static final Logger logger = LoggerFactory.getLogger(CurrencyConversionNbpService.class);

    public CurrencyConversionNbpService(ExchangeRatesNbpClient exchangeRatesNbpClient, Long cacheRefresh, Long failureTimeout, Integer failureThreshold) {
        this.exchangeRatesNbpClient = exchangeRatesNbpClient;
        this.cacheRefresh = cacheRefresh;
        this.failureTimeout = failureTimeout;
        this.failureThreshold = failureThreshold;

        logger.info("CurrencyConversionNbpService initialized with cacheRefresh: {}, failureTimeout: {}, failureThreshold: {}",
                cacheRefresh, failureTimeout, failureThreshold);
    }

    @Override
    public Money convert(Money money, Currency targetCurrency, String traceId) {
        logger.info("{} - Converting money {} to targetCurrency {}", traceId, money, targetCurrency);

        if (money == null || targetCurrency == null) {
            logger.error("{} - Conversion failed: Money or target currency are null", traceId);
            throw new CurrencyConversionServiceException("Money and target currency must not be null");
        }

        if (circuitState == CircuitState.OPEN) {
            if (Instant.now().isAfter(lastFailureTime.plusMillis(failureTimeout))) {
                circuitState = CircuitState.HALF_OPEN;
                logger.debug("{} - Attempting to reconnect to the service", traceId);
            } else {
                logger.error("{} - Service is unavailable", traceId);
                throw new CurrencyConversionServiceException("Service is unavailable");
            }
        }

        try {
            CachedData cachedData = exchangeRateCache.get(targetCurrency.getCurrencyCode());
            BigDecimal midRate;

            if (cachedData == null || Instant.now().isAfter(cachedData.fetchedTime.plusSeconds(cacheRefresh))) {
                logger.debug("{} - Retrieving new exchange rate for currency {}", traceId, targetCurrency.getCurrencyCode());
                RateWrapper rateWrapper = exchangeRatesNbpClient.fetch("A", targetCurrency.getCurrencyCode());
                if (rateWrapper == null || rateWrapper.rates().isEmpty()) {
                    logger.error("{} - No exchange rate available for currency {}", traceId, targetCurrency.getCurrencyCode());
                    throw new CurrencyConversionServiceException("No exchange rate available for currency: " + targetCurrency.getCurrencyCode());
                }
                midRate = rateWrapper.rates().get(0).mid();
                exchangeRateCache.put(targetCurrency.getCurrencyCode(), new CachedData(midRate, Instant.now()));
                logger.info("{} - Retrieved new exchange rate {} for currency {}", traceId, midRate, targetCurrency.getCurrencyCode());

                failureCount = 0;
            } else {
                midRate = cachedData.rate;
                logger.info("{} - Using cached exchange rate {} for currency {}", traceId, midRate, targetCurrency.getCurrencyCode());
            }

            BigDecimal convertedAmount = CurrencyConversions.convert(money.amount(), midRate, RoundingMode.HALF_DOWN);
            logger.info("{} - Converted amount {}", traceId, convertedAmount);

            return new Money(convertedAmount, targetCurrency);
        } catch (Exception ex) {
            failureCount++;
            lastFailureTime = Instant.now();
            logger.error("{} - Currency conversion failed: {}", traceId, ex.getMessage(), ex);

            if (failureCount >= failureThreshold) {
                circuitState = CircuitState.OPEN;
                logger.warn("{} - Too many failures while connecting [{} failures]: service unavailable)", traceId, failureCount);
            }

            throw new CurrencyConversionServiceException("Failed to convert currency: " + ex.getMessage(), ex);
        }
    }

    private record CachedData(BigDecimal rate, Instant fetchedTime) {}

    private enum CircuitState {
        CLOSED,
        OPEN,
        HALF_OPEN
    }
}
