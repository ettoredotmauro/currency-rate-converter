package pl.cleankod.exchange.provider.nbp;

public class CurrencyConversionServiceException extends RuntimeException {
  public CurrencyConversionServiceException(String message) {
    super(message);
  }

  public CurrencyConversionServiceException(String message, Throwable cause) {
    super(message, cause);
  }
}
