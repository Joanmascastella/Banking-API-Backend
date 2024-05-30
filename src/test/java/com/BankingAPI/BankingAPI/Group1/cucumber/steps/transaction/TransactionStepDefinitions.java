package com.BankingAPI.BankingAPI.Group1.cucumber.steps.transaction;

import com.BankingAPI.BankingAPI.Group1.config.testConfigurations.TestConfig;
import com.BankingAPI.BankingAPI.Group1.cucumber.BaseStepDefinitions;
import com.BankingAPI.BankingAPI.Group1.model.dto.*;
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
    @When("I retrieve transaction history for user {long}")
    public void iRetrieveTransactionHistory(long userId) throws JsonProcessingException {
        httpHeaders.clear();
        httpHeaders.add("Authorization", "Bearer " + token);
        httpHeaders.add("Content-Type", "application/json");

        response = restTemplate.exchange(
                testConfig.getBaseUrl() + "/transactions/" + userId + "/history",
                HttpMethod.GET,
                new HttpEntity<>(null, httpHeaders),
                String.class
        );
    }
    @Then("I receive transaction history http status {int}")
    public void iReceiveTransactionHistoryHttpStatus(int status) {
        int actual = response.getStatusCode().value();
        Assertions.assertEquals(status, actual);
    }

    @And("I should receive transaction history as a list of size {int}")
    public void iShouldReceiveTransactionHistory(int count) {
        int actual = JsonPath.read(response.getBody(), "$.size()");
        Assertions.assertEquals(count, actual);
    }

    @When("I search transactions with criteria IBAN {string}, amount {double}, amountGreater {double}, amountLess {double}, startDate {string}, endDate {string}")
    public void iSearchTransactions(String IBAN, Double amount, Double amountGreater, Double amountLess, String startDate, String endDate) throws JsonProcessingException {
        httpHeaders.clear();
        httpHeaders.add("Authorization", "Bearer " + token);
        httpHeaders.add("Content-Type", "application/json");

        UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromUriString(testConfig.getBaseUrl() + "/transactions/search")
                .queryParam("IBAN", IBAN)
                .queryParam("amount", amount)
                .queryParam("amountGreater", amountGreater)
                .queryParam("amountLess", amountLess)
                .queryParam("startDate", startDate)
                .queryParam("endDate", endDate);

        response = restTemplate.exchange(
                uriBuilder.toUriString(),
                HttpMethod.GET,
                new HttpEntity<>(null, httpHeaders),
                String.class
        );
    }

    @Then("I receive search transactions http status {int}")
    public void iReceiveSearchTransactionsHttpStatus(int status) {
        int actual = response.getStatusCode().value();
        Assertions.assertEquals(status, actual);
    }

    @And("I should receive search results as a list of size {int}")
    public void iShouldReceiveSearchResults(int count) {
        int actual = JsonPath.read(response.getBody(), "$.size()");
        Assertions.assertEquals(count, actual);
    }


    @Given("The endpoint for transactions {string} is available for method {string}")
    public void theEndpointForTransactionsIsAvailableForMethod(String endpoint, String method) {

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

    @Given("I log in as user with role employee to view transactions")
    public void iLogInAsUserWithRoleEmployeeToViewTransactions() throws JsonProcessingException {
        httpHeaders.clear();
        httpHeaders.add("Content-Type", "application/json");
        LoginDTO loginDTO = new LoginDTO(VALID_EMPLOYEE, VALID_EMPLOYEE_PASSWORD);
        token = getToken(loginDTO);
    }

    @When("I retrieve all transactions")
    public void iRetrieveAllTransactions() throws JsonProcessingException {

        httpHeaders.clear();
        httpHeaders.add("Authorization", "Bearer " + token);
        httpHeaders.add("Content-Type", "application/json");

        response = restTemplate.exchange(testConfig.getBaseUrl() + "/transactions", HttpMethod.GET, new HttpEntity<>(null, httpHeaders), String.class);
    }


    @Then("I receive http status {int} for transactions get request")
    public void iGetHttpStatus(int status) {
        int actual = response.getStatusCode().value();
        Assertions.assertEquals(status, actual);
    }

    @And("I should receive all transactions as a list of size {int}")
    public void iShouldReceiveAllTransactions(int count) {
        int actual = JsonPath.read(response.getBody(), "$.size()");
        Assertions.assertEquals(count, actual);

    }

    @When("I retrieve ATM transactions")
    public void iRetrieveATMTransactions() throws JsonProcessingException {

        httpHeaders.clear();
        httpHeaders.add("Authorization", "Bearer " + token);
        httpHeaders.add("Content-Type", "application/json");

        response = restTemplate.exchange(testConfig.getBaseUrl() + "/transactions/ATM", HttpMethod.GET, new HttpEntity<>(null, httpHeaders), String.class);
        System.out.println(response.getBody());
    }

    @And("I should receive all ATM transactions as a list of size {int}")
    public void iShouldReceiveAllATMTransactions(int count) {
        int actual = JsonPath.read(response.getBody(), "$.size()");
        Assertions.assertEquals(count, actual);

    }

    @And("the fromAccount or toAccount of each transaction should be ATM")
    public void iShouldReceiveTransactionsFromAccountOrToAccountATM() throws IOException {
        JsonNode res = mapper.readTree(response.getBody());
        ObjectReader reader = mapper.readerFor(new TypeReference<List<TransactionGETPOSTResponseDTO>>() {});
        List<TransactionGETPOSTResponseDTO> retrievedData = reader.readValue(res);

        Boolean filteredData = retrievedData.stream().allMatch(item -> item.fromAccount().equals("ATM") || item.toAccount().equals("ATM"));

        Assertions.assertTrue(filteredData);

    }

    @When("I retrieve all transactions by customers")
    public void iRetrieveAllTransactionsByCustomers() throws JsonProcessingException {

        httpHeaders.clear();
        httpHeaders.add("Authorization", "Bearer " + token);
        httpHeaders.add("Content-Type", "application/json");

        response = restTemplate.exchange(testConfig.getBaseUrl() + "/transactions/byCustomers", HttpMethod.GET, new HttpEntity<>(null, httpHeaders), String.class);
        System.out.println(response.getBody());
    }

    @And("I should receive all transactions by customers as a list of size {int}")
    public void iShouldReceiveAllTransactionsByCustomers(int count) {
        int actual = JsonPath.read(response.getBody(), "$.size()");
        Assertions.assertEquals(count, actual);

    }

    @When("I retrieve all transactions by employees")
    public void iRetrieveAllTransactionsByEmployees() throws JsonProcessingException {

        httpHeaders.clear();
        httpHeaders.add("Authorization", "Bearer " + token);
        httpHeaders.add("Content-Type", "application/json");

        response = restTemplate.exchange(testConfig.getBaseUrl() + "/transactions/byEmployees", HttpMethod.GET, new HttpEntity<>(null, httpHeaders), String.class);
        System.out.println(response.getBody());
    }

    @And("I should receive all transactions by employees as a list of size {int}")
    public void iShouldReceiveAllTransactionsByEmployees(int count) {
        int actual = JsonPath.read(response.getBody(), "$.size()");
        Assertions.assertEquals(count, actual);

    }

    @When("I retrieve online transactions")
    public void iRetrieveOnlineTransactions() throws JsonProcessingException {

        httpHeaders.clear();
        httpHeaders.add("Authorization", "Bearer " + token);
        httpHeaders.add("Content-Type", "application/json");

        response = restTemplate.exchange(testConfig.getBaseUrl() + "/transactions/online", HttpMethod.GET, new HttpEntity<>(null, httpHeaders), String.class);
        System.out.println(response.getBody());
    }

    @And("I should receive all online transactions as a list of size {int}")
    public void iShouldReceiveAllOnlineTransactions(int count) {
        int actual = JsonPath.read(response.getBody(), "$.size()");
        Assertions.assertEquals(count, actual);

    }


    @And("the fromAccount or toAccount of each transaction is different than ATM")
    public void iShouldReceiveTransactionsFromAccountOrToAccountNotATM() throws IOException {
        JsonNode res = mapper.readTree(response.getBody());
        ObjectReader reader = mapper.readerFor(new TypeReference<List<TransactionGETPOSTResponseDTO>>() {});
        List<TransactionGETPOSTResponseDTO> retrievedData = reader.readValue(res);

        Boolean filteredData = retrievedData.stream().allMatch(item -> !(item.fromAccount().equals("ATM") && item.toAccount().equals("ATM")));

        Assertions.assertTrue(filteredData);

    }


}
