Feature: The managed parameters are specified by table in the dictionary
  
  Scenario: A parameter table edit page is available to all users
    Given from the home page
    When the user access to list of parameter tables page
    Then the provided template is parameter table
    
  Scenario: Adding a new parameter table lists the available tables from managed database
    Given a managed database with two tables
    When the user access to new parameter table page
    Then the existing tables are displayed
    
    