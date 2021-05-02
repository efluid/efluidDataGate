Feature: A dictionary can be exported and imported

  Scenario: The active version and schema are listed
    Given the existing versions "v1, v2"
    When the user access to dictionary export page
    Then the active version "v2" is displayed
    And the active schema is displayed

  Scenario: The dictionary can be exported with all its projects
    Given the existing versions "v1, v2"
    When the user access to dictionary export page
    And the current full dictionary is exported
    Then a dictionary archive is produced

  Scenario: The dictionary can be exported for a single project
    Given the existing projects "Default, Other"
    And the parameter table for managed tables "TTAB_ONE, TTAB_THREE" already exists in project "Default"
    And the parameter table for managed tables "TTAB_TWO" already exists in project "Other"
    When the user access to dictionary export page
    And the project "Other" is exported
    Then a dictionary archive is produced for project "Other"

  Scenario: The dictionary can be imported for all its projects
    Given the existing projects "Default, Other"
    And the parameter table for managed tables "TTAB_ONE, TTAB_THREE" already exists in project "Default"
    And the parameter table for managed tables "TTAB_TWO" already exists in project "Other"
    When the user access to dictionary export page
    And the current full dictionary is exported
    And the user accesses to the destination environment without dictionary
    And the existing projects "Default"
    And the user import the available dictionary package as this
    Then the projects "Default, Other" exist
    And these parameter tables are specified :
      | project | table name | select clause                 |
      | Default | TTAB_ONE   | cur."PRESET", cur."SOMETHING" |
      | Default | TTAB_THREE | cur."OTHER"                   |
      | Other   | TTAB_TWO   | cur."VALUE", cur."OTHER"      |

  Scenario: A dictionary from a single project can be imported with its project asked to be copied
    Given the existing projects "Default, Other"
    And the parameter table for managed tables "TTAB_ONE, TTAB_THREE" already exists in project "Default"
    And the parameter table for managed tables "TTAB_TWO" already exists in project "Other"
    When the user access to dictionary export page
    And the project "Other" is exported
    And the user accesses to the destination environment without dictionary
    And the existing projects "Default"
    And the user import the available dictionary package as this
    Then the projects "Default, Other" exist
    And these parameter tables are specified :
      | project | table name | select clause            |
      | Other   | TTAB_TWO   | cur."VALUE", cur."OTHER" |

  Scenario: A dictionary from a single project can be imported onto the current project
    Given the existing projects "Default, Other"
    And the parameter table for managed tables "TTAB_ONE, TTAB_THREE" already exists in project "Default"
    And the parameter table for managed tables "TTAB_TWO" already exists in project "Other"
    When the user access to dictionary export page
    And the project "Other" is exported
    And the user accesses to the destination environment without dictionary
    And the existing projects "Default"
    And the user import the available dictionary package in current project
    Then the projects "Default" exist
    And these parameter tables are specified :
      | project | table name | select clause            |
      | Default | TTAB_TWO   | cur."VALUE", cur."OTHER" |

  Scenario: The conflicts during dictionary import are always resolved by using the imported data
    Given the existing projects "Default"
    And the parameter table for managed tables "TTAB_ONE, TTAB_THREE" already exists in project "Default"
    And a created parameter table with name "Table with 3 keys" for managed table "TTAB_THREE_KEYS" and columns selected as this :
      | name       | selection |
      | FIRST_KEY  | key       |
      | SECOND_KEY | key       |
      | THIRD_KEY  | selected  |
    When the user access to dictionary export page
    And the current full dictionary is exported
    And the user accesses to the destination environment without dictionary
    And the existing projects "Default"
    And the parameter table for managed tables "TTAB_ONE, TTAB_THREE" already exists in project "Default"
    And a created parameter table with name "Table with 3 keys" for managed table "TTAB_THREE_KEYS" and columns selected as this :
      | name       | selection |
      | FIRST_KEY  | key       |
      | SECOND_KEY | selected  |
      | THIRD_KEY  | selected  |
    And the user import the available dictionary package as this
    Then the projects "Default" exist
    And these parameter tables are specified :
      | project | table name      | key 1     | key 2      | select clause                 |
      | Default | TTAB_ONE        | VALUE     |            | cur."PRESET", cur."SOMETHING" |
      | Default | TTAB_THREE      | VALUE     |            | cur."OTHER"                   |
      | Default | TTAB_THREE_KEYS | FIRST_KEY | SECOND_KEY | cur."THIRD_KEY"               |