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

  Scenario: A version can be removed from the list of versions if not used in commits
    Given the existing versions "v1, v2, v3"
    And the version v1 has no lots
    When the user delete version "v1"
    Then the 2 existing versions are displayed
    And a confirmation message on delete is displayed

  Scenario: The model identifier is stored with any new version
    Given the existing versions "v1"
    And the current model id is "MOD1.1.0"
    When the user add new version "v2"
    Then a confirmation message on create is displayed
    And the current version name is "v2"
    And the current version model identifier is "MOD1.1.0"

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

  Scenario: A version can be compared to another one and all the identified differences are displayed
    Given the existing versions "vInit"
    And this dictionary is added to current default domain :
      | entry name | table name  | select clause            | filter clause | key name | key type  |
      | My Entry   | T_TABLE_ONE | cur."COL_A", cur."COL_B" | 1=1           | COL_KEY  | PK_STRING |
      | My Entry 2 | T_TABLE_TWO | cur."COL_A", cur."COL_B" | 1=1           | COL_KEY  | PK_STRING |
    And the user add new version "v2"
    And this dictionary is modified to current default domain :
      | entry name | table name  | select clause            | filter clause           | key name | key type  |
      | My Entry   | T_TABLE_ONE | cur."COL_A"              | cur."COL_B" is not null | COL_KEY  | PK_STRING |
      | My Entry 2 | T_TABLE_TWO | cur."COL_A", cur."COL_B" | 1=1                     | COL_KEY  | PK_STRING |
      | My Entry 3 | T_TABLE_BIS | cur."COL_A", cur."COL_B" | 1=1                     | COL_KEY  | PK_ATOMIC |
    And the user add new version "v3"
    When the user request to compare the version "v2" with last version
    Then these domain changes are identified for the dictionary content :
      | domain      | change   | unmodified table count |
      | Test domain | MODIFIED | 1                      |
    And these table changes are identified for the dictionary content :
      | domain      | table       | change    | table change               | name change              | filter change                  | column change count |
      | Test domain | T_TABLE_BIS | ADDED     | T_TABLE_BIS -> T_TABLE_BIS | My Entry 3 -> My Entry 3 | 1=1 -> 1=1                     | 3                   |
      | Test domain | T_TABLE_ONE | MODIFIED  | T_TABLE_ONE -> T_TABLE_ONE | My Entry -> My Entry     | 1=1 -> cur."COL_B" is not null | 1                   |
      | Test domain | T_TABLE_TWO | UNCHANGED | T_TABLE_TWO -> T_TABLE_TWO | My Entry 2 -> My Entry 2 | 1=1 -> 1=1                     | 0                   |
    And these column changes are identified for the dictionary content :
      | domain      | table       | column  | change    | link change  | key change     |
      | Test domain | T_TABLE_ONE | COL_KEY | UNCHANGED | null -> null | true -> true   |
      | Test domain | T_TABLE_ONE | COL_B   | REMOVED   | null -> null | false -> false |
      | Test domain | T_TABLE_ONE | COL_A   | UNCHANGED | null -> null | false -> false |
      | Test domain | T_TABLE_TWO | COL_KEY | UNCHANGED | null -> null | true -> true   |
      | Test domain | T_TABLE_TWO | COL_B   | UNCHANGED | null -> null | false -> false |
      | Test domain | T_TABLE_TWO | COL_A   | UNCHANGED | null -> null | false -> false |
      | Test domain | T_TABLE_BIS | COL_KEY | ADDED     | null -> null | true -> true   |
      | Test domain | T_TABLE_BIS | COL_B   | ADDED     | null -> null | false -> false |
      | Test domain | T_TABLE_BIS | COL_A   | ADDED     | null -> null | false -> false |

  Scenario: The version of a dictionary can change and the commits apply update on dictionary as content changes
    Given the existing data in managed table "TTAB_ONE" :
      | key | value | preset   | something |
      | 14  | AAA   | Preset 1 | AAA       |
      | 25  | BBB   | Preset 2 | BBB       |
      | 37  | CCC   | Preset 3 | CCC       |
      | 38  | DDD   | Preset 4 | DDD       |
      | 39  | EEE   | Preset 5 | EEE       |
    And the existing data in managed table "TTAB_TWO" :
      | key | value | other     |
      | JJJ | One   | Other JJJ |
      | KKK | Two   | Other KKK |
    And the existing versions "v0"
    And this dictionary is added to current default domain :
      | entry name | table name | select clause | filter clause | key name | key type  |
      | Table One  | TTAB_ONE   | cur."VALUE"   | 1=1           | KEY      | PK_ATOMIC |
    And the user add new version "v1"
    And a new commit ":construction: Commit on version v1 source" has been saved with all the new identified diff content
    And this dictionary is modified to current default domain :
      | entry name | table name | select clause             | filter clause | key name | key type  |
      | Table One  | TTAB_ONE   | cur."VALUE", cur."PRESET" | 1=1           | KEY      | PK_ATOMIC |
      | Table Two  | TTAB_TWO   | cur."VALUE", cur."OTHER"  | 1=1           | KEY      | PK_STRING |
    And the user add new version "v2"
    And these changes are applied to table "TTAB_TWO" :
      | change | key | value | other     |
      | add    | LLL | Three | Other LLL |
      | add    | MMM | Four  | Other MMM |
    And a new commit ":construction: Commit on version v2 source" has been saved with all the new identified diff content
    When the user request an export of all the commits
    Then the export package contains 2 commit contents
    And the export package content has these identified changes for commit with name ":construction: Commit on version v1 source" :
      | Table    | Key | Action | Payload     |
      | TTAB_ONE | 14  | ADD    | VALUE:'AAA' |
      | TTAB_ONE | 25  | ADD    | VALUE:'BBB' |
      | TTAB_ONE | 37  | ADD    | VALUE:'CCC' |
      | TTAB_ONE | 38  | ADD    | VALUE:'DDD' |
      | TTAB_ONE | 39  | ADD    | VALUE:'EEE' |
    And the export package content has these identified changes for commit with name ":construction: Commit on version v2 source" :
      | Table    | Key | Action | Payload                          |
      | TTAB_ONE | 14  | UPDATE | PRESET:n/a=>'Preset 1'           |
      | TTAB_ONE | 25  | UPDATE | PRESET:n/a=>'Preset 2'           |
      | TTAB_ONE | 37  | UPDATE | PRESET:n/a=>'Preset 3'           |
      | TTAB_ONE | 38  | UPDATE | PRESET:n/a=>'Preset 4'           |
      | TTAB_ONE | 39  | UPDATE | PRESET:n/a=>'Preset 5'           |
      | TTAB_TWO | JJJ | ADD    | VALUE:'One', OTHER:'Other JJJ'   |
      | TTAB_TWO | KKK | ADD    | VALUE:'Two', OTHER:'Other KKK'   |
      | TTAB_TWO | LLL | ADD    | VALUE:'Three', OTHER:'Other LLL' |
      | TTAB_TWO | MMM | ADD    | VALUE:'Four', OTHER:'Other MMM'  |

  Scenario: A version can be updated to mark last changes in dictionary
    Given the existing versions "v1, v2, v3"
    When the user update version "v3"
    Then the 3 updated versions are displayed
    And the update date of version "v3" is updated
    And a confirmation message on update is displayed

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
    Then a merge diff fail with error code MERGE_DICT_NOT_COMPATIBLE

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

  Scenario: The dictionary content from referenced version is included with commit export package
    Given the existing data in managed table "TTAB_ONE" :
      | key | value | preset   | something |
      | 14  | AAA   | Preset 1 | AAA       |
      | 25  | BBB   | Preset 2 | BBB       |
      | 37  | CCC   | Preset 3 | CCC       |
      | 38  | DDD   | Preset 4 | DDD       |
      | 39  | EEE   | Preset 5 | EEE       |
    And the existing versions "v0"
    And this dictionary is added to current default domain :
      | entry name | table name | select clause | filter clause | key name | key type  |
      | Table One  | TTAB_ONE   | cur."VALUE"   | 1=1           | KEY      | PK_ATOMIC |
    And the user add new version "v1"
    And this dictionary is modified to current default domain :
      | entry name | table name | select clause             | filter clause | key name | key type  |
      | Table One  | TTAB_ONE   | cur."VALUE", cur."PRESET" | 1=1           | KEY      | PK_ATOMIC |
      | Table Two  | TTAB_TWO   | cur."VALUE", cur."OTHER"  | 1=1           | KEY      | PK_STRING |
    And the user add new version "v2"
    And a new commit ":construction: Commit on version v2 source" has been saved with all the new identified diff content
    When the user request an export of the commit with name ":construction: Commit on version v2 source"
    Then the export package contains 1 commit contents
    And the export package contains 1 version contents
    And the export package content has these identified changes for commit with name ":construction: Commit on version v2 source" :
      | Table    | Key | Action | Payload                        |
      | TTAB_ONE | 14  | ADD    | VALUE:'AAA', PRESET:'Preset 1' |
      | TTAB_ONE | 25  | ADD    | VALUE:'BBB', PRESET:'Preset 2' |
      | TTAB_ONE | 37  | ADD    | VALUE:'CCC', PRESET:'Preset 3' |
      | TTAB_ONE | 38  | ADD    | VALUE:'DDD', PRESET:'Preset 4' |
      | TTAB_ONE | 39  | ADD    | VALUE:'EEE', PRESET:'Preset 5' |
    And the export package content has this dictionary definition extract for commit ":construction: Commit on version v2 source" :
      | entry name | table name | select clause             | filter clause | key name | key type  |
      | Table One  | TTAB_ONE   | cur."VALUE", cur."PRESET" | 1=1           | KEY      | PK_ATOMIC |
      | Table Two  | TTAB_TWO   | cur."VALUE", cur."OTHER"  | 1=1           | KEY      | PK_STRING |

  Scenario: The dictionary contents from all referenced versions are included with multi-commits export package
    Given the existing data in managed table "TTAB_ONE" :
      | key | value | preset   | something |
      | 14  | AAA   | Preset 1 | AAA       |
      | 25  | BBB   | Preset 2 | BBB       |
      | 37  | CCC   | Preset 3 | CCC       |
      | 38  | DDD   | Preset 4 | DDD       |
      | 39  | EEE   | Preset 5 | EEE       |
    And the existing data in managed table "TTAB_TWO" :
      | key | value | other     |
      | JJJ | One   | Other JJJ |
      | KKK | Two   | Other KKK |
    And the existing versions "v0"
    And this dictionary is added to current default domain :
      | entry name | table name | select clause | filter clause | key name | key type  |
      | Table One  | TTAB_ONE   | cur."VALUE"   | 1=1           | KEY      | PK_ATOMIC |
    And the user add new version "v1"
    And a new commit ":construction: Commit on version v1 source" has been saved with all the new identified diff content
    And this dictionary is modified to current default domain :
      | entry name | table name | select clause             | filter clause | key name | key type  |
      | Table One  | TTAB_ONE   | cur."VALUE", cur."PRESET" | 1=1           | KEY      | PK_ATOMIC |
      | Table Two  | TTAB_TWO   | cur."VALUE", cur."OTHER"  | 1=1           | KEY      | PK_STRING |
    And the user add new version "v2"
    And these changes are applied to table "TTAB_TWO" :
      | change | key | value | other     |
      | add    | LLL | Three | Other LLL |
      | add    | MMM | Four  | Other MMM |
    And a new commit ":construction: Commit on version v2 source" has been saved with all the new identified diff content
    When the user request an export of all the commits
    Then the export package contains 2 commit contents
    And the export package contains 2 version contents
    And the export package content has this dictionary definition extract for commit ":construction: Commit on version v1 source" :
      | entry name | table name | select clause | filter clause | key name | key type  |
      | Table One  | TTAB_ONE   | cur."VALUE"   | 1=1           | KEY      | PK_ATOMIC |
    And the export package content has this dictionary definition extract for commit ":construction: Commit on version v2 source" :
      | entry name | table name | select clause             | filter clause | key name | key type  |
      | Table One  | TTAB_ONE   | cur."VALUE", cur."PRESET" | 1=1           | KEY      | PK_ATOMIC |
      | Table Two  | TTAB_TWO   | cur."VALUE", cur."OTHER"  | 1=1           | KEY      | PK_STRING |


