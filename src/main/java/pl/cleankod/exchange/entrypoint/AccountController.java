package pl.cleankod.exchange.entrypoint;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pl.cleankod.exchange.core.domain.Account;
import pl.cleankod.exchange.core.dto.AccountDto;
import pl.cleankod.exchange.service.AccountService;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Currency;
import java.util.UUID;

@RestController
@RequestMapping("/accounts")
public class AccountController {

    private final AccountService accountService;

    public AccountController(AccountService accountService) {
        this.accountService = accountService;
    }

    @Operation(summary = "Find an account by ID",
            description = "Retrieves an account based on the provided account ID.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Account found"),
                    @ApiResponse(responseCode = "404", description = "Account not found")
            })
    @GetMapping(path = "/{id}")
    public ResponseEntity<AccountDto> findAccountById(@PathVariable String id, @RequestParam(required = false) String currency) {
        String traceId = UUID.randomUUID().toString();
        Currency currencyObj = currency != null ? Currency.getInstance(currency) : null;
        return accountService.findAccountById(Account.Id.of(id), currencyObj, traceId)
                .map(Account::toDto)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @Operation(summary = "Find an account by number",
            description = "Retrieves an account based on the provided account number.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Account found"),
                    @ApiResponse(responseCode = "404", description = "Account not found")
            })
    @GetMapping(path = "/number={number}")
    public ResponseEntity<AccountDto> findAccountByNumber(@PathVariable String number, @RequestParam(required = false) String currency) {
        String traceId = UUID.randomUUID().toString();
        Account.Number accountNumber = Account.Number.of(URLDecoder.decode(number, StandardCharsets.UTF_8));
        Currency currencyObj = currency != null ? Currency.getInstance(currency) : null;
        return accountService.findAccountByNumber(accountNumber, currencyObj, traceId)
                .map(Account::toDto)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }


}
