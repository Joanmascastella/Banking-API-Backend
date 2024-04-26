package com.BankingAPI.BankingAPI.Group1.controller;

import com.BankingAPI.BankingAPI.Group1.model.dto.UserPOSTResponseDTO;
import com.BankingAPI.BankingAPI.Group1.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(value = "/users")
public class UserController {

    private UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }


    @GetMapping
    public ResponseEntity<Object> getAllUsers() {
    return ResponseEntity.status(200).body(userService.getAllUsers());
    }

    @RequestMapping(value = "/register")
    @PostMapping
    public ResponseEntity<Object> createUser(@RequestBody UserPOSTResponseDTO userPOSTResponseDTO) {
        try {
            return ResponseEntity.status(200).body(userService.createUser(userPOSTResponseDTO));
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
