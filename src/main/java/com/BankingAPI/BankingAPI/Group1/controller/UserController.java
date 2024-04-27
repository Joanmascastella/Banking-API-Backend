package com.BankingAPI.BankingAPI.Group1.controller;

import com.BankingAPI.BankingAPI.Group1.model.dto.FindIbanResponseDTO;
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


}
