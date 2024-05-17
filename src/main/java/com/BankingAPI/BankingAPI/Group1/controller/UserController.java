package com.BankingAPI.BankingAPI.Group1.controller;

import com.BankingAPI.BankingAPI.Group1.model.dto.*;
import com.BankingAPI.BankingAPI.Group1.model.Users;
import com.BankingAPI.BankingAPI.Group1.service.UserService;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.http.HttpStatus;
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
    @PreAuthorize("hasAnyRole('CUSTOMER','EMPLOYEE')")
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
        try{
            return ResponseEntity.status(200).body(userService.getUnapprovedUsers());
        } catch (Exception e) {
            return ResponseEntity.status(404).body(e.getMessage());
        }
    }

    @PutMapping("/{userId}/approve")
    @PreAuthorize("hasAnyRole('EMPLOYEE')")
    public ResponseEntity<Object> approveUser(@PathVariable Long userId, @RequestBody UserApprovalDTO approvalDTO){
        try {
            userService.approveUser(userId, approvalDTO);
            return ResponseEntity.status(HttpStatus.OK).body(new Object[0]);
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }
    @PutMapping
    @PreAuthorize("hasAnyRole('EMPLOYEE')")
    public ResponseEntity<Object> updateDailyLimit(@RequestBody Users user){
        try {
            userService.updateDailyLimit(user);
            return ResponseEntity.status(HttpStatus.OK).body(new Object[0]);
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found.");
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }
}
