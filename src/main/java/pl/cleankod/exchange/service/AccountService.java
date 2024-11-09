package pl.cleankod.exchange.service;

import pl.cleankod.exchange.core.domain.Account;

import pl.cleankod.exchange.core.usecase.FindAccountAndConvertCurrencyUseCase;
import pl.cleankod.exchange.core.usecase.FindAccountUseCase;

import java.util.Currency;
import java.util.Optional;

public class AccountService {

    private final FindAccountAndConvertCurrencyUseCase findAccountAndConvertCurrencyUseCase;
    private final FindAccountUseCase findAccountUseCase;

    public AccountService(FindAccountAndConvertCurrencyUseCase findAccountAndConvertCurrencyUseCase,
                          FindAccountUseCase findAccountUseCase) {
        this.findAccountAndConvertCurrencyUseCase = findAccountAndConvertCurrencyUseCase;
        this.findAccountUseCase = findAccountUseCase;
    }

    public Optional<Account> findAccountById(Account.Id accountId, Currency currency) {
        if (currency != null) {
            return findAccountAndConvertCurrencyUseCase.execute(accountId, currency);
        } else {
            return findAccountUseCase.execute(accountId);
        }
    }

    public Optional<Account> findAccountByNumber(Account.Number accountNumber, Currency currency) {
        if (currency != null) {
            return findAccountAndConvertCurrencyUseCase.execute(accountNumber, currency);
        } else {
            return findAccountUseCase.execute(accountNumber);
        }
    }
}
