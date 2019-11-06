Feature: Access to the application is possible only for registered users after login
  
  Scenario: Without authentication a anonymous user is redirected to login page
    Given the application is fully initialized with the wizard
    And the user is not authenticated
    When the user access to default page
    Then the user is redirected to login page
  
  Scenario: The user authenticate with valid credentials
    Given the application is fully initialized with the wizard
    And the user is not authenticated
    And the user is currently on login page
    When the user specify valid credentials from "any"
    Then the authentication is successful
    And the user is redirected to home page
  
  Scenario: The user cannot authenticate with wrong credentials
    Given the application is fully initialized with the wizard
    And the user is not authenticated
    And the user is currently on login page
    When the user specify invalid credentials from "other"
    Then the authentication is failed
    Then the user is redirected to login error page
 