package pl.cleankod.util

import spock.lang.Specification

class PreconditionsRequireNonZeroSpecification extends Specification {

    def "should throw IllegalArgumentException when value is zero"() {
        given:
        BigDecimal value = BigDecimal.ZERO

        when:
        Preconditions.requireNonZero(value)

        then:
        def exception = thrown(IllegalArgumentException)
        exception.message.startsWith("Given value cannot be zero")
    }

    def "should pass without exception when value is non-zero"() {
        given:
        BigDecimal value = new BigDecimal("5.00")

        when:
        Preconditions.requireNonZero(value)

        then:
        noExceptionThrown()
    }
}
