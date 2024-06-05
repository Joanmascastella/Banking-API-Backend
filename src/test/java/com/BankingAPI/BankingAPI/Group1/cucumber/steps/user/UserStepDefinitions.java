package com.BankingAPI.BankingAPI.Group1.cucumber.steps.user;

import com.BankingAPI.BankingAPI.Group1.config.testConfigurations.TestConfig;
import com.BankingAPI.BankingAPI.Group1.cucumber.BaseStepDefinitions;
import com.BankingAPI.BankingAPI.Group1.model.Users;
import com.BankingAPI.BankingAPI.Group1.model.dto.LoginDTO;
import com.BankingAPI.BankingAPI.Group1.repository.UserRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
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

import java.util.*;

@Log
public class UserStepDefinitions extends BaseStepDefinitions {
    HttpHeaders httpHeaders = new HttpHeaders();

    @Autowired
    private TestRestTemplate customRestTemplate2;

    @Autowired
    private ObjectMapper mapper;

    @Autowired
    private TestConfig testConfig;

    private ResponseEntity<String> response;
    private String token;

    @Autowired
    private UserRepository userRepository;

    @SneakyThrows
    @Before
    public void init() {
        log.info("Initialized step definitions");
    }

    @Given("The endpoint for {string} is available for method {string}")
    public void theEndpointForIsAvailableForMethod(String endpoint, String method) {
        String url = testConfig.getBaseUrl() + "/" + endpoint;
        log.info("Checking availability of endpoint: " + url + " for method: " + method);

        response = customRestTemplate2.exchange(
                url,
                HttpMethod.OPTIONS,
                new HttpEntity<>(null, httpHeaders),
                String.class);

        log.info("Response status code: " + response.getStatusCode());
        log.info("Response headers: " + response.getHeaders());

        List<String> allowHeaders = response.getHeaders().get("Allow");

        if (allowHeaders == null || allowHeaders.isEmpty()) {
            log.severe("Allow header is missing or empty for endpoint: " + url);
            throw new AssertionError("Allow header is missing or empty for endpoint: " + url);
        }

        log.info("Allow header for endpoint " + url + ": " + allowHeaders);

        List<String> options = Arrays.stream(allowHeaders.get(0).split(","))
                .map(String::trim)
                .toList();

        Assertions.assertTrue(options.contains(method.toUpperCase()), "Method " + method + " is not allowed for endpoint " + endpoint);
    }

    @Given("I log in as user")
    public void iLogInAsUser() throws JsonProcessingException {
        httpHeaders.clear();
        httpHeaders.add("Content-Type", "application/json");
        LoginDTO loginDTO = new LoginDTO(VALID_USER, VALID_USER_PASSWORD);
        token = getToken(loginDTO);
    }

    @Given("I log in as user with no accounts")
    public void iLogInAsUserWithNoAccounts() throws JsonProcessingException {
        httpHeaders.clear();
        httpHeaders.add("Content-Type", "application/json");
        LoginDTO loginDTO = new LoginDTO(VALID_USER_NO_ACCOUNTS_USERNAME, VALID_USER_NO_ACCOUNTS_PASSWORD);
        token = getToken(loginDTO);
    }

    @When("I retrieve user details")
    public void iRetrieveUserDetails() {
        httpHeaders.clear();
        httpHeaders.add("Authorization", "Bearer " + token);
        response = customRestTemplate2.exchange(
                testConfig.getBaseUrl() + "/users/details",
                HttpMethod.GET,
                new HttpEntity<>(null, httpHeaders),
                String.class
        );
    }

    @Then("I get a list of accounts")
    public void iGetAListOfAccounts() {
        String body = response.getBody();
        List<Object> accounts = JsonPath.read(body, "$");
        Assertions.assertFalse(accounts.isEmpty(), "The accounts list should not be empty");
    }

    @Then("I get http status {int}")
    public void iGetHttpStatus(int status) {
        int actual = response.getStatusCode().value();
        Assertions.assertEquals(status, actual);
    }

    @Then("I get message {string}")
    public void iGetMessage(String message) {
        String actualMessage = response.getBody();
        Assertions.assertEquals(message, actualMessage);
    }

    @When("I provide first name {string} and last name {string}")
    public void iProvideFirstNameAndLastName(String firstName, String lastName) {
        httpHeaders.clear();
        httpHeaders.add("Authorization", "Bearer " + token);
        response = customRestTemplate2.exchange(
                testConfig.getBaseUrl() + "/users/iban?firstName=" + firstName + "&lastName=" + lastName,
                HttpMethod.GET,
                new HttpEntity<>(null, httpHeaders),
                String.class
        );
    }

    @Then("I get the IBAN")
    public void iGetTheIBAN() {
        String body = response.getBody();
        String iban = JsonPath.read(body, "$.iban");
        Assertions.assertNotNull(iban, "The IBAN should not be null");
    }

    @When("I retrieve all users")
    public void iRetrieveAllUsers() {
        httpHeaders.clear();
        httpHeaders.add("Authorization", "Bearer " + token);
        response = customRestTemplate2.exchange(
             testConfig.getBaseUrl() + "/users",
             HttpMethod.GET,
             new HttpEntity<>(null, httpHeaders),
             String.class
        );
    }

    @Given("I log in as employee")
    public void iLogInAsEmployee() throws JsonProcessingException {
        httpHeaders.clear();
        httpHeaders.add("Content-Type", "application/json");
        LoginDTO loginDTO = new LoginDTO(VALID_EMPLOYEE, VALID_EMPLOYEE_PASSWORD);
        token = getToken(loginDTO);
    }
    @And("I get a list of all users")
    public void iGetAListOfAllUsers() {
        String body = response.getBody();
        List<Object> users = JsonPath.read(body, "$");
        Assertions.assertFalse(users.isEmpty(), "The users list should not be empty");
    }

    @When("I retrieve all unapproved users")
    public void iRetrieveAllUnapprovedUsers() {
        httpHeaders.clear();
        httpHeaders.add("Authorization", "Bearer " + token);
        response = customRestTemplate2.exchange(
                testConfig.getBaseUrl() + "/users/noncustomers",
                HttpMethod.GET,
                new HttpEntity<>(null, httpHeaders),
                String.class
        );
    }

    @And("I get a list of all unapproved users")
    public void iGetAListOfAllUnapprovedUsers() {
        String body = response.getBody();
        List<Object> users = JsonPath.read(body, "$");
        Assertions.assertFalse(users.isEmpty(), "The users list should not be empty");
    }

    @When("I approve the user with userId {long} and set the dailyLimit to {double}, the absoluteSavingLimit to {double}, and the absoluteCheckingLimit to {double}")
    public void iApproveTheUserWithUserIdAndSetTheDailyLimitToTheAbsoluteSavingLimitToAndTheAbsoluteCheckingLimitTo(long userId, double dailyLimit, double absoluteSavingLimit, double absoluteCheckingLimit) {
        httpHeaders.clear();
        httpHeaders.add(HttpHeaders.CONTENT_TYPE, "application/json");
        httpHeaders.add("Authorization", "Bearer " + token);

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("dailyLimit", dailyLimit);
        requestBody.put("absoluteSavingLimit", absoluteSavingLimit);
        requestBody.put("absoluteCheckingLimit", absoluteCheckingLimit);

        HttpEntity<Object> requestEntity = new HttpEntity<>(requestBody, httpHeaders);

        response = customRestTemplate2.exchange(testConfig.getBaseUrl() + "/users/" + userId + "/approve",
                HttpMethod.PUT,
                requestEntity,
                String.class);
    }

    @And("the user with id {long} is approved")
    public void theUserWithIdIsApproved(long userId) {
        Optional<Users> userOptional = userRepository.findById(userId);

        Assertions.assertTrue(userOptional.isPresent(), "User not found");
        Users user = userOptional.get();

        Assertions.assertTrue(user.isApproved());
    }

    @Then("I get an empty response body")
    public void iGetAnEmptyResponseBody() {
        String responseBody = response.getBody().toString();
        Assertions.assertEquals("[]", responseBody, "Response body should be an empty list");
    }

    @When("I update the daily limit for user with id {int} to {double}")
    public void iUpdateTheDailyLimitForUserWithIdTo(int userId, double dailyLimit) {
        httpHeaders.clear();
        httpHeaders.add(HttpHeaders.CONTENT_TYPE, "application/json");
        httpHeaders.add("Authorization", "Bearer " + token);

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("id", userId);
        requestBody.put("dailyLimit", dailyLimit);

        HttpEntity<Object> requestEntity = new HttpEntity<>(requestBody, httpHeaders);

        response = customRestTemplate2.exchange(testConfig.getBaseUrl() + "/users",
                HttpMethod.PUT,
                requestEntity,
                String.class);
    }

    @And("the dailyLimit for userId {long} is {double}")
    public void theDailyLimitIs(long userId, double dailyLimit) {
        Optional<Users> userOptional = userRepository.findById(userId);

        Assertions.assertTrue(userOptional.isPresent(), "User not found");
        Users user = userOptional.get();

        Assertions.assertEquals(dailyLimit, user.getDailyLimit());
    }

    @When("I close the account for a user with id {long}")
    public void iCloseTheAccountForUserWithId(long userId) {
        httpHeaders.clear();
        httpHeaders.add("Authorization", "Bearer " + token);

        response = customRestTemplate2.exchange(testConfig.getBaseUrl() + "/users/" + userId,
                HttpMethod.DELETE,
                new HttpEntity<>(httpHeaders),
                String.class);
    }


    @And("the account with userId {long} is closed")
    public void theAccountIsClosed(long userId) {
        Optional<Users> userOptional = userRepository.findById(userId);

        Assertions.assertTrue(userOptional.isPresent(), "User not found");
        Users user = userOptional.get();
        Assertions.assertFalse(user.isActive(), "Account should be inactive");
    }

    @When("I approve the user with userId {long} and set the dailyLimit to null, the absoluteSavingsLimit to {double}, and the absoluteCheckingLimit to {double}")
    public void iApproveTheUserWithUserIdAndSetTheDailyLimitToNullTheAbsoluteSavingsLimitToAndTheAbsoluteCheckingLimitTo(long userId, double absoluteSavingsLimit, double absoluteCheckingLimit) {
        httpHeaders.clear();
        httpHeaders.add(HttpHeaders.CONTENT_TYPE, "application/json");
        httpHeaders.add("Authorization", "Bearer " + token);

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("dailyLimit", null);
        requestBody.put("absoluteSavingsLimit", absoluteSavingsLimit);
        requestBody.put("absoluteCheckingLimit", absoluteCheckingLimit);

        HttpEntity<Object> requestEntity = new HttpEntity<>(requestBody, httpHeaders);

        response = customRestTemplate2.exchange(testConfig.getBaseUrl() + "/users/" + userId + "/approve",
                HttpMethod.PUT,
                requestEntity,
                String.class);
    }
}
