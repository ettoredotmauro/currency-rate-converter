package pl.cleankod.util

import spock.lang.Specification

import java.math.RoundingMode

class CurrencyConversionsSpecification extends Specification{

    def "convert should throw NullPointerException when rate is null"() {

        given:
        BigDecimal amount = new BigDecimal("10.00")
        BigDecimal rate = null
        RoundingMode roundingMode = RoundingMode.HALF_EVEN

        when:
        CurrencyConversions.convert(amount, rate, roundingMode)

        then:
        def e = thrown(NullPointerException)
        e.message == "Given value cannot be null"
    }

    def "convert should throw IllegalArgumentException when rate is zero"() {

        given:
        BigDecimal amount = new BigDecimal("10.00")
        BigDecimal rate = BigDecimal.ZERO
        RoundingMode roundingMode = RoundingMode.HALF_EVEN

        when:
        CurrencyConversions.convert(amount, rate, roundingMode)

        then:
        def e = thrown(IllegalArgumentException)
        e.message == "Given value cannot be zero"
    }

    def "convert should return correct result for valid rate and rounding mode"() {
        given:
        BigDecimal amount = new BigDecimal("10.00")
        BigDecimal rate = new BigDecimal("1.23")
        RoundingMode roundingMode = RoundingMode.DOWN

        when:
        BigDecimal result = CurrencyConversions.convert(amount, rate, roundingMode)

        then:
        result == amount.divide(rate, 2, roundingMode)
    }
}
