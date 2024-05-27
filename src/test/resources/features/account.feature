Feature: Account API tests

  Scenario: Successful money transfer between own accounts
    Given I log in as user with valid accounts
    When I transfer 100.0 from account "DE89370400440532013000" to account "DE89370400440532013012"
    Then I get transfer http status 201
    And I get transfer message "Transfer successful"

  Scenario: Transfer fails due to insufficient funds
    Given I log in as user with valid accounts
    When I transfer 1000000.0 from account "DE89370400440532013000" to account "DE89370400440532013012"
    Then I get transfer http status 422
    And I get transfer message "Insufficient funds"

  Scenario: Transfer fails due to account not found
    Given I log in as user with valid accounts
    When I transfer 100.0 from account "DE89370400440532013012" to account "DE89370400440532013011"
    Then I get transfer http status 404
    And I get transfer message "Account with IBAN: DE89370400440532013011 not found"

  Scenario: Transfer fails due to daily limit exceeded
    Given I log in as user with valid accounts
    When I transfer 1000.0 from account "DE89370400440532013000" to account "DE89370400440532013012"
    Then I get transfer http status 422
    And I get transfer message "Transaction exceeds daily limit"

  Scenario: Getting all accounts
    Given The endpoint "/accounts/customers" is available for method "GET"
    And I log in as user with valid accounts
    When I retrieve all accounts
    Then I receive http status 200 for get request
    And I should receive all accounts as a list of size 6


  Scenario: Getting accounts by absolute limit
    Given The endpoint "/accounts/byAbsoluteLimit" is available for method "GET"
    And I log in as user with valid accounts
    When I retrieve accounts by absolute limit 2000
    Then I receive http status 200 for get request
    And I should receive the accounts with absolute limit as a list of size 5
    And The absolute limit of the accounts is less than or equal to 2000

  Scenario: Getting inactive accounts
    Given The endpoint "/accounts/inactive" is available for method "GET"
    And I log in as user with valid accounts
    When I retrieve inactive accounts
    Then I receive http status 200 for get request
    And I should receive the inactive accounts as a list of size 1
    And Each account has inactive status




