package com.BankingAPI.BankingAPI.Group1.cucumber.steps.account;

import com.BankingAPI.BankingAPI.Group1.config.testConfigurations.TestConfig;
import com.BankingAPI.BankingAPI.Group1.cucumber.BaseStepDefinitions;
import com.BankingAPI.BankingAPI.Group1.model.dto.LoginDTO;
import com.BankingAPI.BankingAPI.Group1.model.dto.TokenDTO;
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

import java.util.Map;

@Log
public class AccountStepDefinitions extends BaseStepDefinitions {
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
    private String getToken(LoginDTO loginDTO) throws JsonProcessingException {
        response = restTemplate.exchange(
                testConfig.getBaseUrl() + "/login",
                HttpMethod.POST,
                new HttpEntity<>(mapper.writeValueAsString(loginDTO), httpHeaders),
                String.class);
        TokenDTO tokenDTO = mapper.readValue(response.getBody(), TokenDTO.class);
        return tokenDTO.token();
    }

    @Given("I log in as user with valid accounts")
    public void iLogInAsUserWithValidAccounts() throws JsonProcessingException {
        httpHeaders.clear();
        httpHeaders.add("Content-Type", "application/json");
        LoginDTO loginDTO = new LoginDTO(VALID_USER, VALID_USER_PASSWORD);
        token = getToken(loginDTO);
    }

    @When("I transfer {double} from account {string} to account {string}")
    public void iTransferAmountToOwnAccount(double amount, String fromAccount, String toAccount) throws JsonProcessingException {
        httpHeaders.clear();
        httpHeaders.add("Authorization", "Bearer " + token);
        httpHeaders.add("Content-Type", "application/json");

        TransferMoneyPOSTResponse transactionDTO = new TransferMoneyPOSTResponse(fromAccount, toAccount, amount);
        response = restTemplate.exchange(
                testConfig.getBaseUrl() + "/accounts/own/transfers",
                HttpMethod.POST,
                new HttpEntity<>(mapper.writeValueAsString(transactionDTO), httpHeaders),
                String.class
        );
    }

    @Then("I get transfer http status {int}")
    public void iGetTransferHttpStatus(int status) {
        int actual = response.getStatusCode().value();
        Assertions.assertEquals(status, actual);
    }

    @Then("I get transfer message {string}")
    public void iGetTransferMessage(String message) {
        String actualMessage = response.getBody();
        Assertions.assertTrue(actualMessage.contains(message), "Expected message: " + message + ", but got: " + actualMessage);
    }
}
