package com.BankingAPI.BankingAPI.Group1.controller;

import com.BankingAPI.BankingAPI.Group1.exception.CustomAuthenticationException;
import com.BankingAPI.BankingAPI.Group1.model.dto.LoginDTO;
import com.BankingAPI.BankingAPI.Group1.model.dto.TokenDTO;
import com.BankingAPI.BankingAPI.Group1.model.dto.UserPOSTResponseDTO;
import com.BankingAPI.BankingAPI.Group1.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping
public class AuthenticationController {
    private final UserService userService;

    public AuthenticationController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/register")
    public ResponseEntity<Object> registerUser(@RequestBody UserPOSTResponseDTO userDTO) {
        try {
            userService.createUser(userDTO);
            return ResponseEntity.status(201).body(new Object[0]);
        } catch (IllegalStateException e) {
            return ResponseEntity.status(409).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/login")
    public ResponseEntity<Object> login(@RequestBody LoginDTO dto) {
        try {
            String token = userService.login(dto.username(), dto.password());
            return ResponseEntity.ok(new TokenDTO(token));
        } catch (CustomAuthenticationException e) {
            return ResponseEntity.status(401).body(e.getMessage());
        }
    }
}
