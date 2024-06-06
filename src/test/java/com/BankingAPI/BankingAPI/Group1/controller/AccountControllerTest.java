package com.BankingAPI.BankingAPI.Group1.controller;

import com.BankingAPI.BankingAPI.Group1.config.ApiTestConfiguration;
import com.BankingAPI.BankingAPI.Group1.model.Enums.AccountType;
import com.BankingAPI.BankingAPI.Group1.model.Enums.UserType;
import com.BankingAPI.BankingAPI.Group1.model.Users;
import com.BankingAPI.BankingAPI.Group1.model.dto.AccountGETPOSTResponseDTO;
import com.BankingAPI.BankingAPI.Group1.model.dto.TransactionGETPOSTResponseDTO;
import com.BankingAPI.BankingAPI.Group1.model.dto.TransferMoneyPOSTResponse;
import com.BankingAPI.BankingAPI.Group1.service.AccountService;
import com.BankingAPI.BankingAPI.Group1.service.TransactionService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.Arrays;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(SpringExtension.class)
@WebMvcTest(AccountController.class)
@Import(ApiTestConfiguration.class)

class AccountControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TransactionService transactionService;

    @MockBean
    private AccountService accountService;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private BCryptPasswordEncoder bCryptPasswordEncoder;

    private AccountGETPOSTResponseDTO activeAccount;

    private AccountGETPOSTResponseDTO inactiveAccount;

    private Users user;

    @BeforeEach
    public void setup() {
        user = new Users("johndoe", "john.doe@example.com", "John", "Doe", "123456789", "0123456789", LocalDate.of(1990, 1, 1), 5000.0, 1000.0, true, true, UserType.ROLE_CUSTOMER, bCryptPasswordEncoder.encode("123"));
        activeAccount = new AccountGETPOSTResponseDTO(user.getId(), "NL89INHO0044053200", "EUR", AccountType.CHECKING, true, 5000.0, 0.00);
        inactiveAccount = new AccountGETPOSTResponseDTO(user.getId(), "NL89INHO0044053200", "EUR", AccountType.CHECKING, false, 200.0, 0.00);
    }


    @Test
    @WithMockUser(username = "johndoe", password = "123", roles = "CUSTOMER")
    void transferMoneyToOwnAccount_Success() throws Exception {
        TransferMoneyPOSTResponse transactionDTO = new TransferMoneyPOSTResponse("DE89370400440532013000", "DE89370400440532013012", 100.0);

        Mockito.when(transactionService.transferMoneyToOwnAccount(any(TransferMoneyPOSTResponse.class)))
                .thenReturn(new TransactionGETPOSTResponseDTO(
                        "DE89370400440532013000",
                        "DE89370400440532013012",
                        100.0,
                        LocalDate.now(),
                        1L));

        mockMvc.perform(post("/accounts/own/transfers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(transactionDTO))
                        .with(csrf()))
                .andExpect(status().isCreated())
                .andExpect(content().string("Transfer successful"));
    }


    @Test
    @WithMockUser(username = "johndoe", password = "123", roles = "CUSTOMER")
    void transferMoneyToOwnAccount_NotFound() throws Exception {
        TransferMoneyPOSTResponse transactionDTO = new TransferMoneyPOSTResponse("DE89370400440532013000", "DE89370400440532013012", 100.0);

        Mockito.when(transactionService.transferMoneyToOwnAccount(any(TransferMoneyPOSTResponse.class)))
                .thenThrow(new Exception("Account with IBAN: DE89370400440532013000 not found"));

        mockMvc.perform(post("/accounts/own/transfers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(transactionDTO))
                        .with(csrf()))
                .andExpect(status().isNotFound())
                .andExpect(content().string("Account with IBAN: DE89370400440532013000 not found"));
    }

    @Test
    @WithMockUser(username = "johndoe", password = "123", roles = "CUSTOMER")
    void transferMoneyToOwnAccount_InsufficientFunds() throws Exception {
        TransferMoneyPOSTResponse transactionDTO = new TransferMoneyPOSTResponse("DE89370400440532013000", "DE89370400440532013012", 100.0);

        Mockito.when(transactionService.transferMoneyToOwnAccount(any(TransferMoneyPOSTResponse.class)))
                .thenThrow(new Exception("Insufficient funds"));

        mockMvc.perform(post("/accounts/own/transfers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(transactionDTO))
                        .with(csrf()))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(content().string("Insufficient funds"));
    }

    @Test
    @WithMockUser(username = "johndoe", password = "123", roles = "CUSTOMER")
    void transferMoneyToOwnAccount_ExceedsDailyLimit() throws Exception {
        TransferMoneyPOSTResponse transactionDTO = new TransferMoneyPOSTResponse("DE89370400440532013000", "DE89370400440532013012", 100.0);

        Mockito.when(transactionService.transferMoneyToOwnAccount(any(TransferMoneyPOSTResponse.class)))
                .thenThrow(new Exception("Transaction exceeds daily limit"));

        mockMvc.perform(post("/accounts/own/transfers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(transactionDTO))
                        .with(csrf()))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(content().string("Transaction exceeds daily limit"));
    }

    @Test
    @WithMockUser(username = "johndoe", password = "123", roles = "CUSTOMER")
    void transferMoneyToOwnAccount_GeneralError() throws Exception {
        TransferMoneyPOSTResponse transactionDTO = new TransferMoneyPOSTResponse("DE89370400440532013000", "DE89370400440532013012", 100.0);

        Mockito.when(transactionService.transferMoneyToOwnAccount(any(TransferMoneyPOSTResponse.class)))
                .thenThrow(new Exception("General error"));

        mockMvc.perform(post("/accounts/own/transfers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(transactionDTO))
                        .with(csrf()))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("General error"));
    }

    @Test
    @WithMockUser(username = "Employee", password = "employee", roles = "EMPLOYEE")
    public void getAllCustomerAccountsShouldReturnAccounts() throws Exception {
        given(accountService.getAllCustomerAccounts()).willReturn(Arrays.asList(activeAccount));
        this.mockMvc.perform(get("/accounts/customers")).andExpect(
                        status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].IBAN").value(activeAccount.IBAN()));

    }

    @Test
    @WithMockUser(username = "Employee", password = "employee", roles = "EMPLOYEE")
    public void getAccountsByAbsoluteLimitShouldReturnAccounts() throws Exception {
        given(accountService.findByAbsoluteLimit(2000L)).willReturn(Arrays.asList(activeAccount));
        this.mockMvc.perform(get("/accounts/byAbsoluteLimit").queryParam("absoluteLimit", String.valueOf(2000L))).andExpect(
                        status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].IBAN").value(activeAccount.IBAN()));

    }

    @Test
    @WithMockUser(username = "Employee", password = "employee", roles = "EMPLOYEE")
    public void getAccountsByInactiveTagShouldReturnAccounts() throws Exception {
        given(accountService.findByInactiveTag()).willReturn(Arrays.asList(inactiveAccount));
        this.mockMvc.perform(get("/accounts/inactive")).andExpect(
                        status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].IBAN").value(inactiveAccount.IBAN()));

    }

    @Test
    @WithMockUser(username = "Employee", password = "employee", roles = "EMPLOYEE")
    public void updateAccount() throws Exception {
        String IBAN = "NL89INHO0044053200";
        AccountGETPOSTResponseDTO account = new AccountGETPOSTResponseDTO(1L, "NL89INHO0044053200", "EUR", AccountType.CHECKING, true, 2500, -200.0);
        Mockito.doNothing().when(accountService).updateAccount(eq(IBAN),any(AccountGETPOSTResponseDTO.class));

        mockMvc.perform(put("/accounts/customers/" + IBAN)
                .contentType(MediaType.APPLICATION_JSON)
                .content(new ObjectMapper().writeValueAsString(account))
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(content().string("[]"));
    }

    @Test
    @WithMockUser(username = "Employee", password = "employee", roles = "EMPLOYEE")
    public void updateAccount_AccountNotFound() throws Exception {
        String IBAN = "NL89INHO0044053200";
        AccountGETPOSTResponseDTO account = new AccountGETPOSTResponseDTO(1L, "NL89INHO0044053200", "EUR", AccountType.CHECKING, true, 2500, -200.0);
        Mockito.doThrow(new EntityNotFoundException("Account not found.")).when(accountService).updateAccount(eq(IBAN), any(AccountGETPOSTResponseDTO.class));

        mockMvc.perform(put("/accounts/customers/" + IBAN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(account))
                        .with(csrf()))
                .andExpect(status().isNotFound())
                .andExpect(content().string("Account not found."));
    }

    @Test
    @WithMockUser(username = "Employee", password = "employee", roles = "EMPLOYEE")
    public void updateAccount_GeneralException() throws Exception {
        String IBAN = "NL89INHO0044053200";
        AccountGETPOSTResponseDTO account = new AccountGETPOSTResponseDTO(1L, "NL89INHO0044053200", "EUR", AccountType.CHECKING, true, 2500, -200.0);
        Mockito.doThrow(new RuntimeException("Unexpected error.")).when(accountService).updateAccount(eq(IBAN), any(AccountGETPOSTResponseDTO.class));

        mockMvc.perform(put("/accounts/customers/" + IBAN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(account))
                        .with(csrf()))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Unexpected error."));
    }
}
