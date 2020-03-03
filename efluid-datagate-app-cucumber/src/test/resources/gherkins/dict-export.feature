Feature: A dictionary can be exported

  Scenario: The active version and schema are listed
    Given the existing versions v1, v2
    When the user access to dictionary export page
    Then the active version "v2" is displayed
    And the active schema is displayed

