package com.BankingAPI.BankingAPI.Group1.cucumber.steps;

import com.BankingAPI.BankingAPI.Group1.config.TestConfig;
import com.BankingAPI.BankingAPI.Group1.cucumber.BaseStepDefinitions;
import com.BankingAPI.BankingAPI.Group1.model.dto.LoginDTO;
import com.BankingAPI.BankingAPI.Group1.model.dto.TokenDTO;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.cucumber.java.Before;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.When;
import io.cucumber.java.en.Then;
import lombok.SneakyThrows;
import lombok.extern.java.Log;
import org.junit.jupiter.api.Assertions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;

@Log
public class LoginUserStepsDef extends BaseStepDefinitions {

    private String LOGIN_ENDPOINT;
    private LoginDTO loginDTO;
    private String token;
    private final HttpHeaders httpHeaders = new HttpHeaders();
    private ResponseEntity<String> response;

    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private TestConfig testConfig;

    @SneakyThrows
    @Before
    public void init() {
        LOGIN_ENDPOINT = testConfig.getBaseUrl() + "/login";
        log.info("Initialization before each scenario");
    }

    @Given("I have a valid login object with valid user and valid password")
    public void iHaveAValidLoginObjectWithUserAndPassword() {
        loginDTO = new LoginDTO("johndoe", "123");
    }

    @When("I call the application login endpoint")
    public void iCallTheApplicationLoginEndpoint() throws JsonProcessingException {
        httpHeaders.add("Content-Type", "application/json");
        response = testRestTemplate.exchange(
                LOGIN_ENDPOINT,
                HttpMethod.POST,
                new HttpEntity<>(objectMapper.writeValueAsString(loginDTO), httpHeaders),
                String.class
        );
        log.info("Response body: " + response.getBody());
    }

    @Then("I receive a token")
    public void iReceiveAToken() throws JsonProcessingException {
        String responseBody = response.getBody();
        Assertions.assertNotNull(responseBody, "Response body is null");
        TokenDTO tokenDTO = objectMapper.readValue(responseBody, TokenDTO.class);
        token = tokenDTO.token();
        Assertions.assertNotNull(token, "Token is null");
    }

    @Given("I have a valid username but invalid password")
    public void iHaveAValidUsernameButInvalidPassword() {
        loginDTO = new LoginDTO("johndoe", "invalidPassword");
    }

    @Then("I receive http status {int}")
    public void iReceiveHttpStatus(int status) {
        Assertions.assertEquals(status, response.getStatusCode().value());
    }

    @Given("I have an invalid username and valid password")
    public void iHaveAnInvalidUsernameAndValidPassword() {
        loginDTO = new LoginDTO("johndoea", "123");
    }
}
