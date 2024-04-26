package com.BankingAPI.BankingAPI.Group1.controller;

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
    private UserService userService;

    public AuthenticationController(UserService userService) {
        this.userService = userService;
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

    //I wrote this to test if the jwt was working
    //        @RequestMapping(value = "/login")
    //        @PostMapping
    //        public Object login(@RequestBody LoginDTO dto) throws Exception {
    //            return new TokenDTO(
    //                    userService.login(dto.username(), dto.password())
    //            );
    //        }
}
