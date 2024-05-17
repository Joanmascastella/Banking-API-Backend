package com.BankingAPI.BankingAPI.Group1.cucumber.steps.transaction;

import com.BankingAPI.BankingAPI.Group1.config.testConfigurations.TestConfig;
import com.BankingAPI.BankingAPI.Group1.cucumber.BaseStepDefinitions;
import com.BankingAPI.BankingAPI.Group1.model.dto.LoginDTO;
import com.BankingAPI.BankingAPI.Group1.model.dto.TokenDTO;
import com.BankingAPI.BankingAPI.Group1.model.dto.TransferMoneyPOSTResponse;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
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
public class TransactionStepDefinitions extends BaseStepDefinitions {
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

    @Given("I log in as user with valid accounts for transactions")
    public void iLogInAsUserWithValidAccountsForTransactions() throws JsonProcessingException {
        httpHeaders.clear();
        httpHeaders.add("Content-Type", "application/json");
        LoginDTO loginDTO = new LoginDTO(VALID_USER, VALID_USER_PASSWORD);
        token = getToken(loginDTO);
    }

    @When("I transfer money {double} from account {string} to account {string}")
    public void iTransferMoneyToOtherCustomer(double amount, String fromAccount, String toAccount) throws JsonProcessingException {
        httpHeaders.clear();
        httpHeaders.add("Authorization", "Bearer " + token);
        httpHeaders.add("Content-Type", "application/json");

        TransferMoneyPOSTResponse transactionDTO = new TransferMoneyPOSTResponse(fromAccount, toAccount, amount);
        response = restTemplate.exchange(
                testConfig.getBaseUrl() + "/transactions/transfers",
                HttpMethod.POST,
                new HttpEntity<>(mapper.writeValueAsString(transactionDTO), httpHeaders),
                String.class
        );
    }

    @Then("I get transaction transfer http status {int}")
    public void iGetTransactionTransferHttpStatus(int status) {
        int actual = response.getStatusCode().value();
        Assertions.assertEquals(status, actual);
    }

    @Then("I get transaction transfer message {string}")
    public void iGetTransactionTransferMessage(String expectedMessage) throws JsonProcessingException {
        String responseBody = response.getBody();

        try {
            Map<String, Object> responseMap = mapper.readValue(responseBody, new TypeReference<Map<String, Object>>() {});
            String actualMessage = (String) responseMap.get("message");
            Assertions.assertTrue(actualMessage.contains(expectedMessage), "Expected message: " + expectedMessage + ", but got: " + actualMessage);
        } catch (JsonProcessingException e) {
            Assertions.assertTrue(responseBody.contains(expectedMessage), "Expected message: " + expectedMessage + ", but got: " + responseBody);
        }
    }



}
