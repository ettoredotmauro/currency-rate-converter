package pl.cleankod.exchange.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.cleankod.exchange.core.domain.Account;

import pl.cleankod.exchange.core.usecase.FindAccountAndConvertCurrencyUseCase;
import pl.cleankod.exchange.core.usecase.FindAccountUseCase;

import java.util.Currency;
import java.util.Optional;

public class AccountService {

    private final FindAccountAndConvertCurrencyUseCase findAccountAndConvertCurrencyUseCase;
    private final FindAccountUseCase findAccountUseCase;

    private static final Logger logger = LoggerFactory.getLogger(AccountService.class);

    public AccountService(FindAccountAndConvertCurrencyUseCase findAccountAndConvertCurrencyUseCase,
                          FindAccountUseCase findAccountUseCase) {
        this.findAccountAndConvertCurrencyUseCase = findAccountAndConvertCurrencyUseCase;
        this.findAccountUseCase = findAccountUseCase;
    }

    public Optional<Account> findAccountById(Account.Id accountId, Currency currency, String traceId) {
        logger.debug("{} - Finding account by id {}", traceId, accountId);
        if (currency != null) {
            return findAccountAndConvertCurrencyUseCase.execute(accountId, currency, traceId);
        } else {
            return findAccountUseCase.execute(accountId, traceId);
        }
    }

    public Optional<Account> findAccountByNumber(Account.Number accountNumber, Currency currency, String traceId) {
        logger.debug("{} - Finding account by number {}", traceId, accountNumber);
        if (currency != null) {
            return findAccountAndConvertCurrencyUseCase.execute(accountNumber, currency, traceId);
        } else {
            return findAccountUseCase.execute(accountNumber, traceId);
        }
    }
}
