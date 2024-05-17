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
import java.util.Map;

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

        Map<String, Object> expectedResponse = Map.of(
                "message", "Transfer successful",
                "transaction", responseDTO
        );

        Mockito.when(transactionService.transferToOtherCustomer(Mockito.any(TransferMoneyPOSTResponse.class)))
                .thenReturn(responseDTO);

        mockMvc.perform(post("/transactions/transfers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(transactionDTO))
                        .with(csrf()))
                .andExpect(status().isCreated())
                .andExpect(content().json(objectMapper.writeValueAsString(expectedResponse)));
    }

    @Test
    @WithMockUser(username = "johndoe", password = "123", roles = {"CUSTOMER", "EMPLOYEE"})
    void transferToOtherCustomer_NotFound() throws Exception {
        TransferMoneyPOSTResponse transactionDTO = new TransferMoneyPOSTResponse("DE89370400440532013000", "DE89370400440532013012", 100.0);
        String errorMessage = "Account with IBAN: DE89370400440532013000 not found";
        Map<String, String> expectedResponse = Map.of("message", errorMessage);

        Mockito.when(transactionService.transferToOtherCustomer(Mockito.any(TransferMoneyPOSTResponse.class)))
                .thenThrow(new Exception(errorMessage));

        mockMvc.perform(post("/transactions/transfers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(transactionDTO))
                        .with(csrf()))
                .andExpect(status().isNotFound())
                .andExpect(content().json(objectMapper.writeValueAsString(expectedResponse)));
    }

    @Test
    @WithMockUser(username = "johndoe", password = "123", roles = {"CUSTOMER", "EMPLOYEE"})
    void transferToOtherCustomer_InsufficientFunds() throws Exception {
        TransferMoneyPOSTResponse transactionDTO = new TransferMoneyPOSTResponse("DE89370400440532013000", "DE89370400440532013012", 100.0);
        String errorMessage = "Insufficient funds";
        Map<String, String> expectedResponse = Map.of("message", errorMessage);

        Mockito.when(transactionService.transferToOtherCustomer(Mockito.any(TransferMoneyPOSTResponse.class)))
                .thenThrow(new Exception(errorMessage));

        mockMvc.perform(post("/transactions/transfers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(transactionDTO))
                        .with(csrf()))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(content().json(objectMapper.writeValueAsString(expectedResponse)));
    }

    @Test
    @WithMockUser(username = "johndoe", password = "123", roles = {"CUSTOMER", "EMPLOYEE"})
    void transferToOtherCustomer_ExceedsDailyLimit() throws Exception {
        TransferMoneyPOSTResponse transactionDTO = new TransferMoneyPOSTResponse("DE89370400440532013000", "DE89370400440532013012", 100.0);
        String errorMessage = "Transaction exceeds daily limit";
        Map<String, String> expectedResponse = Map.of("message", errorMessage);

        Mockito.when(transactionService.transferToOtherCustomer(Mockito.any(TransferMoneyPOSTResponse.class)))
                .thenThrow(new Exception(errorMessage));

        mockMvc.perform(post("/transactions/transfers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(transactionDTO))
                        .with(csrf()))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(content().json(objectMapper.writeValueAsString(expectedResponse)));
    }

    @Test
    @WithMockUser(username = "johndoe", password = "123", roles = {"CUSTOMER", "EMPLOYEE"})
    void transferToOtherCustomer_CheckingAccountsError() throws Exception {
        TransferMoneyPOSTResponse transactionDTO = new TransferMoneyPOSTResponse("DE89370400440532013000", "DE89370400440532013012", 100.0);
        String errorMessage = "Both accounts must be of type CHECKING.";
        Map<String, String> expectedResponse = Map.of("message", errorMessage);

        Mockito.when(transactionService.transferToOtherCustomer(Mockito.any(TransferMoneyPOSTResponse.class)))
                .thenThrow(new Exception(errorMessage));

        mockMvc.perform(post("/transactions/transfers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(transactionDTO))
                        .with(csrf()))
                .andExpect(status().isBadRequest())
                .andExpect(content().json(objectMapper.writeValueAsString(expectedResponse)));
    }

    @Test
    @WithMockUser(username = "johndoe", password = "123", roles = {"CUSTOMER", "EMPLOYEE"})
    void transferToOtherCustomer_GeneralError() throws Exception {
        TransferMoneyPOSTResponse transactionDTO = new TransferMoneyPOSTResponse("DE89370400440532013000", "DE89370400440532013012", 100.0);
        String errorMessage = "General error";
        Map<String, String> expectedResponse = Map.of("message", errorMessage);

        Mockito.when(transactionService.transferToOtherCustomer(Mockito.any(TransferMoneyPOSTResponse.class)))
                .thenThrow(new Exception(errorMessage));

        mockMvc.perform(post("/transactions/transfers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(transactionDTO))
                        .with(csrf()))
                .andExpect(status().isBadRequest())
                .andExpect(content().json(objectMapper.writeValueAsString(expectedResponse)));
    }
}
