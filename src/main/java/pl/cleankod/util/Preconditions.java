package pl.cleankod.util;

import java.math.BigDecimal;


public interface Preconditions {
    static <T> T requireNonNull(T obj) {
        if (obj == null) {
            throw new NullPointerException("Given value cannot be null");
        }
        return obj;
    }

    static void requireNonZero(BigDecimal value) {
        if (value.compareTo(BigDecimal.ZERO) == 0) {
            throw new IllegalArgumentException("Given value cannot be zero");
        }
    }
}
