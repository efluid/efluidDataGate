Feature: A wizard is used to init basic behaviors
  
  Scenario: The wizard is default enabled after a fresh install
    Given the application is after a fresh install
    When any unauthenticated user access to home page
    Then the user is forwarded to wizard welcome page
  
  Scenario: The wizard is not enabled if already processed
    Given the application is fully initialized with the wizard
    When any unauthenticated user access to home page
    Then the user is redirected to default page
  
  Scenario: The wizard start with user creation
    Given the user is on wizard welcome page
    When the user access to wizard step one
    Then the provided template is wizard user creation
    
  Scenario: The user specified on wizard is stored and managed as current user
    Given the user is on wizard user creation
    When the login "demo", the email "test@email.fr" and the password "test" are specified 
    Then the demo user is stored
    And the current user is demo
    And the provided template is wizard projects creation
  