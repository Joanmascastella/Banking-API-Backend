package com.BankingAPI.BankingAPI.Group1.controller;

import com.BankingAPI.BankingAPI.Group1.model.Account;
import com.BankingAPI.BankingAPI.Group1.model.dto.AccountGETPOSTResponseDTO;
import com.BankingAPI.BankingAPI.Group1.model.dto.TransactionGETPOSTResponseDTO;
import com.BankingAPI.BankingAPI.Group1.model.dto.UserDetailsDTO;
import com.BankingAPI.BankingAPI.Group1.service.AccountService;
import com.BankingAPI.BankingAPI.Group1.service.TransactionService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.annotation.*;

import javax.naming.AuthenticationException;
import java.util.Collections;
import java.util.List;

@RestController
@RequestMapping(value = "/accounts")
public class AccountController {
    private final AccountService accountService;
    private final TransactionService transactionService;

    public AccountController(AccountService accountService, TransactionService transactionService) {
        this.accountService = accountService;
        this.transactionService = transactionService;
    }

    @PreAuthorize("hasRole('EMPLOYEE')")
    @GetMapping(value = "/customers")
    public ResponseEntity<Object> getAllCustomerAccounts() {
        try{
        return ResponseEntity.status(200).body(accountService.getAllCustomerAccounts());
        }
        catch (Exception exception) {
            if (exception instanceof BadCredentialsException){
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }
            else if (exception instanceof AuthenticationException) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
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
    public ResponseEntity<Object> transferMoneyToOwnAccount(@RequestBody TransactionGETPOSTResponseDTO transactionDTO) {
        try {
            Object result = transactionService.transferMoneyToOwnAccount(transactionDTO);
            return ResponseEntity.status(HttpStatus.CREATED).body(result);
        } catch (Exception e) {
            if (e.getMessage().contains("not found")) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
            } else if (e.getMessage().contains("Insufficient funds") || e.getMessage().contains("exceeds daily limit")) {
                return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(e.getMessage());
            } else {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
            }

        }
    }

    @PutMapping("/customers")
    @PreAuthorize("hasAnyRole('EMPLOYEE')")
    public ResponseEntity<Object> updateAccount(@RequestBody AccountGETPOSTResponseDTO account) {
        try{
            accountService.updateAccount(account);
            return ResponseEntity.status(200).body("Account was updated successfully");
        } catch (Exception e) {
            return ResponseEntity.status(404).body(e.getMessage());
        }
    }

    @DeleteMapping("/{accountId}")
    @PreAuthorize("hasAnyRole('EMPLOYEE')")
    public ResponseEntity<Object> closeAccount(@PathVariable long accountId) {
        try{
            accountService.closeAccount(accountId);
            return ResponseEntity.status(200).body("Account was closed successfully");
        } catch (Exception e) {
            return ResponseEntity.status(404).body(e.getMessage());
        }
    }

    @GetMapping("/savings/{userId}")
    @PreAuthorize("hasAnyRole('EMPLOYEE')")
    public ResponseEntity getSavingsAccountsOfUser(@PathVariable long userId) {
        try {
            List<Object> accounts = Collections.singletonList(accountService.findSavingsAccountsByUserId(userId));
            return ResponseEntity.status(HttpStatus.OK).body(accounts);
        }
        catch (Exception exception) {
            if (exception instanceof BadCredentialsException) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            } else if (exception instanceof AuthenticationException) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            } else if (exception instanceof IllegalArgumentException) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
            }
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    @GetMapping("/checking/{userId}")
    @PreAuthorize("hasAnyRole('EMPLOYEE')")
    public ResponseEntity getCheckingAccountsOfUser(@PathVariable long userId) {
        try {
            List<Object> accounts = Collections.singletonList(accountService.findCheckingAccountsByUserId(userId));
            return ResponseEntity.status(HttpStatus.OK).body(accounts);
        }
         catch (Exception exception) {
                if (exception instanceof BadCredentialsException) {
                    return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
                } else if (exception instanceof AuthenticationException) {
                    return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
                } else if (exception instanceof IllegalArgumentException) {
                    return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
                }
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
            }
    }

    @GetMapping("/byAbsoluteLimit")
    @PreAuthorize("hasAnyRole('EMPLOYEE')")
    public ResponseEntity getAccountsByAbsoluteLimit(@RequestParam(required = true) double absoluteLimit) {
        try {
            List<Object> accounts = Collections.singletonList(accountService.findByAbsoluteLimit(absoluteLimit));
            return ResponseEntity.status(HttpStatus.OK).body(accounts);
        }
        catch (Exception exception) {
            if (exception instanceof BadCredentialsException){
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }
            else if (exception instanceof AuthenticationException) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    @GetMapping("/inactive")
    @PreAuthorize("hasAnyRole('EMPLOYEE')")
    public ResponseEntity getInactiveAccounts() {
        try {
            List<Object> accounts = Collections.singletonList(accountService.findByInactiveTag());
            return ResponseEntity.status(HttpStatus.OK).body(accounts);
        }
        catch (Exception exception) {
            if (exception instanceof BadCredentialsException){
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }
            else if (exception instanceof AuthenticationException) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

}
