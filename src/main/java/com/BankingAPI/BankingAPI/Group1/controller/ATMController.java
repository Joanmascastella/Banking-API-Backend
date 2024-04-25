package com.BankingAPI.BankingAPI.Group1.controller;
import com.BankingAPI.BankingAPI.Group1.model.Transaction;
import com.BankingAPI.BankingAPI.Group1.service.AccountService;
import com.BankingAPI.BankingAPI.Group1.service.TransactionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/atm")
public class ATMController {

    //@PostMapping("/login")
    //public ResponseEntity<Object> login(@RequestBody LoginRequestDTO loginRequest){return null;}


    @Autowired
    private TransactionService transactionService;

    @PostMapping("/withdrawals")
    public ResponseEntity<Transaction> withdraw(@Valid @RequestBody Transaction transaction) {
        try {
            transaction.setToAccount("ATM");
            Transaction completedTransaction = transactionService.processWithdrawal(transaction);
            return ResponseEntity.status(201).body(completedTransaction);
        } catch (IllegalStateException e) {
            return ResponseEntity.status(422).body(null);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(404).body(null);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(null); // Invalid request data
        }
    }
    @PostMapping("/deposits")
    public ResponseEntity<Transaction> deposit(@RequestBody Transaction transaction) {
        try {
            transaction.setFromAccount("ATM");
            Transaction completedTransaction = transactionService.processDeposit(transaction);
            return ResponseEntity.status(201).body(completedTransaction);
        } catch (IllegalStateException e) {
            return ResponseEntity.status(422).body(null); // Operation failed due to ATM limits
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(404).body(null); // Account not found
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(null); // Invalid request data
        }
    }
}
