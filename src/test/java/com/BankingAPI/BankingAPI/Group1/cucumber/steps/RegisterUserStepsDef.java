package com.BankingAPI.BankingAPI.Group1.cucumber.steps;

import com.BankingAPI.BankingAPI.Group1.cucumber.BaseStepDefinitions;
import com.BankingAPI.BankingAPI.Group1.model.dto.UserPOSTResponseDTO;
import io.cucumber.datatable.DataTable;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.When;
import io.cucumber.java.en.Then;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;

import java.time.LocalDate;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class RegisterUserStepsDef extends BaseStepDefinitions {

    @Autowired
    private TestRestTemplate testRestTemplate;

    private ResponseEntity<String> response;
    private UserPOSTResponseDTO userDTO;

    @Given("the following user details:")
    public void givenTheFollowingUserDetails(DataTable dataTable) {
        Map<String, String> userDetails = dataTable.asMap(String.class, String.class);
        userDTO = new UserPOSTResponseDTO(
                userDetails.get("username"),
                userDetails.get("email"),
                userDetails.get("firstName"),
                userDetails.get("lastName"),
                userDetails.get("BSN"),
                userDetails.get("phoneNumber"),
                LocalDate.parse(userDetails.get("birthDate")),
                userDetails.get("password")
        );
    }

    @When("I register the user")
    public void whenIRegisterTheUser() {
        HttpHeaders headers = new HttpHeaders();
        HttpEntity<UserPOSTResponseDTO> request = new HttpEntity<>(userDTO, headers);
        response = testRestTemplate.exchange("/register", HttpMethod.POST, request, String.class);
    }

    @Then("the user should be successfully created")
    public void thenTheUserShouldBeSuccessfullyCreated() {
        assertEquals(201, response.getStatusCodeValue());
    }

    @Then("an error message {string} should be returned")
    public void thenAnErrorMessageShouldBeReturned(String expectedMessage) {
        assertEquals(409, response.getStatusCodeValue());
        assertTrue(response.getBody().contains(expectedMessage));
    }
}
