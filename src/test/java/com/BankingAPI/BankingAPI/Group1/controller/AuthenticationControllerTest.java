package com.BankingAPI.BankingAPI.Group1.controller;

import com.BankingAPI.BankingAPI.Group1.config.ApiTestConfiguration;
import com.BankingAPI.BankingAPI.Group1.config.testConfigurations.TestSecurityConfig;
import com.BankingAPI.BankingAPI.Group1.exception.CustomAuthenticationException;
import com.BankingAPI.BankingAPI.Group1.model.dto.LoginDTO;
import com.BankingAPI.BankingAPI.Group1.model.dto.UserPOSTResponseDTO;
import com.BankingAPI.BankingAPI.Group1.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
@WebMvcTest(AuthenticationController.class)
@Import({ApiTestConfiguration.class, TestSecurityConfig.class})
@ActiveProfiles("test")
class AuthenticationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void testRegisterUser_Success() throws Exception {
        UserPOSTResponseDTO userDTO = new UserPOSTResponseDTO("username", "email@example.com", "firstName", "lastName", "123456789", "1234567890", LocalDate.of(1990, 1, 1), "password");

        mockMvc.perform(post("/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userDTO))
                        .with(csrf()))
                .andExpect(status().isCreated());
    }

    @Test
    void testRegisterUser_EmailAlreadyInUse() throws Exception {
        UserPOSTResponseDTO userDTO = new UserPOSTResponseDTO("username", "email@example.com", "firstName", "lastName", "123456789", "1234567890", LocalDate.of(1990, 1, 1), "password");
        doThrow(new IllegalStateException("Email already in use")).when(userService).createUser(any(UserPOSTResponseDTO.class));

        mockMvc.perform(post("/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userDTO))
                        .with(csrf()))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$").value("Email already in use"));
    }

    @Test
    void testRegisterUser_BadRequest() throws Exception {
        UserPOSTResponseDTO userDTO = new UserPOSTResponseDTO("username", "", "firstName", "lastName", "123456789", "1234567890", LocalDate.of(1990, 1, 1), "password");
        doThrow(new IllegalArgumentException("Bad Request")).when(userService).createUser(any(UserPOSTResponseDTO.class));

        mockMvc.perform(post("/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userDTO))
                        .with(csrf()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$").value("Bad Request"));
    }

    @Test
    void testLogin_Success() throws Exception {
        LoginDTO loginDTO = new LoginDTO("johndoe", "123");
        String token = "sampleToken";
        when(userService.login(anyString(), anyString())).thenReturn(token);

        mockMvc.perform(post("/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginDTO))
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value(token));
    }

    @Test
    void testLogin_InvalidCredentials() throws Exception {
        LoginDTO loginDTO = new LoginDTO("username", "wrongpassword");
        when(userService.login(anyString(), anyString())).thenThrow(new CustomAuthenticationException("Invalid username/password"));

        mockMvc.perform(post("/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginDTO))
                        .with(csrf()))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$").value("Invalid username/password"));
    }

    @Test
    void testLogin_UserNotFound() throws Exception {
        LoginDTO loginDTO = new LoginDTO("unknownuser", "password");
        when(userService.login(anyString(), anyString())).thenThrow(new CustomAuthenticationException("User not found"));

        mockMvc.perform(post("/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginDTO))
                        .with(csrf()))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$").value("User not found"));
    }
}
