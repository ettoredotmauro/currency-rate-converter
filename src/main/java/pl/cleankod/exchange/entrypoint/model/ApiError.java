package pl.cleankod.exchange.entrypoint.model;

public record ApiError(String message, int status, String error) {

}
