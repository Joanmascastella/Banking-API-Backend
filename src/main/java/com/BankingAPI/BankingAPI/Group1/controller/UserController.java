package com.BankingAPI.BankingAPI.Group1.controller;

import com.BankingAPI.BankingAPI.Group1.model.dto.*;
import com.BankingAPI.BankingAPI.Group1.model.Users;
import com.BankingAPI.BankingAPI.Group1.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

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
    return ResponseEntity.status(200).body(userService.getAllUsers());
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
    @PreAuthorize("hasAnyRole('EMPLOYEE')")
    public ResponseEntity<Object> getUnapprovedUsers() {
        return ResponseEntity.status(200).body(userService.getUnapprovedUsers());
    }

    @PutMapping("/{userId}/approve")
    @PreAuthorize("hasAnyRole('EMPLOYEE')")
    public ResponseEntity<Object> approveUser(@PathVariable Long userId, @RequestBody UserApprovalDTO approvalDTO){
        try{
            userService.approveUser(userId, approvalDTO);
            return ResponseEntity.status(200).body("User approved and accounts created succesfully.");
        } catch (Exception ex) {
            return ResponseEntity.status(404).body(ex.getMessage());
        }
    }
    @PutMapping
    @PreAuthorize("hasAnyRole('EMPLOYEE')")
    public ResponseEntity<Object> updateDailyLimit(@RequestBody Users user){
        try{
            userService.updateDailyLimit(user);
            return ResponseEntity.status(200).body("Daily limit was updated successfully.");
        } catch (Exception ex) {
            return ResponseEntity.status(404).body("User not found.");
        }
    }
}
