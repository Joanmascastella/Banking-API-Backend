package com.BankingAPI.BankingAPI.Group1.controller;

import com.BankingAPI.BankingAPI.Group1.model.dto.*;
import com.BankingAPI.BankingAPI.Group1.model.Users;
import com.BankingAPI.BankingAPI.Group1.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(value = "/users")
public class UserController {

    private UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('EMPLOYEE')")
    public ResponseEntity<Object> getAllUsers() {
        try {
            return ResponseEntity.status(200).body(userService.getAllUsers());
        } catch (Exception ex) {
            return ResponseEntity.status(404).body("User not found.");
        }
    }

    @GetMapping(value = "/details")
    @PreAuthorize("hasAnyRole('CUSTOMER')")
    public ResponseEntity<Object> getUserDetails(){
        try {
            List<AccountDetailsGETResponse> details = userService.getAccountDetailsForCurrentUser();
            if (details.isEmpty()) {
                return ResponseEntity.status(404).body("No accounts found for user.");
            }
            return ResponseEntity.ok(details);
        } catch (Exception ex) {
            return ResponseEntity.status(500).body("An internal error occurred.");
        }
    }

    @GetMapping("/iban")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<Object> getIbanByFirstNameLastName(@RequestParam String firstName, @RequestParam String lastName) {
        try {
            FindIbanResponseDTO iban = userService.getIbanByFirstNameLastName(firstName, lastName);
            if (iban != null) {
                return ResponseEntity.ok(iban);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }

    @GetMapping("/noncustomers")
    @PreAuthorize("hasRole('EMPLOYEE')")
    public ResponseEntity<Object> getUnapprovedUsers() {
        return ResponseEntity.status(200).body(userService.getUnapprovedUsers());
    }

    @PutMapping("/approve")
    @PreAuthorize("hasRole('EMPLOYEE')")
    public ResponseEntity<Object> approveUser(@RequestBody Users user, double absoluteSavingLimit, double absoluteCheckingLimit){
        try{
            userService.approveUser(user, absoluteSavingLimit, absoluteCheckingLimit);
            return ResponseEntity.status(200).body("User approved and accounts created succesfully.");
        } catch (Exception ex) {
            return ResponseEntity.status(404).body("User not found.");
        }
    }
    @PutMapping
    @PreAuthorize("hasRole('EMPLOYEE')")
    public ResponseEntity<Object> updateDailyLimit(@RequestBody Users user){
        try{
            userService.updateDailyLimit(user);
            return ResponseEntity.status(200).body("Daily limit was updated successfully.");
        } catch (Exception ex) {
            return ResponseEntity.status(404).body("User not found.");
        }
    }
}
