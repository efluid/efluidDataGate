Feature: The managed parameters are specified by table in the dictionary

  Scenario: A parameter table edit page is available to all users
    Given from the home page
    When the user access to list of parameter tables page
    Then the provided template is parameter table list

  Scenario: Adding a new parameter table lists the available tables from managed database
    Given a managed database with two tables
    When the user access to new parameter table page
    Then the existing tables are displayed

  Scenario: A parameter table can be initialized from the list of available tables
    Given a managed database with two tables
    And the user is on new parameter table page
    When the user select one table to create
    Then the provided template is parameter table create
    And the selected table data are initialized
    And the default domain is automatically selected

  Scenario: An initialized parameter table is added to the project dictionary
    Given a prepared parameter table data with name "My Table" for managed table "TTAB_ONE"
    When the parameter table is saved by user
    Then the parameter table is added to the current user's project dictionary
    And the provided template is parameter table list

  Scenario: An simple initialized parameter table has its select query adapted with the selected columns
    Given a prepared parameter table data with name "My table" for managed table "TTAB_ONE" and columns selected as this :
      | name      | selection |
      | KEY       | ignored   |
      | VALUE     | key       |
      | PRESET    | selected  |
      | SOMETHING | selected  |
    When the parameter table is saved by user
    Then the parameter table for managed table "TTAB_ONE" is added to the current user's project dictionary
    And the selection clause for the parameter table for managed table "TTAB_ONE" is equals to "cur."PRESET", cur."SOMETHING""

  Scenario: An initialized parameter table with joined table has its select query adapted with the selected columns and relative tables
    Given a prepared parameter table data with name "My join table" for managed table "TTAB_FOUR" and columns selected as this :
      | name         | selection |
      | KEY          | key       |
      | OTHER_TABLE  | selected  |
      | CONTENT_TIME | selected  |
      | CONTENT_INT  | selected  |
    And the parameter table for managed table "TTAB_ONE" already exists
    When the parameter table is saved by user
    Then the parameter table for managed table "TTAB_FOUR" is added to the current user's project dictionary
    And the selection clause for the parameter table for managed table "TTAB_FOUR" is equals to "cur."CONTENT_INT", cur."CONTENT_TIME", ln1."VALUE" as ln_OTHER_TABLE_KEY"

  Scenario: A dictionary entry table content is available for testing. The result is provided from stale table details
    Given a prepared parameter table data with name "My Table" for managed table "TTAB_TWO"
    When the parameter table is tested by user
    Then the parameter table query result is provided with 10 detailled lines from managed table "TTAB_TWO"

  Scenario: For a table with only keys, no content is managed without error
    Given a prepared parameter table data with name "My key Only table" for managed table "TTAB_ONLY_KEYS" and columns selected as this :
      | name      | selection |
      | ONE_KEY   | key       |
      | OTHER_KEY | key       |
    And the parameter table for managed table "TTAB_ONE" already exists
    When the parameter table is saved by user
    Then the parameter table for managed table "TTAB_ONLY_KEYS" is added to the current user's project dictionary
    And the selection clause for the parameter table for managed table "TTAB_ONLY_KEYS" is empty
