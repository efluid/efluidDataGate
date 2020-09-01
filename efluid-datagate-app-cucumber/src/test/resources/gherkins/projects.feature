Feature: The user can change project name
  Scenario: As a user I can change a project's name
    Given the existing projects "Default"
    When the user change the name of project "Default" to "maison1234"
    Then the name of project "maison1234" is updated

