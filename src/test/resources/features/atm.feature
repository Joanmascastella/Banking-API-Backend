Feature: atm

  Scenario: ATM login with valid credentials
    Given I log in to the ATM as user with valid credentials
    When I call the ATM login endpoint
    Then I get ATM http status 200
    And I receive an ATM token

# This fails due to dependency
  Scenario: ATM login with invalid credentials
    Given I log in to the ATM as user with invalid credentials
    When I call the ATM login endpoint
    Then I get ATM http status 401
    And I receive error message "Invalid username/password"

  Scenario: Successful ATM deposit
    Given I log in to the ATM as user with valid credentials
    When I perform an ATM deposit of 100.0 to account "NL89INHO0044053200"
    Then I get ATM http status 201
    And the ATM deposit to account "NL89INHO0044053200" is successful

  Scenario: Successful ATM withdrawal
    Given I log in to the ATM as user with valid credentials
    When I perform an ATM withdrawal of 100.0 from account "NL89INHO0044053200"
    Then I get ATM http status 201
    And the ATM withdrawal from account "NL89INHO0044053200" is successful

  Scenario: ATM withdrawal exceeds daily limit
    Given I log in to the ATM as user with valid credentials
    When I perform an ATM withdrawal of 1001.0 from account "NL89INHO0044053200"
    Then I get ATM http status 422
    And I receive error message "Transaction exceeds daily limit"

  Scenario: ATM withdrawal exceeds absolute limit
    Given I log in to the ATM as user with valid credentials
    When I perform an ATM withdrawal of 101.0 from account "NL89INHO0044053200"
    Then I get ATM http status 422
    And I receive error message "Transaction exceeds absolute limit"

  Scenario: ATM deposit exceeds daily limit
    Given I log in to the ATM as user with valid credentials
    When I perform an ATM deposit of 1001.0 to account "NL89INHO0044053200"
    Then I get ATM http status 422
    And I receive error message "Transaction exceeds daily limit"