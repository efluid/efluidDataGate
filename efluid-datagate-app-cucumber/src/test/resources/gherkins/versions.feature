Feature: A dictionary is associated to versions

  Scenario: The list of specified versions is available to all users
    Given from the home page
    When the user access to list of versions
    Then the provided template is list of versions

  Scenario: The existing versions are listed
    Given the existing versions "v1, v2"
    When the user access to list of versions
    Then the 2 existing versions are displayed

  Scenario: A version can be added
    Given the existing versions "v1, v2"
    When the user add new version "v3"
    Then the 3 updated versions are displayed

  Scenario: A version can be deleted
    Given the existing versions "v1"
    When the user delete version "v1"
    Then the 0 existing versions are displayed

  Scenario: A version can be deleted if it is not used in commits
    Given the existing versions "v1"
    And the version v1 has no lots
    When the user delete version "v1"
    Then the 0 existing versions are displayed


  Scenario: When a version is added, the content of the dictionary is available for version compare
    Given the existing versions "v1"
    And this dictionary is added to current default domain :
      | entry name | table name  | select clause            | filter clause | key name | key type  |
      | My Entry   | T_TABLE_ONE | cur."COL_A", cur."COL_B" | 1=1           | COL_KEY  | PK_STRING |
      | My Entry 2 | T_TABLE_TWO | cur."COL_A", cur."COL_B" | 1=1           | COL_KEY  | PK_STRING |
    When the user add new version "v2"
    Then the version v2 contains this dictionary content :
      | entry name | table name  | select clause            | filter clause | key name | key type  |
      | My Entry   | T_TABLE_ONE | cur."COL_A", cur."COL_B" | 1=1           | COL_KEY  | PK_STRING |
      | My Entry 2 | T_TABLE_TWO | cur."COL_A", cur."COL_B" | 1=1           | COL_KEY  | PK_STRING |

  Scenario: Each added version manages its dictionary content for version compare
    Given the existing versions "vInit"
    And this dictionary is added to current default domain :
      | entry name | table name  | select clause            | filter clause | key name | key type  |
      | My Entry   | T_TABLE_ONE | cur."COL_A", cur."COL_B" | 1=1           | COL_KEY  | PK_STRING |
      | My Entry 2 | T_TABLE_TWO | cur."COL_A", cur."COL_B" | 1=1           | COL_KEY  | PK_STRING |
    And the user add new version "v2"
    And this dictionary is modified to current default domain :
      | entry name | table name  | select clause            | filter clause | key name | key type  |
      | My Entry   | T_TABLE_ONE | cur."COL_A"              | 1=1           | COL_KEY  | PK_STRING |
      | My Entry 2 | T_TABLE_TWO | cur."COL_A", cur."COL_B" | 1=1           | COL_KEY  | PK_STRING |
      | My Entry 3 | T_TABLE_BIS | cur."COL_A", cur."COL_B" | 1=1           | COL_KEY  | PK_ATOMIC |
    And the user add new version "v3"
    Then the version v2 contains this dictionary content :
      | entry name | table name  | select clause            | filter clause | key name | key type  |
      | My Entry   | T_TABLE_ONE | cur."COL_A", cur."COL_B" | 1=1           | COL_KEY  | PK_STRING |
      | My Entry 2 | T_TABLE_TWO | cur."COL_A", cur."COL_B" | 1=1           | COL_KEY  | PK_STRING |
    Then the version v3 contains this dictionary content :
      | entry name | table name  | select clause            | filter clause | key name | key type  |
      | My Entry   | T_TABLE_ONE | cur."COL_A"              | 1=1           | COL_KEY  | PK_STRING |
      | My Entry 2 | T_TABLE_TWO | cur."COL_A", cur."COL_B" | 1=1           | COL_KEY  | PK_STRING |
      | My Entry 3 | T_TABLE_BIS | cur."COL_A", cur."COL_B" | 1=1           | COL_KEY  | PK_ATOMIC |

  Scenario: A version can be updated to mark last changes in dictionary
    Given the existing versions "v1, v2, v3"
    When the user update version "v3"
    Then the 3 updated versions are displayed
    And the update date of version "v3" is updated

  Scenario: The version is created with the model identifier as name if this feature is enabled
    Given the existing versions "v1"
    And the current model id is "MOD1.1.0"
    And the feature "USE_MODEL_ID_AS_VERSION_NAME" is enabled
    When the user add new version "whatever"
    Then the current version name is "MOD1.1.0"

  Scenario: The version is not created with the model identifier as name if this feature is disabled
    Given the existing versions "v1"
    And the current model id is "MOD1.1.0"
    And the feature "USE_MODEL_ID_AS_VERSION_NAME" is disabled
    When the user add new version "whatever"
    Then the current version name is "whatever"

  Scenario: A new version cannot be created when the model identifier is used as name and the model identifier has not be updated
    Given the existing versions "MOD1.1.0"
    And the current model id is "MOD1.1.0"
    And the feature "USE_MODEL_ID_AS_VERSION_NAME" is enabled
    When the user access to list of versions
    Then the user cannot add a new version

  Scenario: A new version can be created when the model identifier is not used as name and the model identifier has not be updated
    Given the existing versions "MOD1.1.0"
    And the current model id is "MOD1.1.0"
    And the feature "USE_MODEL_ID_AS_VERSION_NAME" is disabled
    When the user access to list of versions
    Then the user can add a new version

  Scenario: The source dictionary version must be present in destination environment when processing a merge if validation feature is enabled
    Given the existing data in managed table "TTAB_TWO" :
      | key | value | other     |
      | JJJ | One   | Other JJJ |
      | KKK | Two   | Other KKK |
      | VVV | 333   | Other VVV |
      | III | 444   | Other III |
    And the commit ":tada: Test commit init source" has been saved and exported with all the identified initial diff content
    And the user accesses to the destination environment with the same dictionary
    And the existing version in destination environment is different
    And the feature "VALIDATE_VERSION_FOR_IMPORT" is enabled
    And the existing data in managed table "TTAB_TWO" in destination environment :
      | key | value | other     |
      | VVV | 333   | Other VVV |
      | III | 444   | Other III |
    And a commit ":construction: Destination commit 1" has been saved with all the new identified diff content in destination environment
    When the user import the available source package
    Then a merge diff fail with error code VERSION_NOT_IMPORTED

  Scenario: The source dictionary version may be missing in destination environment when processing a merge if validation feature is disabled
    Given the existing data in managed table "TTAB_TWO" :
      | key | value | other     |
      | JJJ | One   | Other JJJ |
      | KKK | Two   | Other KKK |
      | VVV | 333   | Other VVV |
      | III | 444   | Other III |
    And the commit ":tada: Test commit init source" has been saved and exported with all the identified initial diff content
    And the user accesses to the destination environment with the same dictionary
    And the existing version in destination environment is different
    And the feature "VALIDATE_VERSION_FOR_IMPORT" is disabled
    And the existing data in managed table "TTAB_TWO" in destination environment :
      | key | value | other     |
      | VVV | 333   | Other VVV |
      | III | 444   | Other III |
    And a commit ":construction: Destination commit 1" has been saved with all the new identified diff content in destination environment
    When the user import the available source package
    Then a merge diff is running
