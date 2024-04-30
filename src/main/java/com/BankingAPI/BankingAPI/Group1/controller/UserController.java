package com.BankingAPI.BankingAPI.Group1.controller;

import com.BankingAPI.BankingAPI.Group1.model.Users;
import com.BankingAPI.BankingAPI.Group1.model.dto.LoginDTO;
import com.BankingAPI.BankingAPI.Group1.model.dto.TokenDTO;
import com.BankingAPI.BankingAPI.Group1.model.dto.UserPOSTResponseDTO;
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
}
