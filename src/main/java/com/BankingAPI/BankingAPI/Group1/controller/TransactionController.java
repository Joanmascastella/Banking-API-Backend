package com.BankingAPI.BankingAPI.Group1.controller;

import com.BankingAPI.BankingAPI.Group1.service.TransactionService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = "/transactions")
public class TransactionController {
    private TransactionService transactionService;

    public TransactionController(TransactionService transactionService) {
        this.transactionService = transactionService;
    }


    @GetMapping
    public ResponseEntity<Object> getAllTransactions() {
        return ResponseEntity.status(200).body(transactionService.allTransactions());
    }
    @GetMapping("/{userId}/history")
    public ResponseEntity<Object> getTransactionsByUserId(@PathVariable Long userId) {
        try {
            return ResponseEntity.ok(transactionService.getTransactionsByUserId(userId));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error retrieving transactions for user: " + userId);
        }
    }
}
