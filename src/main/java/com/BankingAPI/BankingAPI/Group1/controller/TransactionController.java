package com.BankingAPI.BankingAPI.Group1.controller;

import com.BankingAPI.BankingAPI.Group1.model.dto.TransactionGETPOSTResponseDTO;
import com.BankingAPI.BankingAPI.Group1.service.TransactionService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import com.BankingAPI.BankingAPI.Group1.model.Transaction;
import com.BankingAPI.BankingAPI.Group1.model.dto.TransactionGETPOSTResponseDTO;
import com.BankingAPI.BankingAPI.Group1.service.TransactionService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping(value = "/transactions")
public class TransactionController {


    private final TransactionService transactionService;

    public TransactionController(TransactionService transactionService) {
        this.transactionService = transactionService;
    }


    @GetMapping
    @PreAuthorize("hasAnyRole('EMPLOYEE')")
    public ResponseEntity<Object> getAllTransactions() {
        return ResponseEntity.status(200).body(transactionService.allTransactions());
    }

    @PostMapping("/transfers")
    @PreAuthorize("hasAnyRole('CUSTOMER', 'EMPLOYEE')")
    public ResponseEntity<Object> transferToOtherCustomer(@RequestBody TransactionGETPOSTResponseDTO transactionDTO) {
        try {
            TransactionGETPOSTResponseDTO result = transactionService.transferToOtherCustomer(transactionDTO);
            return ResponseEntity.status(HttpStatus.CREATED).body(result);
        } catch (Exception e) {
            if (e.getMessage().contains("not found")) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
            } else if (e.getMessage().contains("Insufficient funds") || e.getMessage().contains("exceeds daily limit")) {
                return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(e.getMessage());
            } else if (e.getMessage().contains("CHECKING accounts")) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Both accounts must be of type CHECKING.");
            } else {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
            }
        }
    }

    @GetMapping("/{userId}/history")
    public ResponseEntity<Object> getTransactionsByUserId(@PathVariable Long userId) {
        try {
            return ResponseEntity.ok(transactionService.getTransactionsByUserId(userId));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error retrieving transactions for user: " + userId);
        }
    }
    @GetMapping("/search")
    public ResponseEntity<List<TransactionGETPOSTResponseDTO>> searchTransactions(
            @RequestParam(required = false) String IBAN,
            @RequestParam(required = false) Double amount,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false) Double amountGreater,
            @RequestParam(required = false) Double amountLess) {

        List<Transaction> transactions = transactionService.filterTransactions(IBAN, amount,amountGreater,amountLess, startDate, endDate);
        List<TransactionGETPOSTResponseDTO> transactionDto = transactions.stream()
                .map(t -> new TransactionGETPOSTResponseDTO(t.getFromAccount(), t.getToAccount(), t.getAmount(), t.getDate(), t.getUserId()))
                .collect(Collectors.toList());

        return ResponseEntity.ok(transactionDto);
    }
}
