package com.BankingAPI.BankingAPI.Group1.controller;

import com.BankingAPI.BankingAPI.Group1.config.ApiTestConfiguration;
import com.BankingAPI.BankingAPI.Group1.model.dto.TransactionGETPOSTResponseDTO;
import com.BankingAPI.BankingAPI.Group1.model.dto.TransferMoneyPOSTResponse;
import com.BankingAPI.BankingAPI.Group1.service.AccountService;
import com.BankingAPI.BankingAPI.Group1.service.TransactionService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
@WebMvcTest(TransactionController.class)
@Import(ApiTestConfiguration.class)
class TransactionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TransactionService transactionService;

    @MockBean
    private AccountService accountService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @WithMockUser(username = "johndoe", password = "123", roles = {"CUSTOMER", "EMPLOYEE"})
    void transferToOtherCustomer_Success() throws Exception {
        TransferMoneyPOSTResponse transactionDTO = new TransferMoneyPOSTResponse("DE89370400440532013000", "DE89370400440532013012", 100.0);
        TransactionGETPOSTResponseDTO responseDTO = new TransactionGETPOSTResponseDTO(
                "DE89370400440532013000",
                "DE89370400440532013012",
                100.0,
                LocalDate.now(),
                1L);

        Mockito.when(transactionService.transferToOtherCustomer(Mockito.any(TransferMoneyPOSTResponse.class)))
                .thenReturn(responseDTO);

        mockMvc.perform(post("/transactions/transfers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(transactionDTO))
                        .with(csrf()))
                .andExpect(status().isCreated())
                .andExpect(content().json(objectMapper.writeValueAsString(responseDTO)));
    }

    @Test
    @WithMockUser(username = "johndoe", password = "123", roles = {"CUSTOMER", "EMPLOYEE"})
    void transferToOtherCustomer_NotFound() throws Exception {
        TransferMoneyPOSTResponse transactionDTO = new TransferMoneyPOSTResponse("DE89370400440532013000", "DE89370400440532013012", 100.0);

        Mockito.when(transactionService.transferToOtherCustomer(Mockito.any(TransferMoneyPOSTResponse.class)))
                .thenThrow(new Exception("Account with IBAN: DE89370400440532013000 not found"));

        mockMvc.perform(post("/transactions/transfers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(transactionDTO))
                        .with(csrf()))
                .andExpect(status().isNotFound())
                .andExpect(content().string("Account with IBAN: DE89370400440532013000 not found"));
    }

    @Test
    @WithMockUser(username = "johndoe", password = "123", roles = {"CUSTOMER", "EMPLOYEE"})
    void transferToOtherCustomer_InsufficientFunds() throws Exception {
        TransferMoneyPOSTResponse transactionDTO = new TransferMoneyPOSTResponse("DE89370400440532013000", "DE89370400440532013012", 100.0);

        Mockito.when(transactionService.transferToOtherCustomer(Mockito.any(TransferMoneyPOSTResponse.class)))
                .thenThrow(new Exception("Insufficient funds"));

        mockMvc.perform(post("/transactions/transfers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(transactionDTO))
                        .with(csrf()))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(content().string("Insufficient funds"));
    }

    @Test
    @WithMockUser(username = "johndoe", password = "123", roles = {"CUSTOMER", "EMPLOYEE"})
    void transferToOtherCustomer_ExceedsDailyLimit() throws Exception {
        TransferMoneyPOSTResponse transactionDTO = new TransferMoneyPOSTResponse("DE89370400440532013000", "DE89370400440532013012", 100.0);

        Mockito.when(transactionService.transferToOtherCustomer(Mockito.any(TransferMoneyPOSTResponse.class)))
                .thenThrow(new Exception("Transaction exceeds daily limit"));

        mockMvc.perform(post("/transactions/transfers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(transactionDTO))
                        .with(csrf()))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(content().string("Transaction exceeds daily limit"));
    }

    @Test
    @WithMockUser(username = "johndoe", password = "123", roles = {"CUSTOMER", "EMPLOYEE"})
    void transferToOtherCustomer_CheckingAccountsError() throws Exception {
        TransferMoneyPOSTResponse transactionDTO = new TransferMoneyPOSTResponse("DE89370400440532013000", "DE89370400440532013012", 100.0);

        Mockito.when(transactionService.transferToOtherCustomer(Mockito.any(TransferMoneyPOSTResponse.class)))
                .thenThrow(new Exception("Both accounts must be of type CHECKING."));

        mockMvc.perform(post("/transactions/transfers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(transactionDTO))
                        .with(csrf()))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Both accounts must be of type CHECKING."));
    }

    @Test
    @WithMockUser(username = "johndoe", password = "123", roles = {"CUSTOMER", "EMPLOYEE"})
    void transferToOtherCustomer_GeneralError() throws Exception {
        TransferMoneyPOSTResponse transactionDTO = new TransferMoneyPOSTResponse("DE89370400440532013000", "DE89370400440532013012", 100.0);

        Mockito.when(transactionService.transferToOtherCustomer(Mockito.any(TransferMoneyPOSTResponse.class)))
                .thenThrow(new Exception("General error"));

        mockMvc.perform(post("/transactions/transfers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(transactionDTO))
                        .with(csrf()))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("General error"));
    }
}
