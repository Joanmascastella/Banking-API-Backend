Feature: atm

  Scenario: Successful ATM login
    Given I log in to the ATM as user with valid credentials
    Then I receive http status 200

  Scenario: Successful ATM deposit
    Given I log in to the ATM as user with valid credentials
    When I perform an ATM deposit of 100.0 to account "NL89INHO0044053200"
    Then I get ATM transaction http status 201
    And the ATM deposit to account "NL89INHO0044053200" is successful

  Scenario: Successful ATM withdrawal
    Given I log in to the ATM as user with valid credentials
    When I perform an ATM withdrawal of 100.0 from account "NL89INHO0044053200"
    Then I get ATM transaction http status 201
    And the ATM withdrawal from account "NL89INHO0044053200" is successful

