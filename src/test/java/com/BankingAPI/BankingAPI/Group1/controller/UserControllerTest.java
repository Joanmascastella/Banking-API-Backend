package com.BankingAPI.BankingAPI.Group1.controller;

import com.BankingAPI.BankingAPI.Group1.config.ApiTestConfiguration;
import com.BankingAPI.BankingAPI.Group1.model.Enums.AccountType;
import com.BankingAPI.BankingAPI.Group1.model.dto.AccountDetailsGETResponse;
import com.BankingAPI.BankingAPI.Group1.model.dto.FindIbanRequestDTO;
import com.BankingAPI.BankingAPI.Group1.model.dto.FindIbanResponseDTO;
import com.BankingAPI.BankingAPI.Group1.service.UserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
@WebMvcTest(UserController.class)
@Import(ApiTestConfiguration.class)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;


    @Test
    @WithMockUser(username = "johndoe", password = "123", roles = "CUSTOMER")
    void getIbanByFirstNameLastName() throws Exception {
        String firstName = "Joan";
        String lastName = "Doe";
        FindIbanResponseDTO responseDTO = new FindIbanResponseDTO("DE89370400440532013022");

        Mockito.when(userService.getIbanByFirstNameLastName(Mockito.any(FindIbanRequestDTO.class)))
                .thenReturn(responseDTO);

        mockMvc.perform(get("/users/iban")
                        .param("firstName", firstName)
                        .param("lastName", lastName))
                .andExpect(status().isOk())
                .andExpect(content().json("{\"iban\":\"DE89370400440532013022\"}"));
    }

    @Test
    @WithMockUser(username = "johndoe", password = "123", roles = "CUSTOMER")
    void getIbanByFirstNameLastName_NotFound() throws Exception {
        String firstName = "Jane";
        String lastName = "Doe";

        Mockito.when(userService.getIbanByFirstNameLastName(Mockito.any(FindIbanRequestDTO.class)))
                .thenReturn(null);

        mockMvc.perform(get("/users/iban")
                        .param("firstName", firstName)
                        .param("lastName", lastName))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(username = "johndoe", password = "123", roles = "CUSTOMER")
    void getUserDetails() throws Exception {
        Mockito.when(userService.getAccountDetailsForCurrentUser())
                .thenReturn(List.of(
                        new AccountDetailsGETResponse("johndoe", "john.doe@example.com", "John", "Doe", "123456789", "0123456789", LocalDate.of(1990, 1, 1),"DE89370400440532013000", "EUR", AccountType.CHECKING, 5000.0, 0.00),
                        new AccountDetailsGETResponse("johndoe", "john.doe@example.com", "John", "Doe", "123456789", "0123456789", LocalDate.of(1990, 1, 1),"DE89370400440532013012", "EUR", AccountType.SAVINGS, 5000.0, 0.00)));

        mockMvc.perform(get("/users/details"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "janedoe", password = "user", roles = "CUSTOMER")
    void getUserDetails_NotFound() throws Exception {
        Mockito.when(userService.getAccountDetailsForCurrentUser())
                .thenReturn(new ArrayList<>());

        mockMvc.perform(get("/users/details"))
                .andExpect(status().isNotFound())
                .andExpect(content().string("No accounts found for user."));
    }


    @Test
    @WithMockUser(username = "johndoe", password = "123", roles = "CUSTOMER")
    void getUserDetails_InternalServerError() throws Exception {
        Mockito.when(userService.getAccountDetailsForCurrentUser())
                .thenThrow(new RuntimeException("Unexpected error"));

        mockMvc.perform(get("/users/details"))
                .andExpect(status().isInternalServerError())
                .andExpect(content().string("An internal error occurred: Unexpected error"));
    }



}