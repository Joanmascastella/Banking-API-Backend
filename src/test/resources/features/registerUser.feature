Feature: RegisterUsers

  Scenario: Successful registration of a new user
    Given the following user details:
      | username    | testuser       |
      | email       | test@example.com |
      | firstName   | Test           |
      | lastName    | User           |
      | BSN         | 123456789      |
      | phoneNumber | 123-456-7890   |
      | birthDate   | 1990-01-01     |
      | password    | password123    |
    When I register the user
    Then the user should be successfully created

  Scenario: Registration with an existing email
    Given the following user details:
      | username    | testuser2      |
      | email       | test@example.com |
      | firstName   | Test2          |
      | lastName    | User2          |
      | BSN         | 987654321      |
      | phoneNumber | 098-765-4321   |
      | birthDate   | 1992-02-02     |
      | password    | password456    |
    When I register the user
    Then an error message "Email already in use" should be returned
