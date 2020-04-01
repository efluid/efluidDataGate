Feature: Some transformers can be set for a project to adapt commits at import regarding various rules

  Scenario: The list of configured transformers for a project is available to all users
    Given from the home page
    When the user access to list of transformers
    Then the provided template is list of transformers

  Scenario: The existing transformers from project are listed
    Given the configured transformers for project "Default" :
      | name | type | priority | configuration |
      | tt   | yy   | tt       | yy            |
    When the user access to list of transformers
    Then the 2 configured transformers from project "Default" are displayed

  Scenario: The available types of transformers are listed
    Given from the home page
    When the user access to list of transformers
    Then the available transformer types are :
      | name | type |
      | tt   | yy   |

  Scenario: A transformer can be added for a specified type
    Given the existing versions "v1, v2"
    When the user add new version "v3"
    Then the 3 updated versions are displayed
