Feature: The user can change project name
  Scenario: As a user I can change the name of a project
    Given the existing projects "Default"
    When the user change the name of project "Default" to "maison1234"
    Then the name of project "maison1234" is updated

