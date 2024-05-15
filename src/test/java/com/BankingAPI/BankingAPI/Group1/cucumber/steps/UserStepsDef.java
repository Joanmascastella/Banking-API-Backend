package com.BankingAPI.BankingAPI.Group1.cucumber.steps;

import com.BankingAPI.BankingAPI.Group1.cucumber.BaseStepDefinitions;
import com.BankingAPI.BankingAPI.Group1.model.dto.UserPOSTResponseDTO;
import io.cucumber.datatable.DataTable;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.When;
import io.cucumber.java.en.Then;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDate;
import java.util.Map;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class UserStepsDef extends BaseStepDefinitions {

    @Autowired
    private TestRestTemplate testRestTemplate;

    private ResponseEntity<String> response;
    private UserPOSTResponseDTO userDTO;

    @Given("the following user details:")
    public void the_following_user_details(DataTable dataTable) {
        Map<String, String> data = dataTable.asMap(String.class, String.class);
        userDTO = new UserPOSTResponseDTO(
                data.get("username"),
                data.get("email"),
                data.get("firstName"),
                data.get("lastName"),
                data.get("BSN"),
                data.get("phoneNumber"),
                LocalDate.parse(data.get("birthDate")),
                data.get("password")
        );
    }

    @When("I register the user")
    public void i_register_the_user() {
        response = testRestTemplate.postForEntity("/register", userDTO, String.class);
    }

    @Then("the user should be successfully created")
    public void the_user_should_be_successfully_created() {
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
    }

    @Then("an error message {string} should be returned")
    public void an_error_message_should_be_returned(String expectedMessage) {
        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertTrue(Objects.requireNonNull(response.getBody()).contains(expectedMessage));
    }
}
