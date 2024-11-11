package pl.cleankod.exchange.provider;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.cleankod.exchange.core.domain.Money;
import pl.cleankod.exchange.core.gateway.CurrencyConversionService;
import pl.cleankod.exchange.provider.nbp.CurrencyConversionServiceException;
import pl.cleankod.exchange.provider.nbp.ExchangeRatesNbpClient;
import pl.cleankod.exchange.provider.nbp.model.RateWrapper;
import pl.cleankod.util.CircuitBreaker;
import pl.cleankod.util.CurrencyConversions;
import pl.cleankod.util.ExchangeRateCache;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Currency;

public class CurrencyConversionNbpService implements CurrencyConversionService {
    private final ExchangeRatesNbpClient exchangeRatesNbpClient;
    private final ExchangeRateCache exchangeRateCache;
    private final CircuitBreaker circuitBreaker;

    private static final Logger logger = LoggerFactory.getLogger(CurrencyConversionNbpService.class);

    public CurrencyConversionNbpService(ExchangeRatesNbpClient exchangeRatesNbpClient, Long cacheRefresh, Long failureTimeout, Integer failureThreshold) {
        this.exchangeRatesNbpClient = exchangeRatesNbpClient;
        this.exchangeRateCache = new ExchangeRateCache(cacheRefresh);
        this.circuitBreaker = new CircuitBreaker(failureTimeout, failureThreshold);

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

        if (!circuitBreaker.isAvailable()) {
            logger.error("{} - Service is unavailable", traceId);
            throw new CurrencyConversionServiceException("Service is unavailable");
        }

        try {
            BigDecimal midRate = exchangeRateCache.getRate(targetCurrency.getCurrencyCode());

            if (midRate == null) {
                logger.debug("{} - Retrieving new exchange rate for currency {}", traceId, targetCurrency.getCurrencyCode());
                RateWrapper rateWrapper = exchangeRatesNbpClient.fetch("A", targetCurrency.getCurrencyCode());
                if (rateWrapper == null || rateWrapper.rates().isEmpty()) {
                    logger.error("{} - No exchange rate available for currency {}", traceId, targetCurrency.getCurrencyCode());
                    throw new CurrencyConversionServiceException("No exchange rate available for currency: " + targetCurrency.getCurrencyCode());
                }
                midRate = rateWrapper.rates().get(0).mid();
                exchangeRateCache.putRate(targetCurrency.getCurrencyCode(), midRate);
                logger.info("{} - Retrieved new exchange rate {} for currency {}", traceId, midRate, targetCurrency.getCurrencyCode());

                circuitBreaker.reset();
            } else {
                logger.info("{} - Using cached exchange rate {} for currency {}", traceId, midRate, targetCurrency.getCurrencyCode());
            }

            BigDecimal convertedAmount = CurrencyConversions.convert(money.amount(), midRate, RoundingMode.HALF_DOWN);
            logger.info("{} - Converted amount {}", traceId, convertedAmount);

            return new Money(convertedAmount, targetCurrency);
        } catch (Exception ex) {
            circuitBreaker.recordFailure();
            logger.error("{} - Currency conversion failed: {}", traceId, ex.getMessage(), ex);
            throw new CurrencyConversionServiceException("Failed to convert currency: " + ex.getMessage(), ex);
        }
    }
}