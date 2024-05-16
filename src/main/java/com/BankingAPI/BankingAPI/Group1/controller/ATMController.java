package com.BankingAPI.BankingAPI.Group1.controller;
import com.BankingAPI.BankingAPI.Group1.model.Transaction;
import com.BankingAPI.BankingAPI.Group1.model.Users;
import com.BankingAPI.BankingAPI.Group1.model.dto.ATMLoginDTO;
import com.BankingAPI.BankingAPI.Group1.model.dto.LoginDTO;
import com.BankingAPI.BankingAPI.Group1.model.dto.TokenDTO;
import com.BankingAPI.BankingAPI.Group1.model.dto.TransactionGETPOSTResponseDTO;
import com.BankingAPI.BankingAPI.Group1.service.AccountService;
import com.BankingAPI.BankingAPI.Group1.service.TransactionService;
import com.BankingAPI.BankingAPI.Group1.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
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


    @PostMapping(value = "/login")
    public Object login(@RequestBody ATMLoginDTO dto) throws Exception {
        return new TokenDTO(
                userService.atmLogin(dto.email(), dto.password())
        );
    }

    @PostMapping("/withdrawals")
    @PreAuthorize("hasAnyRole('CUSTOMER', 'EMPLOYEE')")
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
    @PreAuthorize("hasAnyRole('CUSTOMER', 'EMPLOYEE')")
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
