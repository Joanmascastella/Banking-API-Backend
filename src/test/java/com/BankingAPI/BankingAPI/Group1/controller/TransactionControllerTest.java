package com.BankingAPI.BankingAPI.Group1.controller;

import com.BankingAPI.BankingAPI.Group1.config.ApiTestConfiguration;
import com.BankingAPI.BankingAPI.Group1.model.Enums.UserType;
import com.BankingAPI.BankingAPI.Group1.model.Transaction;
import com.BankingAPI.BankingAPI.Group1.model.Users;
import com.BankingAPI.BankingAPI.Group1.model.dto.TransactionGETPOSTResponseDTO;
import com.BankingAPI.BankingAPI.Group1.model.dto.TransferMoneyPOSTResponse;
import com.BankingAPI.BankingAPI.Group1.service.AccountService;
import com.BankingAPI.BankingAPI.Group1.service.TransactionService;
import com.fasterxml.jackson.databind.ObjectMapper;
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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.BDDMockito.given;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

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
    @MockBean
    private BCryptPasswordEncoder bCryptPasswordEncoder;
    private TransactionGETPOSTResponseDTO ATMDeposit;
    private TransactionGETPOSTResponseDTO ATMWithdrawal;
    private TransactionGETPOSTResponseDTO onlineTransferByCustomer;
    private TransactionGETPOSTResponseDTO onlineTransferByEmployee;

    private Users customer;

    private Users employee;

    private List<TransactionGETPOSTResponseDTO> transactions;



    @BeforeEach
    public void setup() {
        customer = new Users("johndoe", "john.doe@example.com", "John", "Doe", "123456789", "0123456789", LocalDate.of(1990, 1, 1), 5000.0, 1000.0, true, true, UserType.ROLE_CUSTOMER, bCryptPasswordEncoder.encode("123"));
        employee = new Users("Employee", "employee@example.com", "Em", "Yee", "1234567893", "01234567891", LocalDate.of(1990, 1, 1), 5000.0, 1000.0, true, true, UserType.ROLE_EMPLOYEE, bCryptPasswordEncoder.encode("employee"));
        ATMDeposit = new TransactionGETPOSTResponseDTO("123456789", "ATM", 1500.0, LocalDate.now(), customer.getId());
        ATMWithdrawal = new TransactionGETPOSTResponseDTO("ATM", "123456789", 2000.0, LocalDate.now(), customer.getId());
        onlineTransferByCustomer = new TransactionGETPOSTResponseDTO("123456789", "123456817", 2800.0, LocalDate.now(), customer.getId());
        onlineTransferByEmployee = new TransactionGETPOSTResponseDTO("123456789", "123456817", 2800.0, LocalDate.now(), employee.getId());
        transactions = new ArrayList<>();
        transactions.add(ATMDeposit);
        transactions.add(ATMWithdrawal);
        transactions.add(onlineTransferByCustomer);
        transactions.add(onlineTransferByEmployee);



    }


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
    @Test
    @WithMockUser(username = "johndoe", roles = {"CUSTOMER"})
    void getTransactionsByUserId_ShouldReturnTransactions() throws Exception {
        Long userId = 1L;
        given(transactionService.getTransactionsByUserId(userId)).willReturn(transactions);

        this.mockMvc.perform(get("/transactions/{userId}/history", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(transactions.size())))
                .andExpect(jsonPath("$[0].amount").value(transactions.get(0).amount()))
                .andExpect(jsonPath("$[1].amount").value(transactions.get(1).amount()));
    }

    @Test
    @WithMockUser(username = "johndoe", roles = {"CUSTOMER"})
    void getTransactionsByUserId_ShouldReturnBadRequestOnError() throws Exception {
        Long userId = 1L;
        String errorMessage = "Error retrieving transactions for user: " + userId;
        given(transactionService.getTransactionsByUserId(userId)).willThrow(new RuntimeException(errorMessage));

        this.mockMvc.perform(get("/transactions/{userId}/history", userId))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(errorMessage));
    }
    @Test
    @WithMockUser(username = "johndoe", roles = {"CUSTOMER"})
    void searchTransactions_ShouldReturnFilteredTransactions() throws Exception {
        String IBAN = "123456789";
        Double amount = 100.0;
        LocalDate startDate = LocalDate.of(2023, 1, 1);
        LocalDate endDate = LocalDate.of(2023, 12, 31);
        Double amountGreater = 50.0;
        Double amountLess = 200.0;

        List<Transaction> filteredTransactions = Arrays.asList(
                new Transaction(customer, "123456789", "987654321", 100.0, LocalDate.of(2023, 5, 1))
        );

        List<TransactionGETPOSTResponseDTO> transactionDto = filteredTransactions.stream()
                .map(t -> new TransactionGETPOSTResponseDTO(t.getFromAccount(), t.getToAccount(), t.getAmount(), t.getDate(), t.getUser().getId()))
                .collect(Collectors.toList());

        given(transactionService.filterTransactions(IBAN, amount, amountGreater, amountLess, startDate, endDate)).willReturn(filteredTransactions);

        this.mockMvc.perform(get("/transactions/search")
                        .param("IBAN", IBAN)
                        .param("amount", amount.toString())
                        .param("startDate", startDate.toString())
                        .param("endDate", endDate.toString())
                        .param("amountGreater", amountGreater.toString())
                        .param("amountLess", amountLess.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(transactionDto.size())))
                .andExpect(jsonPath("$[0].amount").value(transactionDto.get(0).amount()));
    }


    @Test
    @WithMockUser(username = "Employee", password = "employee", roles = "EMPLOYEE")
    public void getAllTransactionsShouldReturnTransactions() throws Exception {
        given(transactionService.findAllTransactions()).willReturn(transactions);
        this.mockMvc.perform(get("/transactions")).andExpect(
                        status().isOk())
                .andExpect(jsonPath("$", hasSize(4)))
                .andExpect(jsonPath("$[0].amount").value(transactions.get(0).amount()));
    }


    @Test
    @WithMockUser(username = "Employee", password = "employee", roles = "EMPLOYEE")
    public void getCustomerTransactionsByEmployeeShouldReturnFilteredTransactions() throws Exception {
        given(transactionService.getTransactionsByUserId(1L)).willReturn(Arrays.asList(ATMDeposit));
        this.mockMvc.perform(get("/transactions/customer/{userId}",1L)).andExpect(
                        status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].amount").value(ATMDeposit.amount()));

    }


    @Test
    @WithMockUser(username = "Employee", password = "employee", roles = "EMPLOYEE")
    public void getATMTransactionsShouldReturnFilteredTransactions() throws Exception {
        given(transactionService.findATMTransactions()).willReturn(Arrays.asList(ATMDeposit));
        this.mockMvc.perform(get("/transactions/ATM")).andExpect(
                        status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].amount").value(ATMDeposit.amount()));

    }


    @Test
    @WithMockUser(username = "Employee", password = "employee", roles = "EMPLOYEE")
    public void getATMDepositsOfUserShouldReturnFilteredTransactions() throws Exception {
        given(transactionService.findATMDepositsByUserId(1L)).willReturn(Arrays.asList(ATMDeposit));
        this.mockMvc.perform(get("/transactions/ATM/deposits/{userId}", 1L)).andExpect(
                        status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].fromAccount").value(ATMDeposit.fromAccount()));

    }


    @Test
    @WithMockUser(username = "Employee", password = "employee", roles = "EMPLOYEE")
    public void getATMWithdrawalsOfUserShouldReturnFilteredTransactions() throws Exception {
        given(transactionService.findATMWithdrawalsByUserId(1L)).willReturn(Arrays.asList(ATMWithdrawal));
        this.mockMvc.perform(get("/transactions/ATM/withdrawals/{userId}", 1L)).andExpect(
                        status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].fromAccount").value(ATMWithdrawal.fromAccount()));
    }



    @Test
    @WithMockUser(username = "Employee", password = "employee", roles = "EMPLOYEE")
    public void getTransactionsByCustomersShouldReturnFilteredTransactions() throws Exception {
        given(transactionService.findTransactionsInitializedByCustomers()).willReturn(Arrays.asList(ATMDeposit));
        this.mockMvc.perform(get("/transactions/byCustomers")).andExpect(
                        status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].amount").value(ATMDeposit.amount()));

    }


    @Test
    @WithMockUser(username = "Employee", password = "employee", roles = "EMPLOYEE")
    public void getTransactionsByEmployeesShouldReturnFilteredTransactions() throws Exception {
        given(transactionService.findTransactionsInitializedByEmployees()).willReturn(Arrays.asList(onlineTransferByEmployee));
        this.mockMvc.perform(get("/transactions/byEmployees")).andExpect(
                        status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].amount").value(onlineTransferByEmployee.amount()));

    }


    @Test
    @WithMockUser(username = "Employee", password = "employee", roles = "EMPLOYEE")
    public void getOnlineTransactionsShouldReturnFilteredTransactions() throws Exception {
        given(transactionService.findOnlineTransactions()).willReturn(Arrays.asList(onlineTransferByCustomer));
        this.mockMvc.perform(get("/transactions/online")).andExpect(
                        status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].amount").value(onlineTransferByCustomer.amount()));

    }


    @Test
    @WithMockUser(username = "Employee", password = "employee", roles = "EMPLOYEE")
    public void getOnlineTransactionsOfUserShouldReturnFilteredTransactions() throws Exception {
        given(transactionService.findOnlineTransactionsByUserId(1L)).willReturn(Arrays.asList(onlineTransferByCustomer));
        this.mockMvc.perform(get("/transactions/online/{userId}", 1L)).andExpect(
                        status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].amount").value(onlineTransferByCustomer.amount()));
    }


    @Test
    @WithMockUser(username = "Employee", password = "employee", roles = "EMPLOYEE")
    public void getOnlineTransactionsByCustomersShouldReturnFilteredTransactions() throws Exception {
        given(transactionService.findOnlineTransactionsByCustomers()).willReturn(Arrays.asList(onlineTransferByCustomer));
        this.mockMvc.perform(get("/transactions/online/byCustomers")).andExpect(
                        status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].amount").value(onlineTransferByCustomer.amount()));

    }


    @Test
    @WithMockUser(username = "Employee", password = "employee", roles = "EMPLOYEE")
    public void getOnlineTransactionsByEmployeesShouldReturnFilteredTransactions() throws Exception {
        given(transactionService.findOnlineTransactionsByEmployees()).willReturn(Arrays.asList(onlineTransferByEmployee));
        this.mockMvc.perform(get("/transactions/online/byEmployees")).andExpect(
                        status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].amount").value(onlineTransferByEmployee.amount()));
    }





}



