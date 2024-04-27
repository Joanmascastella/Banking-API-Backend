package com.BankingAPI.BankingAPI.Group1.controller;
import com.BankingAPI.BankingAPI.Group1.model.Transaction;
import com.BankingAPI.BankingAPI.Group1.model.Users;
import com.BankingAPI.BankingAPI.Group1.model.dto.ATMLoginDTO;
import com.BankingAPI.BankingAPI.Group1.model.dto.TransactionGETPOSTResponseDTO;
import com.BankingAPI.BankingAPI.Group1.service.AccountService;
import com.BankingAPI.BankingAPI.Group1.service.TransactionService;
import com.BankingAPI.BankingAPI.Group1.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/atm")
public class ATMController {


    @Autowired
    private TransactionService transactionService;
    @Autowired
    private UserService userService;

    @PostMapping("/login")
    public ResponseEntity<String> login(@RequestBody ATMLoginDTO loginDTO) {
        try {
            Users user = userService.findByEmail(loginDTO.email());
            if (user != null && user.getPassword().equals(loginDTO.password())) {
                return ResponseEntity.ok("Login successful");
            } else {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid email or password");
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Login request failed: " + e.getMessage());
        }
    }

    @PostMapping("/withdrawals")
    public ResponseEntity<TransactionGETPOSTResponseDTO> withdraw(@Valid @RequestBody TransactionGETPOSTResponseDTO transaction) {
        try {
            TransactionGETPOSTResponseDTO completedTransaction = transactionService.processWithdrawal(transaction);
            return ResponseEntity.status(201).body(completedTransaction);
        } catch (IllegalStateException e) {
            return ResponseEntity.status(422).body(null);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(404).body(null);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(null);
        }
    }
    @PostMapping("/deposits")
    public ResponseEntity<TransactionGETPOSTResponseDTO> deposit(@RequestBody TransactionGETPOSTResponseDTO transaction) {
        try {
            TransactionGETPOSTResponseDTO completedTransaction = transactionService.processDeposit(transaction);
            return ResponseEntity.status(201).body(completedTransaction);
        } catch (IllegalStateException e) {
            return ResponseEntity.status(422).body(null);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(404).body(null);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(null);
        }
    }
}
