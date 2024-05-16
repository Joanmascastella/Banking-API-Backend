Feature: Transaction API tests


  Scenario: Successful transfer to another customer
    Given I log in as user with valid accounts for transactions
    When I transfer money 100.0 from account "DE89370400440532013000" to account "DE89370400440532013022"
    Then I get transaction transfer http status 201
    And I get transaction transfer message "Transfer successful"

  Scenario: Transfer fails due to insufficient funds
    Given I log in as user with valid accounts for transactions
    When I transfer money 1000000.0 from account "DE89370400440532013000" to account "DE89370400440532013022"
    Then I get transaction transfer http status 422
    And I get transaction transfer message "Insufficient funds"

  Scenario: Transfer fails due to account not found
    Given I log in as user with valid accounts for transactions
    When I transfer money 100.0 from account "DE89370400440532013010" to account "DE89370400440532013000"
    Then I get transaction transfer http status 404
    And I get transaction transfer message "Account with IBAN: DE89370400440532013010 not found"

  Scenario: Transfer fails due to daily limit exceeded
    Given I log in as user with valid accounts for transactions
    When I transfer money 3000.0 from account "DE89370400440532013000" to account "DE89370400440532013022"
    Then I get transaction transfer http status 422
    And I get transaction transfer message "Transaction exceeds daily limit"

  Scenario: Transfer fails due to same user accounts
    Given I log in as user with valid accounts for transactions
    When I transfer money 100.0 from account "DE89370400440532013000" to account "DE89370400440532013012"
    Then I get transaction transfer http status 400
    And I get transaction transfer message "Both accounts cannot belong to the same user for this operation."

  Scenario: Transfer fails due to non-checking accounts
    Given I log in as user with valid accounts for transactions
    When I transfer money 100.0 from account "DE89370400440532013012" to account "DE89370400440532013042"
    Then I get transaction transfer http status 400
    And I get transaction transfer message "Cannot transfer money between savings accounts."
