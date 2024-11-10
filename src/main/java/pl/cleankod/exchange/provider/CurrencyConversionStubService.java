package pl.cleankod.exchange.provider;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.cleankod.exchange.core.domain.Money;
import pl.cleankod.exchange.core.gateway.CurrencyConversionService;
import pl.cleankod.util.CurrencyConversions;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Currency;

public class CurrencyConversionStubService implements CurrencyConversionService {
    private static final BigDecimal PLN_TO_EUR_RATE = BigDecimal.valueOf(0.22d);
    private static final BigDecimal EUR_TO_PLN_RATE = BigDecimal.valueOf(4.58d);

    private static final Logger logger = LoggerFactory.getLogger(CurrencyConversionStubService.class);

    @Override
    public Money convert(Money money, Currency targetCurrency, String traceId) {
        logger.info("{} - Converting money {} to targetCurrency: {}", traceId, money, targetCurrency.getCurrencyCode());
        return money.currency().equals(targetCurrency)
                ? money
                : calculate(money, targetCurrency, traceId);
    }

    private Money calculate(Money money, Currency targetCurrency, String traceId) {
        logger.debug("{} - Calculating money {} to targetCurrency: {}", traceId, money, targetCurrency.getCurrencyCode());
        BigDecimal rate = "PLN".equals(targetCurrency.getCurrencyCode()) ? EUR_TO_PLN_RATE : PLN_TO_EUR_RATE;
        return Money.of(CurrencyConversions.convert(money.amount(), rate, RoundingMode.HALF_DOWN), targetCurrency);
    }
}
