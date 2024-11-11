package pl.cleankod.exchange.core.dto;

public record MoneyDto(String amount, String currency) {

    public static MoneyDto of(String amount, String currency) {
        return new MoneyDto(amount, currency);
    }
}