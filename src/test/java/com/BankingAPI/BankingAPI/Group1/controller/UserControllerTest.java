package com.BankingAPI.BankingAPI.Group1.controller;

import com.BankingAPI.BankingAPI.Group1.config.ApiTestConfiguration;
import com.BankingAPI.BankingAPI.Group1.exception.*;
import com.BankingAPI.BankingAPI.Group1.model.Enums.AccountType;
import com.BankingAPI.BankingAPI.Group1.model.Enums.UserType;
import com.BankingAPI.BankingAPI.Group1.model.Users;
import com.BankingAPI.BankingAPI.Group1.model.dto.*;
import com.BankingAPI.BankingAPI.Group1.service.UserService;
import io.cucumber.core.internal.com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityNotFoundException;
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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

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

        Mockito.when(userService.getIbanByFirstNameLastName(any(FindIbanRequestDTO.class)))
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

        Mockito.when(userService.getIbanByFirstNameLastName(any(FindIbanRequestDTO.class)))
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

    @Test
    @WithMockUser(username = "Employee", password = "employee", roles = "EMPLOYEE")
    void getAllUsers() throws Exception {
        Mockito.when(userService.getAllUsers()).thenReturn(
                List.of(new UserGETResponseDTO(2, "janedoe", "jane@doe.com", "jane", "doe", "12345678", "0123456789", LocalDate.of(1998, 4, 14), 0, 0.0, false, true, UserType.ROLE_CUSTOMER),
                        new UserGETResponseDTO(5, "sara", "sara@doe.com", "sara", "doe", "12345678", "0123456789", LocalDate.of(2000, 12, 4), 0, 0.0, true, true, UserType.ROLE_CUSTOMER)));
        mockMvc.perform(get("/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id").value(2))
                .andExpect(jsonPath("$[0].username").value("janedoe"))
                .andExpect(jsonPath("$[0].email").value("jane@doe.com"))
                .andExpect(jsonPath("$[1].id").value(5))
                .andExpect(jsonPath("$[1].username").value("sara"))
                .andExpect(jsonPath("$[1].email").value("sara@doe.com"));
    }

    @Test
    @WithMockUser(username = "employee", roles = "EMPLOYEE")
    void getAllUsers_NoneFound() throws Exception {
        Mockito.when(userService.getAllUsers())
                .thenReturn(Collections.emptyList());

        mockMvc.perform(get("/users"))
                .andExpect(status().isOk())
                .andExpect(content().string("[]"));
    }

    @Test
    @WithMockUser(username = "Employee", password = "employee", roles = "EMPLOYEE")
    void getUnapprovedUsers() throws Exception {
        Mockito.when(userService.getUnapprovedUsers()).thenReturn(
                List.of(new UserGETResponseDTO(2, "janedoe", "jane@doe.com", "jane", "doe", "12345678", "0123456789", LocalDate.of(1998, 4, 14), 0, 0.0, false, true, UserType.ROLE_CUSTOMER),
                        new UserGETResponseDTO(5, "sara", "sara@doe.com", "sara", "doe", "12345678", "0123456789", LocalDate.of(2000, 12, 4), 0, 0.0, false, true, UserType.ROLE_CUSTOMER)));
        mockMvc.perform(get("/users/noncustomers"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id").value(2))
                .andExpect(jsonPath("$[0].username").value("janedoe"))
                .andExpect(jsonPath("$[0].email").value("jane@doe.com"))
                .andExpect(jsonPath("$[0].isApproved").value(false))
                .andExpect(jsonPath("$[1].id").value(5))
                .andExpect(jsonPath("$[1].username").value("sara"))
                .andExpect(jsonPath("$[1].email").value("sara@doe.com"))
                .andExpect(jsonPath("$[0].isApproved").value(false));
    }

    @Test
    @WithMockUser(username = "employee", roles = "EMPLOYEE")
    void getUnapprovedUsers_NoneFound() throws Exception {
        Mockito.when(userService.getUnapprovedUsers()).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/users/noncustomers"))
                .andExpect(status().isOk())
                .andExpect(content().string("[]"));
    }

    @Test
    @WithMockUser(username = "Employee", password = "employee", roles = "EMPLOYEE")
    void approveUser() throws Exception {
        Long userId = 2L;
        UserApprovalDTO approvalDTO = new UserApprovalDTO(100.0, 0.0, -200.0);

        Mockito.doNothing().when(userService).approveUser(eq(userId), any(UserApprovalDTO.class));

        mockMvc.perform(put("/users/" + userId + "/approve")
                        .contentType("application/json")
                        .content("{\"dailyLimit\":100,\"transactionLimit\":0,\"balance\":-200}")
                .with(csrf()))
                .andExpect(status().isOk());
    }
    @Test
    @WithMockUser(username = "Employee", password = "employee", roles = "EMPLOYEE")
    void approveUser_UserNotFound() throws Exception {
        Long userId = 2L;
        UserApprovalDTO approvalDTO = new UserApprovalDTO(100.0, 0.0, -200.0);

        // Simulate user not found scenario
        Mockito.doThrow(EntityNotFoundException.class)
                .when(userService).approveUser(eq(userId), any(UserApprovalDTO.class));

        mockMvc.perform(put("/users/" + userId + "/approve")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"dailyLimit\":100,\"transactionLimit\":0,\"balance\":-200}")
                        .with(csrf()))
                .andExpect(status().isNotFound());

        Mockito.verify(userService, Mockito.times(1))
                .approveUser(eq(userId), any(UserApprovalDTO.class));
    }

    @Test
    @WithMockUser(username = "Employee", password = "employee", roles = "EMPLOYEE")
    void approveUser_InvalidDailyLimit() throws Exception {
        Long userId = 2L;
        UserApprovalDTO approvalDTO = new UserApprovalDTO(100.0, 0.0, -200.0);

        // Simulate runtime exception scenario
        Mockito.doThrow(InvalidLimitException.class)
                .when(userService).approveUser(eq(userId), any(UserApprovalDTO.class));

        mockMvc.perform(put("/users/" + userId + "/approve")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"dailyLimit\":100,\"transactionLimit\":0,\"balance\":-200}")
                        .with(csrf()))
                .andExpect(status().isUnprocessableEntity());

        Mockito.verify(userService, Mockito.times(1))
                .approveUser(eq(userId), any(UserApprovalDTO.class));
    }

    @Test
    @WithMockUser(username = "Employee", password = "employee", roles = "EMPLOYEE")
    void approveUser_AlreadyApproved() throws Exception {
        Long userId = 2L;
        UserApprovalDTO approvalDTO = new UserApprovalDTO(100.0, 0.0, -200.0);

        // Simulate runtime exception scenario
        Mockito.doThrow(UserAlreadyApprovedException.class)
                .when(userService).approveUser(eq(userId), any(UserApprovalDTO.class));

        mockMvc.perform(put("/users/" + userId + "/approve")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"dailyLimit\":100,\"transactionLimit\":0,\"balance\":-200}")
                        .with(csrf()))
                .andExpect(status().isConflict());

        Mockito.verify(userService, Mockito.times(1))
                .approveUser(eq(userId), any(UserApprovalDTO.class));
    }

    @Test
    @WithMockUser(username = "Employee", password = "employee", roles = "EMPLOYEE")
    void approveUser_IBANException() throws Exception {
        Long userId = 2L;
        UserApprovalDTO approvalDTO = new UserApprovalDTO(100.0, 0.0, -200.0);

        // Simulate runtime exception scenario
        Mockito.doThrow(IBANGenerationException.class)
                .when(userService).approveUser(eq(userId), any(UserApprovalDTO.class));

        mockMvc.perform(put("/users/" + userId + "/approve")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"dailyLimit\":100,\"transactionLimit\":0,\"balance\":-200}")
                        .with(csrf()))
                .andExpect(status().isInternalServerError());

        Mockito.verify(userService, Mockito.times(1))
                .approveUser(eq(userId), any(UserApprovalDTO.class));
    }
    @Test
    @WithMockUser(username = "Employee", password = "employee", roles = "EMPLOYEE")
    void approveUser_RuntimeException() throws Exception {
        Long userId = 2L;
        UserApprovalDTO approvalDTO = new UserApprovalDTO(100.0, 0.0, -200.0);

        // Simulate runtime exception scenario
        Mockito.doThrow(RuntimeException.class)
                .when(userService).approveUser(eq(userId), any(UserApprovalDTO.class));

        mockMvc.perform(put("/users/" + userId + "/approve")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"dailyLimit\":100,\"transactionLimit\":0,\"balance\":-200}")
                        .with(csrf()))
                .andExpect(status().isInternalServerError());

        Mockito.verify(userService, Mockito.times(1))
                .approveUser(eq(userId), any(UserApprovalDTO.class));
    }

    @Test
    @WithMockUser(username = "Employee", password = "employee", roles = "EMPLOYEE")
    void updateDailyLimit() throws Exception {
        Long userId = 4L;
        UserGETResponseDTO user = new UserGETResponseDTO(4,"joan", "joan.doe@example.com", "Joan", "Doe", "12345673", "0123456789", LocalDate.of(1990, 1, 1), 5000.0, 1000.0, true, true, UserType.ROLE_CUSTOMER);
        Mockito.doNothing().when(userService).updateDailyLimit(userId, user);

        mockMvc.perform(put("/users/" + userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"joan\",\"email\":\"joan.doe@example.com\",\"firstName\":\"Joan\",\"lastName\":\"Doe\",\"BSN\":\"12345673\",\"phoneNumber\":\"0123456789\",\"dateOfBirth\":\"1990-01-01\",\"balance\":5000.0,\"dailyLimit\":1000.0,\"isApproved\":true,\"isActive\":true,\"userType\":\"ROLE_CUSTOMER\"}")
                .with(csrf()))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "Employee", roles = "EMPLOYEE")
    public void testUpdateDailyLimit_UserNotFound() throws Exception {
        Users user = new Users();
        user.setId(4L);
        user.setDailyLimit(100.0);

        Mockito.doThrow(new EntityNotFoundException("User not found with id: 4")).when(userService).updateDailyLimit(eq(user.getId()), any(UserGETResponseDTO.class));

        mockMvc.perform(put("/users/" + user.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(user))
                        .with(csrf()))
                .andExpect(status().isNotFound())
                .andExpect(content().string("User not found with id: 4"));
    }

    @Test
    @WithMockUser(username = "Employee", roles = "EMPLOYEE")
    public void testUpdateDailyLimit_InvalidDailyLimit() throws Exception {
        Users user = new Users();
        user.setId(4L);
        user.setDailyLimit(100.0);

        Mockito.doThrow(new InvalidLimitException("The daily limit can't be set to a negative amount.")).when(userService).updateDailyLimit(eq(user.getId()), any(UserGETResponseDTO.class));

        mockMvc.perform(put("/users/" + user.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(user))
                        .with(csrf()))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(content().string("The daily limit can't be set to a negative amount."));
    }

    @Test
    @WithMockUser(username = "Employee", password = "employee", roles = "EMPLOYEE")
    void closeAccount() throws Exception {
        long userId = 1L;
        Mockito.doNothing().when(userService).closeAccount(userId);

        mockMvc.perform(delete("/users/{userId}", userId)
                .with(csrf()))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "Employee", password = "employee", roles = "EMPLOYEE")
    void closeAccount_UserNotFound() throws Exception {
        long userId = 1L;
        Mockito.doThrow(new EntityNotFoundException()).when(userService).closeAccount(userId);

        mockMvc.perform(delete("/users/{userId}", userId)
                        .with(csrf()))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(username = "Employee", password = "employee", roles = "EMPLOYEE")
    void closeAccount_InactiveUser() throws Exception {
        long userId = 1L;
        Mockito.doThrow(new InactiveUserException("This account can't be closed.")).when(userService).closeAccount(userId);

        mockMvc.perform(delete("/users/{userId}", userId)
                        .with(csrf()))
                .andExpect(status().isUnprocessableEntity());
    }

    @Test
    @WithMockUser(username = "Employee", password = "employee", roles = "EMPLOYEE")
    void closeAccount_Unauthorized() throws Exception {
        long userId = 1L;
        Mockito.doThrow(new UnauthorizedException("Employee accounts cannot be closed.")).when(userService).closeAccount(userId);

        mockMvc.perform(delete("/users/{userId}", userId)
                        .with(csrf()))
                .andExpect(status().isUnauthorized());
    }
}