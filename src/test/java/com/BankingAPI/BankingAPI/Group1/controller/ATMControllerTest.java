package com.BankingAPI.BankingAPI.Group1.controller;

import com.BankingAPI.BankingAPI.Group1.config.ApiTestConfiguration;
import com.BankingAPI.BankingAPI.Group1.config.testConfigurations.TestSecurityConfig;
import com.BankingAPI.BankingAPI.Group1.exception.CustomAuthenticationException;
import com.BankingAPI.BankingAPI.Group1.model.dto.ATMLoginDTO;
import com.BankingAPI.BankingAPI.Group1.model.dto.TransactionGETPOSTResponseDTO;
import com.BankingAPI.BankingAPI.Group1.model.dto.TransferMoneyPOSTResponse;
import com.BankingAPI.BankingAPI.Group1.service.TransactionService;
import com.BankingAPI.BankingAPI.Group1.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(SpringExtension.class)
@WebMvcTest(ATMController.class)
@Import({ApiTestConfiguration.class, TestSecurityConfig.class})
@ActiveProfiles("test")
class ATMControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TransactionService transactionService;

    @MockBean
    private UserService userService;

    @Autowired
    private ObjectMapper objectMapper;

    private TransferMoneyPOSTResponse transactionDTO;
    private TransactionGETPOSTResponseDTO responseDTO;

    @BeforeEach
    void setUp() {
        transactionDTO = new TransferMoneyPOSTResponse("DE89370400440532013000", "DE89370400440532013012", 100.0);
        responseDTO = new TransactionGETPOSTResponseDTO("DE89370400440532013000", "DE89370400440532013012", 100.0, LocalDate.now(), 1L);
    }
    @Test
    void testLogin_Success() throws Exception {
        ATMLoginDTO loginDTO = new ATMLoginDTO("john.doe@example.com", "123");
        String token = "sampleToken";
        when(userService.atmLogin(anyString(), anyString())).thenReturn(token);

        mockMvc.perform(post("/atm/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginDTO))
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value(token));
    }
    @Test
    void testLogin_InvalidCredentials() throws Exception {
        ATMLoginDTO loginDTO = new ATMLoginDTO("email", "password");
        when(userService.atmLogin(anyString(), anyString())).thenThrow(new CustomAuthenticationException("Invalid email/password"));

        mockMvc.perform(post("/atm/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginDTO))
                        .with(csrf()))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$").value("Invalid email/password"));
    }

    @Test
    void testLogin_UserNotFound() throws Exception {
        ATMLoginDTO loginDTO = new ATMLoginDTO("unknownemail", "password");
        when(userService.atmLogin(anyString(), anyString())).thenThrow(new CustomAuthenticationException("User not found"));

        mockMvc.perform(post("/atm/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginDTO))
                        .with(csrf()))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$").value("User not found"));
    }

    @Test
    @WithMockUser(username = "johndoe", password = "123", roles = {"CUSTOMER", "EMPLOYEE"})
    void deposit_Success() throws Exception {
        when(transactionService.processDeposit(any(TransferMoneyPOSTResponse.class))).thenReturn(responseDTO);

        mockMvc.perform(post("/atm/deposits")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(transactionDTO))
                        .with(csrf()))
                .andExpect(status().isCreated())
                .andExpect(content().json(objectMapper.writeValueAsString(responseDTO)));
    }


    @Test
    @WithMockUser(username = "johndoe", password = "123", roles = {"CUSTOMER", "EMPLOYEE"})
    void deposit_AccountNotFound() throws Exception {
        String errorMessage = "Account with IBAN: DE89370400440532013000 not found";
        when(transactionService.processDeposit(any(TransferMoneyPOSTResponse.class))).thenThrow(new Exception(errorMessage));

        mockMvc.perform(post("/atm/deposits")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(transactionDTO))
                        .with(csrf()))
                .andExpect(status().isNotFound())
                .andExpect(content().string(errorMessage));
    }

    @Test
    @WithMockUser(username = "johndoe", password = "123", roles = {"CUSTOMER", "EMPLOYEE"})
    void deposit_ExceedsDailyLimit() throws Exception {
        String errorMessage = "Transaction exceeds daily limit";
        when(transactionService.processDeposit(any(TransferMoneyPOSTResponse.class))).thenThrow(new Exception(errorMessage));

        mockMvc.perform(post("/atm/deposits")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(transactionDTO))
                        .with(csrf()))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(content().string(errorMessage));
    }

    @Test
    @WithMockUser(username = "johndoe", password = "123", roles = {"CUSTOMER", "EMPLOYEE"})
    void deposit_GeneralError() throws Exception {
        String errorMessage = "General error";
        when(transactionService.processDeposit(any(TransferMoneyPOSTResponse.class))).thenThrow(new Exception(errorMessage));

        mockMvc.perform(post("/atm/deposits")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(transactionDTO))
                        .with(csrf()))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(errorMessage));
    }

    @Test
    @WithMockUser(username = "johndoe", password = "123", roles = {"CUSTOMER", "EMPLOYEE"})
    void withdraw_Success() throws Exception {
        when(transactionService.processWithdrawal(any(TransferMoneyPOSTResponse.class))).thenReturn(responseDTO);

        mockMvc.perform(post("/atm/withdrawals")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(transactionDTO))
                        .with(csrf()))
                .andExpect(status().isCreated())
                .andExpect(content().json(objectMapper.writeValueAsString(responseDTO)));
    }

    @Test
    @WithMockUser(username = "johndoe", password = "123", roles = {"CUSTOMER", "EMPLOYEE"})
    void withdraw_AccountNotFound() throws Exception {
        String errorMessage = "Account with IBAN: DE89370400440532013000 not found";
        when(transactionService.processWithdrawal(any(TransferMoneyPOSTResponse.class))).thenThrow(new Exception(errorMessage));

        mockMvc.perform(post("/atm/withdrawals")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(transactionDTO))
                        .with(csrf()))
                .andExpect(status().isNotFound())
                .andExpect(content().string(errorMessage));
    }

    @Test
    @WithMockUser(username = "johndoe", password = "123", roles = {"CUSTOMER", "EMPLOYEE"})
    void withdraw_ExceedsDailyLimit() throws Exception {
        String errorMessage = "Transaction exceeds daily limit";
        when(transactionService.processWithdrawal(any(TransferMoneyPOSTResponse.class))).thenThrow(new Exception(errorMessage));

        mockMvc.perform(post("/atm/withdrawals")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(transactionDTO))
                        .with(csrf()))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(content().string(errorMessage));
    }

    @Test
    @WithMockUser(username = "johndoe", password = "123", roles = {"CUSTOMER", "EMPLOYEE"})
    void withdraw_GeneralError() throws Exception {
        String errorMessage = "General error";
        when(transactionService.processWithdrawal(any(TransferMoneyPOSTResponse.class))).thenThrow(new Exception(errorMessage));

        mockMvc.perform(post("/atm/withdrawals")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(transactionDTO))
                        .with(csrf()))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(errorMessage));
    }
}
