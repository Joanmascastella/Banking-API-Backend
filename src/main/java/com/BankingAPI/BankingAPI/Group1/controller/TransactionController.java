package com.BankingAPI.BankingAPI.Group1.controller;

import com.BankingAPI.BankingAPI.Group1.model.Transaction;
import com.BankingAPI.BankingAPI.Group1.model.dto.TransactionGETPOSTResponseDTO;
import com.BankingAPI.BankingAPI.Group1.model.dto.TransferMoneyPOSTResponse;
import com.BankingAPI.BankingAPI.Group1.service.TransactionService;
import org.apache.coyote.BadRequestException;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.annotation.*;

import javax.naming.AuthenticationException;
import java.time.LocalDate;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping(value = "/transactions")
public class TransactionController {


    private final TransactionService transactionService;

    public TransactionController(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @PostMapping("/transfers")
    @PreAuthorize("hasAnyRole('CUSTOMER', 'EMPLOYEE')")
    public ResponseEntity<Object> transferToOtherCustomer(@RequestBody TransferMoneyPOSTResponse transactionDTO) {
        try {
            TransactionGETPOSTResponseDTO result = transactionService.transferToOtherCustomer(transactionDTO);
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Transfer successful");
            response.put("transaction", result);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception e) {
            String errorMessage = e.getMessage();
            if (errorMessage.contains("not found")) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", errorMessage));
            } else if (errorMessage.contains("Insufficient funds") || errorMessage.contains("exceeds daily limit")) {
                return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(Map.of("message", errorMessage));
            } else if (errorMessage.contains("CHECKING accounts") || errorMessage.contains("Cannot transfer money between savings accounts.")) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message", errorMessage));
            } else if (errorMessage.contains("Both accounts cannot belong to the same user for this operation.")) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message", errorMessage));
            } else {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message", errorMessage));
            }
        }
    }



    @GetMapping("/{userId}/history")
    @PreAuthorize("hasAnyRole('CUSTOMER', 'EMPLOYEE')")
    public ResponseEntity<Object> getTransactionsByUserId(@PathVariable Long userId) {
        try {
            return ResponseEntity.ok(transactionService.getTransactionsByUserId(userId));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error retrieving transactions for user: " + userId);
        }
    }
    @GetMapping("/search")
    @PreAuthorize("hasAnyRole('CUSTOMER', 'EMPLOYEE')")
    public ResponseEntity<List<TransactionGETPOSTResponseDTO>> searchTransactions(
            @RequestParam(required = false) String IBAN,
            @RequestParam(required = false) Double amount,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false) Double amountGreater,
            @RequestParam(required = false) Double amountLess) {

        List<Transaction> transactions = transactionService.filterTransactions(IBAN, amount,amountGreater,amountLess, startDate, endDate);
        List<TransactionGETPOSTResponseDTO> transactionDto = transactions.stream()
                .map(t -> new TransactionGETPOSTResponseDTO(t.getFromAccount(), t.getToAccount(), t.getAmount(), t.getDate(), t.getUser().getId()))
                .collect(Collectors.toList());

        return ResponseEntity.ok(transactionDto);
    }

    @PreAuthorize("hasAnyRole('EMPLOYEE')")
    @GetMapping
    public ResponseEntity<Object> getAllTransactions() {
        try {
            return ResponseEntity.status(200).body(transactionService.allTransactions());
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


    @GetMapping("/byCustomers")
    @PreAuthorize("hasAnyRole('EMPLOYEE')")
    public ResponseEntity getTransactionsInitializedByCustomers() {
        try {
            return ResponseEntity.status(HttpStatus.OK).body(transactionService.findTransactionsInitializedByCustomers());
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

    @GetMapping("/byEmployees")
    @PreAuthorize("hasAnyRole('EMPLOYEE')")
    public ResponseEntity getTransactionsInitializedByEmployees() {
        try {
            return ResponseEntity.status(HttpStatus.OK).body(transactionService.findTransactionsInitializedByEmployees());
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




    @GetMapping("/ATM")
    @PreAuthorize("hasAnyRole('EMPLOYEE')")
    public ResponseEntity getATMTransactions() {
        try {
            return ResponseEntity.status(HttpStatus.OK).body(transactionService.findATMTransactions());}
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



    @GetMapping("/ATM/{userId}")
    @PreAuthorize("hasAnyRole('EMPLOYEE')")
    public ResponseEntity getATMTransactionsByUserId(@PathVariable long userId) {
        try {
            List<Object> transactions = Collections.singletonList(transactionService.findATMTransactionsByUserId(userId));
            return ResponseEntity.status(HttpStatus.OK).body(transactions);
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



    @GetMapping("/ATM/withdrawals/{userId}")
    @PreAuthorize("hasAnyRole('EMPLOYEE')")
    public ResponseEntity getATMWithdrawalsByUserId(@PathVariable long userId) {
        try {
            List<Object> transactions = Collections.singletonList(transactionService.findATMWithdrawalsByUserId(userId));
            return ResponseEntity.status(HttpStatus.OK).body(transactions);
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


    @GetMapping("/ATM/deposits/{userId}")
    @PreAuthorize("hasAnyRole('EMPLOYEE')")
    public ResponseEntity getATMDepositsByUserId(@PathVariable long userId) {
        try {
            List<Object> transactions = Collections.singletonList(transactionService.findATMDepositsByUserId(userId));
            return ResponseEntity.status(HttpStatus.OK).body(transactions);
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

    @GetMapping("/online")
    @PreAuthorize("hasAnyRole('EMPLOYEE')")
    public ResponseEntity getOnlineTransactions() {
        try {
            return ResponseEntity.status(HttpStatus.OK).body(transactionService.findOnlineTransactions());}
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


    @GetMapping("/online/byEmployees")
     @PreAuthorize("hasAnyRole('EMPLOYEE')")
    public ResponseEntity getOnlineTransactionsByEmployees() {
        try {
            return ResponseEntity.status(HttpStatus.OK).body(transactionService.findOnlineTransactionsByEmployees());}
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

    @GetMapping("/online/byCustomers")
    @PreAuthorize("hasAnyRole('EMPLOYEE')")
    public ResponseEntity getOnlineTransactionsByCustomers() {
        try {
            return ResponseEntity.status(HttpStatus.OK).body(transactionService.findOnlineTransactionsByCustomers());}
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

    @GetMapping("/online/{userId}")
    @PreAuthorize("hasAnyRole('EMPLOYEE')")
    public ResponseEntity getOnlineTransactionsByUserId(@PathVariable long userId) {
        try {
            List<Object> transactions = Collections.singletonList(transactionService.findOnlineTransactionsByUserId(userId));
            return ResponseEntity.status(HttpStatus.OK).body(transactions);
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

}
