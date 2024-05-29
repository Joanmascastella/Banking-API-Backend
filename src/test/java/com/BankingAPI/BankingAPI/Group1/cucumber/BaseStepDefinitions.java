package com.BankingAPI.BankingAPI.Group1.cucumber;

import io.cucumber.spring.CucumberContextConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.boot.test.web.client.TestRestTemplate;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@CucumberContextConfiguration
@ActiveProfiles("test")
public class BaseStepDefinitions {
    @Autowired
    protected TestRestTemplate testRestTemplate;

    public static final String VALID_USER = "johndoe";
    public static final String VALID_USER_EMAIL = "john.doe@example.com";
    public static final String VALID_EMPLOYEE = "Employee";
    public static final String VALID_USER_PASSWORD = "123";
    public static final String VALID_EMPLOYEE_PASSWORD = "employee";
    public static final String VALID_USER_NO_ACCOUNTS_USERNAME = "janedoe";
    public static final String VALID_USER_NO_ACCOUNTS_PASSWORD = "user";
    public static final String INVALID_USERNAME = "bla";
    public static final String INVALID_PASSWORD = "invalid";
}
