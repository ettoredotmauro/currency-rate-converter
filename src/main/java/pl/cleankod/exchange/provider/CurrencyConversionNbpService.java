package pl.cleankod.exchange.provider;

import pl.cleankod.exchange.core.domain.Money;
import pl.cleankod.exchange.core.gateway.CurrencyConversionService;
import pl.cleankod.exchange.provider.nbp.CurrencyConversionServiceException;
import pl.cleankod.exchange.provider.nbp.ExchangeRatesNbpClient;
import pl.cleankod.exchange.provider.nbp.model.RateWrapper;
import pl.cleankod.util.CurrencyConversions;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Currency;

public class CurrencyConversionNbpService implements CurrencyConversionService {
    private final ExchangeRatesNbpClient exchangeRatesNbpClient;

    public CurrencyConversionNbpService(ExchangeRatesNbpClient exchangeRatesNbpClient) {
        this.exchangeRatesNbpClient = exchangeRatesNbpClient;
    }

    @Override
    public Money convert(Money money, Currency targetCurrency) {
        if (money == null || targetCurrency == null) {
            throw new CurrencyConversionServiceException("Money and target currency must not be null");
        }

        try {
            // Fetch rate from external service
            RateWrapper rateWrapper = exchangeRatesNbpClient.fetch("A", targetCurrency.getCurrencyCode());
            if (rateWrapper == null || rateWrapper.rates().isEmpty()) {
                throw new CurrencyConversionServiceException("No exchange rate available for currency: " + targetCurrency.getCurrencyCode());
            }

            BigDecimal midRate = rateWrapper.rates().get(0).mid();
            BigDecimal calculatedRate = CurrencyConversions.convert(money.amount(), midRate, RoundingMode.HALF_DOWN);
            return new Money(calculatedRate, targetCurrency);
        } catch (Exception ex) {
            throw new CurrencyConversionServiceException("Failed to convert currency: " + ex.getMessage(), ex);
        }
    }
}
