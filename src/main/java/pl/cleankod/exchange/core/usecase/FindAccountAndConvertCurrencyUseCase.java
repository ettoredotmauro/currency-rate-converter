package pl.cleankod.exchange.core.usecase;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.cleankod.exchange.core.domain.Account;
import pl.cleankod.exchange.core.domain.Money;
import pl.cleankod.exchange.core.gateway.AccountRepository;
import pl.cleankod.exchange.core.gateway.CurrencyConversionService;

import java.util.Currency;
import java.util.Optional;

public class FindAccountAndConvertCurrencyUseCase {

    private final AccountRepository accountRepository;
    private final CurrencyConversionService currencyConversionService;
    private final Currency baseCurrency;

    private static final Logger logger = LoggerFactory.getLogger(FindAccountAndConvertCurrencyUseCase.class);

    public FindAccountAndConvertCurrencyUseCase(AccountRepository accountRepository,
                                                CurrencyConversionService currencyConversionService,
                                                Currency baseCurrency) {
        this.accountRepository = accountRepository;
        this.currencyConversionService = currencyConversionService;
        this.baseCurrency = baseCurrency;
    }

    public Optional<Account> execute(Account.Id id, Currency targetCurrency, String traceId) {
        logger.info("{} - Executing find account by ID {} and converting to currency {}", traceId, id, targetCurrency);
        return accountRepository.find(id)
                .map(account -> new Account(account.id(), account.number(), convert(account.balance(), targetCurrency, traceId)));
    }

    public Optional<Account> execute(Account.Number number, Currency targetCurrency, String traceId) {
        logger.info("{} - Executing find account by number {} and converting to currency {}", traceId, number, targetCurrency);
        return accountRepository.find(number)
                .map(account -> new Account(account.id(), account.number(), convert(account.balance(), targetCurrency, traceId)));
    }

    private Money convert(Money money, Currency targetCurrency, String traceId) {
        if (!baseCurrency.equals(targetCurrency)) {
            logger.debug("{} - Calling service to convert money {} to target currency {}", traceId, money, targetCurrency);
            return money.convert(currencyConversionService, targetCurrency, traceId);
        }

        if (!money.currency().equals(targetCurrency)) {
            logger.error("{} - Cannot convert money between the same currency {}", traceId, targetCurrency);
            throw new CurrencyConversionException(money.currency(), targetCurrency);
        }

        return money;
    }
}
