package com.BankingAPI.BankingAPI.Group1.controller;

import com.BankingAPI.BankingAPI.Group1.exception.RestResponseEntityExceptionHandler;
import com.BankingAPI.BankingAPI.Group1.model.Transaction;
import com.BankingAPI.BankingAPI.Group1.model.dto.TransactionGETPOSTResponseDTO;
import com.BankingAPI.BankingAPI.Group1.model.dto.TransferMoneyPOSTResponse;
import com.BankingAPI.BankingAPI.Group1.service.TransactionService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping(value = "/transactions")
public class TransactionController extends RestResponseEntityExceptionHandler {


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
            } else if (errorMessage.contains("Cannot transfer money between savings accounts.") || errorMessage.contains("Customer can only transfer money to a checking account.")) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message", errorMessage));
            } else if (errorMessage.contains("Both accounts cannot belong to the same user for this operation.") || errorMessage.contains("Customer can only transfer money from their own account.")) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message", errorMessage));
            } else {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message", errorMessage));
            }
        }
    }




    @GetMapping("/{userId}/history")
    @PreAuthorize("hasAnyRole('CUSTOMER')")
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
    public ResponseEntity<Object> getAllTransactions() throws Exception {
        return ResponseEntity.status(HttpStatus.OK).body(transactionService.findAllTransactions());
    }

    @PreAuthorize("hasAnyRole('EMPLOYEE')")
    @GetMapping("/customer/{userId}")
    public ResponseEntity<Object> getTransactionsOfCustomerByEmployee(@PathVariable("userId") long userId) throws Exception {
        return ResponseEntity.status(HttpStatus.OK).body(transactionService.getTransactionsByUserId(userId));
    }

    @GetMapping("/byCustomers")
    @PreAuthorize("hasAnyRole('EMPLOYEE')")
    public ResponseEntity getTransactionsInitializedByCustomers() throws Exception {
        return ResponseEntity.status(HttpStatus.OK).body(transactionService.findTransactionsInitializedByCustomers());
    }

    @GetMapping("/byEmployees")
    @PreAuthorize("hasAnyRole('EMPLOYEE')")
    public ResponseEntity getTransactionsInitializedByEmployees() throws Exception {
        return ResponseEntity.status(HttpStatus.OK).body(transactionService.findTransactionsInitializedByEmployees());
    }

    @GetMapping("/ATM")
    @PreAuthorize("hasAnyRole('EMPLOYEE')")
    public ResponseEntity getATMTransactions() throws Exception {
        return ResponseEntity.status(HttpStatus.OK).body(transactionService.findATMTransactions());
    }

    @GetMapping("/ATM/withdrawals/{userId}")
    @PreAuthorize("hasAnyRole('EMPLOYEE')")
    public ResponseEntity<Object> getATMWithdrawalsByUserId(@PathVariable long userId) throws Exception {
        return ResponseEntity.status(HttpStatus.OK).body(transactionService.findATMWithdrawalsByUserId(userId));
    }

    @GetMapping("/ATM/deposits/{userId}")
    @PreAuthorize("hasAnyRole('EMPLOYEE')")
    public ResponseEntity<Object> getATMDepositsByUserId(@PathVariable long userId) throws Exception {
        return ResponseEntity.status(HttpStatus.OK).body(transactionService.findATMDepositsByUserId(userId));
    }

    @GetMapping("/online")
    @PreAuthorize("hasAnyRole('EMPLOYEE')")
    public ResponseEntity getOnlineTransactions() throws Exception {
        return ResponseEntity.status(HttpStatus.OK).body(transactionService.findOnlineTransactions());
    }

    @GetMapping("/online/byEmployees")
    @PreAuthorize("hasAnyRole('EMPLOYEE')")
    public ResponseEntity getOnlineTransactionsByEmployees() throws Exception {
        return ResponseEntity.status(HttpStatus.OK).body(transactionService.findOnlineTransactionsByEmployees());
    }

    @GetMapping("/online/byCustomers")
    @PreAuthorize("hasAnyRole('EMPLOYEE')")
    public ResponseEntity getOnlineTransactionsByCustomers() throws Exception {
        return ResponseEntity.status(HttpStatus.OK).body(transactionService.findOnlineTransactionsByCustomers());
    }

    @GetMapping("/online/{userId}")
    @PreAuthorize("hasAnyRole('EMPLOYEE')")
    public ResponseEntity<Object> getOnlineTransactionsByUserId(@PathVariable long userId) throws Exception {
        return ResponseEntity.status(HttpStatus.OK).body(transactionService.findOnlineTransactionsByUserId(userId));
    }

}
