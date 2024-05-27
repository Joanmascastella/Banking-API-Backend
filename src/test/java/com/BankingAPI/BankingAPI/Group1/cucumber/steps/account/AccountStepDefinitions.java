package com.BankingAPI.BankingAPI.Group1.cucumber.steps.account;

import com.BankingAPI.BankingAPI.Group1.config.testConfigurations.TestConfig;
import com.BankingAPI.BankingAPI.Group1.cucumber.BaseStepDefinitions;
import com.BankingAPI.BankingAPI.Group1.model.dto.AccountGETPOSTResponseDTO;
import com.BankingAPI.BankingAPI.Group1.model.dto.LoginDTO;
import com.BankingAPI.BankingAPI.Group1.model.dto.TokenDTO;
import com.BankingAPI.BankingAPI.Group1.model.dto.TransferMoneyPOSTResponse;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.jayway.jsonpath.JsonPath;
import io.cucumber.java.Before;
import io.cucumber.java.en.And;
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
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

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

    @Given("The endpoint {string} is available for method {string}")
    public void theEndpointForIsAvailableForMethod(String endpoint, String method) {

        response = restTemplate
                .exchange(testConfig.getBaseUrl() + endpoint,
                        HttpMethod.OPTIONS,
                        new HttpEntity<>(null, httpHeaders),
                        String.class);
        List<String> options = Arrays.stream(response.getHeaders()
                        .get("Allow")
                        .get(0)
                        .split(","))
                .toList();
        Assertions.assertTrue(options.contains(method.toUpperCase()));
    }

    @When("I retrieve all accounts")
    public void iRetrieveAllAccounts() throws JsonProcessingException {

        httpHeaders.clear();
        httpHeaders.add("Authorization", "Bearer " + token);
        httpHeaders.add("Content-Type", "application/json");

        response = restTemplate.exchange(testConfig.getBaseUrl() + "/accounts/customers", HttpMethod.GET, new HttpEntity<>(null, httpHeaders), String.class);
    }


    @Then("I receive http status {int} for get request")
    public void iGetHttpStatus(int status) {
        int actual = response.getStatusCode().value();
        Assertions.assertEquals(status, actual);
    }

    @And("I should receive all accounts as a list of size {int}")
    public void iShouldReceiveAllAccounts(int count) {
        int actual = JsonPath.read(response.getBody(), "$.size()");
        Assertions.assertEquals(count, actual);

    }

    @When("I retrieve accounts by absolute limit {int}")
    public void iRetrieveAccountsByAbsoluteLimit(int limit) throws JsonProcessingException {

        httpHeaders.clear();
        httpHeaders.add("Authorization", "Bearer " + token);
        httpHeaders.add("Content-Type", "application/json");
        response = restTemplate.exchange(UriComponentsBuilder
                    .fromUriString(testConfig.getBaseUrl() + "/accounts/byAbsoluteLimit")
                    .queryParam("absoluteLimit", String.valueOf(limit))
                    .toUriString(),
                    HttpMethod.GET, new HttpEntity<>(null, httpHeaders), String.class);
    }

    @And("I should receive the accounts with absolute limit as a list of size {int}")
    public void iShouldReceiveAccountsWithLimit(int count) {
        int actual = JsonPath.read(response.getBody(), "$.size()");
        Assertions.assertEquals(count, actual);

    }

    @And("The absolute limit of the accounts is less than or equal to {int}")
    public void iShouldReceiveAccountsWithLimitLessThanOrEqualTo(int limit) throws IOException {
        JsonNode res = mapper.readTree(response.getBody());
        ObjectReader reader = mapper.readerFor(new TypeReference<List<AccountGETPOSTResponseDTO>>() {});
        List<AccountGETPOSTResponseDTO> retrievedData = reader.readValue(res);

        Boolean filteredData = retrievedData.stream().allMatch(item -> item.absoluteLimit() <= limit);

        Assertions.assertTrue(filteredData);


    }

    @When("I retrieve inactive accounts")
    public void iRetrieveInactiveAccounts() throws JsonProcessingException {

        httpHeaders.clear();
        httpHeaders.add("Authorization", "Bearer " + token);
        httpHeaders.add("Content-Type", "application/json");

        response = restTemplate.exchange(testConfig.getBaseUrl() + "/accounts/inactive", HttpMethod.GET, new HttpEntity<>(null, httpHeaders), String.class);
    }


    @And("I should receive the inactive accounts as a list of size {int}")
    public void iShouldReceiveInactiveAccounts(int count) {
        int actual = JsonPath.read(response.getBody(), "$.size()");
        Assertions.assertEquals(count, actual);

    }

    @And("Each account has inactive status")
    public void iShouldReceiveAccountsWithStatusInactive() throws IOException {
        JsonNode res = mapper.readTree(response.getBody());
        ObjectReader reader = mapper.readerFor(new TypeReference<List<AccountGETPOSTResponseDTO>>() {});
        List<AccountGETPOSTResponseDTO> retrievedData = reader.readValue(res);

        Boolean filteredData = retrievedData.stream().allMatch(item -> item.isActive() == false);

        Assertions.assertTrue(filteredData);

    }


}
