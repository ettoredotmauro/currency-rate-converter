package pl.cleankod.exchange.core.usecase;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.cleankod.exchange.core.domain.Account;
import pl.cleankod.exchange.core.gateway.AccountRepository;

import java.util.Optional;

public class FindAccountUseCase {
    private final AccountRepository accountRepository;

    private static final Logger logger = LoggerFactory.getLogger(FindAccountUseCase.class);


    public FindAccountUseCase(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    public Optional<Account> execute(Account.Id id, String traceId) {
        logger.info("{} - Executing find account by ID {}", traceId, id);
        return accountRepository.find(id);
    }

    public Optional<Account> execute(Account.Number number, String traceId) {
        logger.info("{} - Executing find account by number {}", traceId, number);
        return accountRepository.find(number);
    }
}
