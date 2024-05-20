package com.BankingAPI.BankingAPI.Group1.controller;

import com.BankingAPI.BankingAPI.Group1.model.dto.AccountGETPOSTResponseDTO;
import com.BankingAPI.BankingAPI.Group1.model.dto.TransferMoneyPOSTResponse;
import com.BankingAPI.BankingAPI.Group1.model.dto.UserDetailsDTO;
import com.BankingAPI.BankingAPI.Group1.service.AccountService;
import com.BankingAPI.BankingAPI.Group1.service.TransactionService;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.annotation.*;

import javax.naming.AuthenticationException;

@RestController
@RequestMapping(value = "/accounts")
public class AccountController {
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
        try {
            accountService.updateAccount(account);
            return ResponseEntity.status(HttpStatus.OK).body(new Object[0]);
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
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
            else {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();}
        }
    }

    @GetMapping("/byAbsoluteLimit")
    @PreAuthorize("hasAnyRole('EMPLOYEE')")
    public ResponseEntity<Object> getAccountsByAbsoluteLimit(@RequestParam(required = true) double absoluteLimit) {
        try {
            return ResponseEntity.status(HttpStatus.OK).body(accountService.findByAbsoluteLimit(absoluteLimit));
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
            return ResponseEntity.status(HttpStatus.OK).body(accountService.findByInactiveTag());
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

    @DeleteMapping("/{userId}")
    @PreAuthorize("hasAnyRole('EMPLOYEE')")
    public ResponseEntity<Object> closeAccount(@PathVariable long userId) {
        try {
            accountService.closeAccount(userId);
            return ResponseEntity.status(HttpStatus.OK).body(new Object[0]);
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }
}
