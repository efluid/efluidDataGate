Feature: A wizzard is used to init basic behaviors
  
  Scenario: The wizzard is default enabled after a fresh install
    Given the application is after a fresh install
    When any unauthenticated user access to home page
    Then the user is forwarded to wizzard welcome page
  
  Scenario: The wizzard is not enabled if already processed
    Given the application is fully initialized with the wizzard
    When any unauthenticated user access to home page
    Then the user is redirected to default page
  
  Scenario: The wizzard start with user creation
    Given the user is on wizzard welcome page
    When the user access to wizzard step one
    Then the provided template is wizzard user creation
    
  Scenario: The user specified on wizzard is stored and managed as current user
    Given the user is on wizzard user creation
    When the login "demo", the email "test@email.fr" and the password "test" are specified 
    Then the demo user is stored
    And the current user is demo
    And the provided template is wizzard projects creation
  