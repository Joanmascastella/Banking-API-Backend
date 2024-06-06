Feature: Account API tests

  Scenario: Successful money transfer between own accounts
    Given I log in as user with valid accounts
    When I transfer 100.0 from account "NL89INHO0044053200" to account "NL89INHO0044053203"
    Then I get transfer http status 201
    And I get transfer message "Transfer successful"

  Scenario: Transfer fails due to insufficient funds
    Given I log in as user with valid accounts
    When I transfer 1000000.0 from account "NL89INHO0044053200" to account "NL89INHO0044053203"
    Then I get transfer http status 422
    And I get transfer message "Insufficient funds"

  Scenario: Transfer fails due to account not found
    Given I log in as user with valid accounts
    When I transfer 100.0 from account "NL89INHO0044053200" to account "DE89370400440532013011"
    Then I get transfer http status 404
    And I get transfer message "Account with IBAN: DE89370400440532013011 not found"

  Scenario: Transfer fails due to daily limit exceeded
    Given I log in as user with valid accounts
    When I transfer 1000.0 from account "NL89INHO0044053200" to account "NL89INHO0044053203"
    Then I get transfer http status 422
    And I get transfer message "Transaction exceeds daily limit"


  Scenario: Getting all accounts
    Given The endpoint for accounts "/accounts/customers" is available for method "GET"
    And I log in as user with role employee
    When I retrieve all accounts
    Then I receive http status 200 for accounts get request
    And I should receive all accounts as a list of size 6


  Scenario: Getting accounts by absolute limit
    Given The endpoint for accounts "/accounts/byAbsoluteLimit" is available for method "GET"
    And I log in as user with role employee
    When I retrieve accounts by absolute limit 2000
    Then I receive http status 200 for accounts get request
    And I should receive the accounts with absolute limit as a list of size 4
    And The absolute limit of the accounts is less than or equal to 2000

  Scenario: Getting inactive accounts
    Given The endpoint for accounts "/accounts/inactive" is available for method "GET"
    And I log in as user with role employee
    When I retrieve inactive accounts
    Then I receive http status 200 for accounts get request
    And I should receive the inactive accounts as a list of size 1
    And Each account has inactive status

  Scenario: Successfully update an account
    Given The endpoint for accounts "/accounts/customers/NL89INHO0044053200" is available for method "PUT"
    And I log in as user with role employee
    When I update the absolute limit of the account with IBAN "NL89INHO0044053200" to -200.0
    Then I get transfer http status 200
    And I get an empty account response body
    And the absolute limit of account with IBAN "NL89INHO0044053200" is updated to -200.0

  Scenario: Try to update an account that doesn't exist
    Given The endpoint for accounts "/accounts/customers/NL38INHO0000000000" is available for method "PUT"
    And I log in as user with role employee
    When I update the absolute limit of the account with IBAN "NL38INHO0000000000" to -200.0
    Then I get transfer http status 404
    And I get transfer message "Account not found by IBAN: NL38INHO0000000000"

  Scenario: Try to update an account with invalid limit
    Given The endpoint for accounts "/accounts/customers/NL89INHO0044053200" is available for method "PUT"
    And I log in as user with role employee
    When I update the absolute limit of the account with IBAN "NL89INHO0044053200" to null
    Then I get transfer http status 422
    And I get transfer message "Can't leave absolute limit empty. Please enter a valid amount."