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

  Scenario: Getting all users
    Given The endpoint for "/users" is available for method "GET"
    And I log in as employee
    When I retrieve all users
    Then I get http status 200
    And I get a list of all users

  Scenario: Getting all unapproved users
    Given The endpoint for "/users/noncustomers" is available for method "GET"
    And I log in as employee
    When I retrieve all unapproved users
    Then I get http status 200
    And I get a list of all unapproved users

  Scenario: Approving an unapproved user
    Given The endpoint for "/users/2/approve" is available for method "PUT"
    And I log in as employee
    When I approve the user with userId 2 and set the dailyLimit to 100.0, the absoluteSavingLimit to 0.0, and the absoluteCheckingLimit to -200.0
    Then I get http status 200
    And I get an empty response body
    And the user with id 2 is approved
    And two bank accounts are created for the user with id 2

  Scenario: user to be approved not found
    Given The endpoint for "/users/99/approve" is available for method "PUT"
    And I log in as employee
    When I approve the user with userId 99 and set the dailyLimit to 100.0, the absoluteSavingLimit to 0.0, and the absoluteCheckingLimit to -200.0
    Then I get http status 404
    And I get message "User not found with id: 99"

  Scenario: daily limit is negative
    Given The endpoint for "/users/3/approve" is available for method "PUT"
    And I log in as employee
    When I approve the user with userId 3 and set the dailyLimit to -100.0, the absoluteSavingLimit to 0.0, and the absoluteCheckingLimit to -200.0
    Then I get http status 422
    And I get message "The daily limit can't be set to a negative amount or be left empty."

  Scenario: daily limit is empty
    Given The endpoint for "/users/3/approve" is available for method "PUT"
    And I log in as employee
    When I approve the user with userId 3 and set the dailyLimit to null, the absoluteSavingsLimit to 0.0, and the absoluteCheckingLimit to -200.0
    Then I get http status 422
    And I get message "The daily limit can't be set to a negative amount or be left empty."

  Scenario: user is already approved
   Given The endpoint for "/users/1/approve" is available for method "PUT"
   And I log in as employee
   When I approve the user with userId 1 and set the dailyLimit to 100.0, the absoluteSavingLimit to 0.0, and the absoluteCheckingLimit to -200.0
   Then I get http status 409
   And I get message "The user is already approved."

  Scenario: Updating limit for a valid user
    Given The endpoint for "/users/1" is available for method "PUT"
    And I log in as employee
    When I update the daily limit for user with id 1 to 500.0
    Then I get http status 200
    And I get an empty response body
    And the dailyLimit for userId 1 is 500.0
    
  Scenario: Updating limit to invalid amount
    Given The endpoint for "/users/1" is available for method "PUT"
    And I log in as employee
    When I update the daily limit for user with id 1 to -500.0
    Then I get http status 422
    And I get message "The daily limit can't be set to a negative amount or be left empty."

  Scenario: Updating limit for non existent user
    Given The endpoint for "/users/99" is available for method "PUT"
    And I log in as employee
    When I update the daily limit for user with id 99 to 500.0
    Then I get http status 404                                            
    And I get message "User not found with id: 99"

  Scenario: Closing a users account
    Given The endpoint for "/users/1" is available for method "DELETE"
    And I log in as employee
    When I close the account for a user with id 1
    Then I get http status 200
    And the account with userId 1 is closed
    And all bank account of userId 1 are closed
    
  Scenario: Closing account of non-existent user
    Given The endpoint for "/users/99" is available for method "DELETE"   
    And I log in as employee                                             
    When I close the account for a user with id 9                
    Then I get http status 404                                           
    And I get message "User not found with id: 9"

  Scenario: Closing an employee account
    Given The endpoint for "/users/5" is available for method "DELETE"
    And I have a valid employee token
    When I close the account for a user with id 5
    Then I get http status 401
    And I get message "Invalid JWT token"

  Scenario: Closing an account that is already closed or not approved
    Given The endpoint for "/users/3" is available for method "DELETE"
    And I log in as employee
    When I close the account for a user with id 3
    Then I get http status 422
    And I get message "This account can't be closed."