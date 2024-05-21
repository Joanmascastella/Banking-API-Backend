Feature: User API tests

  Scenario: Getting user details
    Given The endpoint for "users/details" is available for method "GET"
    And I log in as user
    When I retrieve user details
    Then I get a list of accounts
    And I get http status 200

  Scenario: No accounts found for user
    Given The endpoint for "users/details" is available for method "GET"
    And I log in as user with no accounts
    When I retrieve user details
    Then I get http status 404
    And I get message "No accounts found for user."

  Scenario: Getting IBAN by first and last name
    Given The endpoint for "users/iban" is available for method "GET"
    And I log in as user
    When I provide first name "John" and last name "Doe"
    Then I get the IBAN
    And I get http status 200

  Scenario: IBAN not found for given name
    Given The endpoint for "users/iban" is available for method "GET"
    And I log in as user
    When I provide first name "Unknown" and last name "User"
    Then I get http status 404
    And I get message "IBAN not found for given name."
