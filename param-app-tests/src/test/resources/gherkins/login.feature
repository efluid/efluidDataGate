Feature: Access to the application is possible only for registered users after login
  
  Scenario: Without authentication a anonymous user is redirected to login page
    Given the user is not authenticated
    And the application is fully initialized with the wizzard
    When the user access to default page
    Then the user is redirected to login page
  
  Scenario: The user needs to specify a valid account to authenticate
    Given the user is not authenticated
    And the user is currently on login page
    And the application is fully initialized with the wizzard
    When the user specify valid credentials from "any"
    Then the authentication is successful
    And the user is redirected to home page
 