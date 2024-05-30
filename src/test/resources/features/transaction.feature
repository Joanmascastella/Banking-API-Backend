Feature: Transaction API tests


  Scenario: Successful transfer to another customer
    Given I log in as user with valid accounts for transactions
    When I transfer money 100.0 from account "NL89INHO0044053200" to account "NL89INHO0044053201"
   Then I get transaction transfer http status 201
   And I get transaction transfer message "Transfer successful"

  Scenario: Transfer fails due to insufficient funds
    Given I log in as user with valid accounts for transactions
    When I transfer money 1000000.0 from account "NL89INHO0044053200" to account "NL89INHO0044053201"
    Then I get transaction transfer http status 422
    And I get transaction transfer message "Insufficient funds"

  Scenario: Transfer fails due to account not found
    Given I log in as user with valid accounts for transactions
    When I transfer money 100.0 from account "DE89370400440532013010" to account "NL89INHO0044053200"
    Then I get transaction transfer http status 404
    And I get transaction transfer message "Account with IBAN: DE89370400440532013010 not found"

  Scenario: Transfer fails due to daily limit exceeded
    Given I log in as user with valid accounts for transactions
    When I transfer money 3000.0 from account "NL89INHO0044053200" to account "NL89INHO0044053201"
    Then I get transaction transfer http status 422
    And I get transaction transfer message "Transaction exceeds daily limit"

  Scenario: Transfer fails due to same user accounts
    Given I log in as user with valid accounts for transactions
    When I transfer money 100.0 from account "NL89INHO0044053200" to account "NL89INHO0044053203"
    Then I get transaction transfer http status 400
    And I get transaction transfer message "Both accounts cannot belong to the same user for this operation."

  Scenario: Transfer fails due to non-checking accounts
    Given I log in as user with valid accounts for transactions
    When I transfer money 100.0 from account "NL89INHO004523271" to account "NL89INHO0044053203"
    Then I get transaction transfer http status 400
    And I get transaction transfer message "Cannot transfer money between savings accounts."

  Scenario: Getting transaction history
    Given I log in as user with valid accounts for transactions
    When I retrieve transaction history for user 1
    Then I receive transaction history http status 200
    And I should receive transaction history as a list of size 6

  Scenario: Searching transactions
    Given I log in as user with valid accounts for transactions
    When I search transactions with criteria IBAN "NL89INHO0044053200", amount 9500.0, amountGreater 50.0, amountLess 10000.0, startDate "2024-01-01", endDate "2024-12-31"
    Then I receive search transactions http status 200
    And I should receive search results as a list of size 1

  Scenario: Getting all transactions
    Given The endpoint for transactions "/transactions" is available for method "GET"
    And I log in as user with role employee to view transactions
    When I retrieve all transactions
    Then I receive http status 200 for transactions get request
    And I should receive all transactions as a list of size 8

  Scenario: Getting ATM transactions
    Given The endpoint for transactions "/transactions/ATM" is available for method "GET"
    And I log in as user with role employee to view transactions
    When I retrieve ATM transactions
    Then I receive http status 200 for transactions get request
    And I should receive all ATM transactions as a list of size 4
    And the fromAccount or toAccount of each transaction should be ATM

  Scenario: Getting all transactions by customers
    Given The endpoint for transactions "/transactions/byCustomers" is available for method "GET"
    And I log in as user with role employee to view transactions
    When I retrieve all transactions by customers
    Then I receive http status 200 for transactions get request
    And I should receive all transactions by customers as a list of size 6


  Scenario: Getting all transactions by employees
    Given The endpoint for transactions "/transactions/byEmployees" is available for method "GET"
    And I log in as user with role employee to view transactions
    When I retrieve all transactions by employees
    Then I receive http status 200 for transactions get request
    And I should receive all transactions by employees as a list of size 2

  Scenario: Getting online transactions
    Given The endpoint for transactions "/transactions/online" is available for method "GET"
    And I log in as user with role employee to view transactions
    When I retrieve online transactions
    Then I receive http status 200 for transactions get request
    And I should receive all online transactions as a list of size 4
    And the fromAccount or toAccount of each transaction is different than ATM
