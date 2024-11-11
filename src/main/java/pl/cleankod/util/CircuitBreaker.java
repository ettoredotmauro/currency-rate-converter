package pl.cleankod.util;

import java.time.Instant;

public class CircuitBreaker {
    private final Long failureTimeout;
    private final Integer failureThreshold;

    private int failureCount = 0;
    private Instant lastFailureTime = Instant.now();
    private CircuitState circuitState = CircuitState.CLOSED;


    public CircuitBreaker(Long failureTimeout, Integer failureThreshold) {
        this.failureTimeout = failureTimeout;
        this.failureThreshold = failureThreshold;
    }

    public boolean isAvailable() {
        if (circuitState == CircuitState.OPEN) {
            if (Instant.now().isAfter(lastFailureTime.plusMillis(failureTimeout))) {
                circuitState = CircuitState.HALF_OPEN;
            } else {
                return false;
            }
        }

        return true;
    }

    public void recordFailure() {
        failureCount++;
        lastFailureTime = Instant.now();
        if (failureCount >= failureThreshold) {
            circuitState = CircuitState.OPEN;
        }
    }

    public void reset() {
        failureCount = 0;
        circuitState = CircuitState.CLOSED;
    }

    private enum CircuitState {
        CLOSED,
        OPEN,
        HALF_OPEN
    }
}
