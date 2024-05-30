package com.BankingAPI.BankingAPI.Group1.cucumber.steps.atm;

import com.BankingAPI.BankingAPI.Group1.config.testConfigurations.TestConfig;
import com.BankingAPI.BankingAPI.Group1.cucumber.BaseStepDefinitions;
import com.BankingAPI.BankingAPI.Group1.model.dto.ATMLoginDTO;
import com.BankingAPI.BankingAPI.Group1.model.dto.TokenDTO;
import com.BankingAPI.BankingAPI.Group1.model.dto.TransactionGETPOSTResponseDTO;
import com.BankingAPI.BankingAPI.Group1.model.dto.TransferMoneyPOSTResponse;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.cucumber.java.Before;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import lombok.SneakyThrows;
import lombok.extern.java.Log;
import org.junit.jupiter.api.Assertions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;

@Log
public class ATMStepDefinitions extends BaseStepDefinitions {

    HttpHeaders httpHeaders = new HttpHeaders();

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private ObjectMapper mapper;

    @Autowired
    private TestConfig testConfig;

    private ResponseEntity<String> response;
    private String token;

    @SneakyThrows
    @Before
    public void init() {
        log.info("Initialized step definitions");
    }

    private String getToken(ATMLoginDTO loginDTO) throws JsonProcessingException {
        response = restTemplate.exchange(
                testConfig.getBaseUrl() + "/atm/login",
                HttpMethod.POST,
                new HttpEntity<>(mapper.writeValueAsString(loginDTO), httpHeaders),
                String.class);
        TokenDTO tokenDTO = mapper.readValue(response.getBody(), TokenDTO.class);
        return tokenDTO.token();
    }

    @Given("I log in to the ATM as user with valid credentials")
    public void iLogInToTheATMAsUserWithValidCredentials() throws JsonProcessingException {
        httpHeaders.clear();
        httpHeaders.add("Content-Type", "application/json");
        ATMLoginDTO loginDTO = new ATMLoginDTO(VALID_USER_EMAIL, VALID_USER_PASSWORD);
        token = getToken(loginDTO);
    }

    @When("I perform an ATM deposit of {double} to account {string}")
    public void iPerformATMDeposit(double amount, String toAccount) throws JsonProcessingException {
        httpHeaders.clear();
        httpHeaders.add("Authorization", "Bearer " + token);
        httpHeaders.add("Content-Type", "application/json");

        TransferMoneyPOSTResponse transactionDTO = new TransferMoneyPOSTResponse("ATM", toAccount, amount);
        response = restTemplate.exchange(
                testConfig.getBaseUrl() + "/atm/deposits",
                HttpMethod.POST,
                new HttpEntity<>(mapper.writeValueAsString(transactionDTO), httpHeaders),
                String.class
        );
    }

    @When("I perform an ATM withdrawal of {double} from account {string}")
    public void iPerformATMWithdrawal(double amount, String fromAccount) throws JsonProcessingException {
        httpHeaders.clear();
        httpHeaders.add("Authorization", "Bearer " + token);
        httpHeaders.add("Content-Type", "application/json");

        TransferMoneyPOSTResponse transactionDTO = new TransferMoneyPOSTResponse(fromAccount, "ATM", amount);
        response = restTemplate.exchange(
                testConfig.getBaseUrl() + "/atm/withdrawals",
                HttpMethod.POST,
                new HttpEntity<>(mapper.writeValueAsString(transactionDTO), httpHeaders),
                String.class
        );
    }

    @Then("I get ATM transaction http status {int}")
    public void iGetATMTransactionHttpStatus(int status) {
        int actual = response.getStatusCode().value();
        Assertions.assertEquals(status, actual);
    }

    @Then("the ATM deposit to account {string} is successful")
    public void theATMDepositToAccountIsSuccessful(String toAccount) throws JsonProcessingException {
        String responseBody = response.getBody();
        Assertions.assertNotNull(responseBody, "Response body is null");

        // Parse the response body to get the transaction details
        TransactionGETPOSTResponseDTO transactionResponse = mapper.readValue(responseBody, TransactionGETPOSTResponseDTO.class);

        // Check the transaction details
        Assertions.assertEquals("ATM", transactionResponse.fromAccount(), "From account mismatch");
        Assertions.assertEquals(toAccount, transactionResponse.toAccount(), "To account mismatch");
        Assertions.assertNotNull(transactionResponse.amount(), "Amount is null");
        Assertions.assertNotNull(transactionResponse.date(), "Date is null");
        Assertions.assertNotNull(transactionResponse.userId(), "User ID is null");
    }
    @Then("the ATM withdrawal from account {string} is successful")
    public void theATMWithdrawalFromAccountIsSuccessful(String fromAccount) throws JsonProcessingException {
        String responseBody = response.getBody();
        Assertions.assertNotNull(responseBody, "Response body is null");

        // Parse the response body to get the transaction details
        TransactionGETPOSTResponseDTO transactionResponse = mapper.readValue(responseBody, TransactionGETPOSTResponseDTO.class);

        // Check the transaction details
        Assertions.assertEquals(fromAccount, transactionResponse.fromAccount(), "From account mismatch");
        Assertions.assertEquals("ATM", transactionResponse.toAccount(), "To account mismatch");
        Assertions.assertNotNull(transactionResponse.amount(), "Amount is null");
        Assertions.assertNotNull(transactionResponse.date(), "Date is null");
        Assertions.assertNotNull(transactionResponse.userId(), "User ID is null");
    }



}
