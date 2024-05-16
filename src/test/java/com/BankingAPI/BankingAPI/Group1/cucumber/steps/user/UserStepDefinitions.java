package com.BankingAPI.BankingAPI.Group1.cucumber.steps.user;

import com.BankingAPI.BankingAPI.Group1.config.testConfigurations.TestConfig;
import com.BankingAPI.BankingAPI.Group1.cucumber.BaseStepDefinitions;
import com.BankingAPI.BankingAPI.Group1.model.dto.LoginDTO;
import com.BankingAPI.BankingAPI.Group1.model.dto.TokenDTO;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.JsonPath;
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

import java.util.Arrays;
import java.util.List;

@Log
public class UserStepDefinitions extends BaseStepDefinitions {
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

    @Given("The endpoint for {string} is available for method {string}")
    public void theEndpointForIsAvailableForMethod(String endpoint, String method) {
        String url = testConfig.getBaseUrl() + "/" + endpoint;
        log.info("Checking availability of endpoint: " + url + " for method: " + method);

        response = restTemplate.exchange(
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

    private String getToken(LoginDTO loginDTO) throws JsonProcessingException {
        response = restTemplate.exchange(
                testConfig.getBaseUrl() + "/login",
                HttpMethod.POST,
                new HttpEntity<>(mapper.writeValueAsString(loginDTO), httpHeaders),
                String.class);
        TokenDTO tokenDTO = mapper.readValue(response.getBody(), TokenDTO.class);
        return tokenDTO.token();
    }

    @When("I retrieve user details")
    public void iRetrieveUserDetails() {
        httpHeaders.clear();
        httpHeaders.add("Authorization", "Bearer " + token);
        response = restTemplate.exchange(
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
        response = restTemplate.exchange(
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
}
