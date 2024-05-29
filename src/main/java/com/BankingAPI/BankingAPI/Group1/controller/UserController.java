package com.BankingAPI.BankingAPI.Group1.controller;

import com.BankingAPI.BankingAPI.Group1.exception.IBANGenerationException;
import com.BankingAPI.BankingAPI.Group1.exception.InactiveUserException;
import com.BankingAPI.BankingAPI.Group1.exception.InvalidDailyLimitException;
import com.BankingAPI.BankingAPI.Group1.exception.UnauthorizedException;
import com.BankingAPI.BankingAPI.Group1.model.dto.*;
import com.BankingAPI.BankingAPI.Group1.model.Users;
import com.BankingAPI.BankingAPI.Group1.service.UserService;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(value = "/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('EMPLOYEE')")
    public ResponseEntity<Object> getAllUsers() {
        try {
            return ResponseEntity.status(200).body(userService.getAllUsers());
        } catch (Exception ex) {
            return ResponseEntity.status(404).body("Users not found.");
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
            return ResponseEntity.status(500).body("An internal error occurred: " + ex.getMessage());
        }
    }

    @GetMapping(value = "/getOne")
    @PreAuthorize("hasAnyRole('CUSTOMER')")
    public ResponseEntity<Object> getUser(){
        try {
            UserGetOneRESPONSE details = userService.getUserDetails();
            if (details == null) {
                return ResponseEntity.status(404).body("No Such User");
            }
            return ResponseEntity.ok(details);
        } catch (Exception ex) {
            return ResponseEntity.status(500).body("An internal error occurred: " + ex.getMessage());
        }
    }


    @GetMapping("/iban")
    @PreAuthorize("hasAnyRole('CUSTOMER','EMPLOYEE')")
    public ResponseEntity<Object> getIbanByFirstNameLastName(@RequestParam String firstName, @RequestParam String lastName) {

        try {
            FindIbanRequestDTO findIban = new FindIbanRequestDTO(firstName, lastName);
            FindIbanResponseDTO iban = userService.getIbanByFirstNameLastName(findIban);
            if (iban != null) {
                return ResponseEntity.ok(iban);
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("IBAN not found for given name.");
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
        } catch (InvalidDailyLimitException e) {
            return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(e.getMessage());
        } catch (IBANGenerationException | RuntimeException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }
    @PutMapping
    @PreAuthorize("hasAnyRole('EMPLOYEE')")
    public ResponseEntity<Object> updateDailyLimit(@RequestBody Users user){
        try {
            userService.updateDailyLimit(user);
            return ResponseEntity.status(HttpStatus.OK).body(new Object[0]);
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (InvalidDailyLimitException e) {
            return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(e.getMessage());
        }
    }

    @DeleteMapping("/{userId}")
    @PreAuthorize("hasAnyRole('EMPLOYEE')")
    public ResponseEntity<Object> closeAccount(@PathVariable long userId) {
        try {
            userService.closeAccount(userId);
            return ResponseEntity.status(HttpStatus.OK).body(new Object[0]);
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (InactiveUserException e) {
            return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(e.getMessage());
        } catch (UnauthorizedException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.getMessage());
        }
    }
}