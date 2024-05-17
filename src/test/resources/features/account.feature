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
