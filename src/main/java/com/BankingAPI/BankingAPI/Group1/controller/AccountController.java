package com.BankingAPI.BankingAPI.Group1.controller;

import com.BankingAPI.BankingAPI.Group1.exception.InvalidLimitException;
import com.BankingAPI.BankingAPI.Group1.exception.RestResponseEntityExceptionHandler;
import com.BankingAPI.BankingAPI.Group1.model.dto.AccountGETPOSTResponseDTO;
import com.BankingAPI.BankingAPI.Group1.model.dto.TransferMoneyPOSTResponse;
import com.BankingAPI.BankingAPI.Group1.model.dto.UserDetailsDTO;
import com.BankingAPI.BankingAPI.Group1.service.AccountService;
import com.BankingAPI.BankingAPI.Group1.service.TransactionService;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(value = "/accounts")
public class AccountController extends RestResponseEntityExceptionHandler {
    private final AccountService accountService;
    private final TransactionService transactionService;

    public AccountController(AccountService accountService, TransactionService transactionService) {

        this.accountService = accountService;
        this.transactionService = transactionService;
    }

    @GetMapping("/{userId}/details")
    @PreAuthorize("hasAnyRole('CUSTOMER')")
    public ResponseEntity<Object> getAccountDetails(@PathVariable Long userId) {
        try {
            UserDetailsDTO userDetails = accountService.getAccountDetails(userId);
            return ResponseEntity.ok(userDetails);
        } catch (Exception e) {
            return ResponseEntity.status(404).body(e.getMessage());
        }
    }

    @PostMapping("/own/transfers")
    @PreAuthorize("hasAnyRole('CUSTOMER')")
    public ResponseEntity<Object> transferMoneyToOwnAccount(@RequestBody TransferMoneyPOSTResponse transactionDTO) {
        try {
            transactionService.transferMoneyToOwnAccount(transactionDTO);
            return ResponseEntity.status(HttpStatus.CREATED).body("Transfer successful");
        } catch (Exception e) {
            if (e.getMessage().contains("not found")) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
            } else if (e.getMessage().contains("Insufficient funds") || e.getMessage().contains("Transaction exceeds daily limit")) {
                return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(e.getMessage());
            } else {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
            }
        }
    }


    @PutMapping("/customers/{IBAN}")
    @PreAuthorize("hasAnyRole('EMPLOYEE')")
    public ResponseEntity<Object> updateAccount(@PathVariable String IBAN, @RequestBody AccountGETPOSTResponseDTO account) {
        try {
            accountService.updateAccount(IBAN, account);
            return ResponseEntity.status(HttpStatus.OK).body(new Object[0]);
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }catch(InvalidLimitException e) {
            return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(e.getMessage());
        }catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @PreAuthorize("hasRole('EMPLOYEE')")
    @GetMapping(value = "/customers")
    public ResponseEntity<Object> getAllCustomerAccounts() throws Exception {
            return ResponseEntity.status(HttpStatus.OK).body(accountService.getAllCustomerAccounts());
    }

    @GetMapping("/byAbsoluteLimit")
    @PreAuthorize("hasAnyRole('EMPLOYEE')")
    public ResponseEntity<Object> getAccountsByAbsoluteLimit(@RequestParam(required = true) double absoluteLimit) throws Exception {
            return ResponseEntity.status(HttpStatus.OK).body(accountService.findByAbsoluteLimit(absoluteLimit));
    }

    @GetMapping("/inactive")
    @PreAuthorize("hasAnyRole('EMPLOYEE')")
    public ResponseEntity getInactiveAccounts() throws Exception {
            return ResponseEntity.status(HttpStatus.OK).body(accountService.findByInactiveTag());

    }
}
