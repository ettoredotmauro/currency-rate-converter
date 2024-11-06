package pl.cleankod.util;

import java.math.BigDecimal;
import java.math.RoundingMode;

public interface CurrencyConversions {
    static BigDecimal convert(BigDecimal amount, BigDecimal rate, RoundingMode roundingMode) {
        Preconditions.requireNonNull(rate);
        Preconditions.requireNonZero(rate);
        return amount.divide(rate, 2, roundingMode);
    }
}