package com.BankingAPI.BankingAPI.Group1.cucumber;

import com.BankingAPI.BankingAPI.Group1.config.testConfigurations.TestConfig;
import com.BankingAPI.BankingAPI.Group1.model.dto.LoginDTO;
import com.BankingAPI.BankingAPI.Group1.model.dto.TokenDTO;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.cucumber.java.en.Given;
import io.cucumber.spring.CucumberContextConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.boot.test.web.client.TestRestTemplate;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@CucumberContextConfiguration
@ActiveProfiles("test")
public class BaseStepDefinitions {

    @Autowired
    public TestRestTemplate restTemplate;

    @Autowired
    private ObjectMapper mapper;

    @Autowired
    private TestConfig testConfig;

    public static final String VALID_USER = "johndoe";
    public static final String VALID_USER_EMAIL = "john.doe@example.com";
    public static final String VALID_USER_PASSWORD = "123";
    public static final String VALID_EMPLOYEE = "Employee";
    public static final String VALID_EMPLOYEE_PASSWORD = "employee";
    public static final String VALID_USER_NO_ACCOUNTS_USERNAME = "janedoae";
    public static final String VALID_USER_NO_ACCOUNTS_PASSWORD = "user";
    public static final String INVALID_USERNAME = "bla";
    public static final String INVALID_PASSWORD = "invalid";



    protected String getToken(LoginDTO loginDTO) throws JsonProcessingException {
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.add("Content-Type", "application/json");
        ResponseEntity<String> response = restTemplate.exchange(
                testConfig.getBaseUrl() + "/login",
                HttpMethod.POST,
                new HttpEntity<>(mapper.writeValueAsString(loginDTO), httpHeaders),
                String.class);
        TokenDTO tokenDTO = mapper.readValue(response.getBody(), TokenDTO.class);
        return tokenDTO.token();
    }
}
