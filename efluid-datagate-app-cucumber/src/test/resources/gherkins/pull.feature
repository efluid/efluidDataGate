#noinspection NonAsciiCharacters
Feature: The backlog can be imported and merged with local changes

  The backlog content (commit with indexes, lob content and attachments) can be imported, and mixed (merged)
  with the changes from the locale instance using fixed merging rules

  Background:
    Given the existing data in managed table "TTAB_TWO" :
      | key | value | other     |
      | JJJ | One   | Other JJJ |
      | KKK | Two   | Other KKK |
      | VVV | 333   | Other VVV |
      | III | 444   | Other III |
    And the existing data in managed table "TTAB_FIVE" :
      | key | data                | simple |
      | 1   | ABCDEF1234567ABDDDD | 17.81  |
      | 2   | ABCDEF1234567ABEEEE | 17.82  |
      | 3   | ABCDEF1234567ABFFFF | 17.83  |
    And the existing data in managed table "TTAB_SIX" :
      | identifier | text                                     | date       |
      | 7          | Ceci est un text enregistré dans un CLOB | 2012-01-15 |
      | 8          | Un autre text CLOB                       | 2005-07-08 |
      | 9          | Encore un autre                          | 2021-12-25 |

  Scenario: Commits are prepared in a destination environment before importing a package - merge init state
    Given the commit ":tada: Test commit init source" has been saved and exported with all the identified initial diff content
    And the user accesses to the destination environment with the same dictionary
    And the existing data in managed table "TTAB_TWO" in destination environment :
      | key | value | other     |
      | VVV | 333   | Other VVV |
      | III | 444   | Other III |
    And a commit ":construction: Destination commit 1" has been saved with all the new identified diff content in destination environment
    When the user do nothing more
    Then the commit ":construction: Destination commit 1" is added to commit list for current project in destination environment
    And the exported commit ":tada: Test commit init source" is not present in the destination environment

  Scenario: The dedicated merge diff with changes from destination environment is started when importing a package
    Given the commit ":tada: Test commit init source" has been saved and exported with all the identified initial diff content
    And the user accesses to the destination environment with the same dictionary
    And the existing data in managed table "TTAB_TWO" in destination environment :
      | key | value | other     |
      | VVV | 333   | Other VVV |
      | III | 444   | Other III |
    And a commit ":construction: Destination commit 1" has been saved with all the new identified diff content in destination environment
    When the user import the available source package
    Then a merge diff is running

  Scenario: The dedicated merge diff provides details on differences between imported package and local content - simple add content
    Given the commit ":tada: Test commit init source" has been saved and exported with all the identified initial diff content
    And the user accesses to the destination environment with the same dictionary
    And the existing data in managed table "TTAB_TWO" in destination environment :
      | key | value | other     |
      | VVV | 333   | Other VVV |
      | III | 444   | Other III |
    And a commit ":construction: Destination commit 1" has been saved with all the new identified diff content in destination environment
    And a merge diff has already been launched with the available source package
    And the merge diff is completed
    When the user access to merge commit page
    Then the merge commit content is rendered with these identified changes :
      | Table     | Key | Action | Need Resolve | Payload                                                                                                                                    |
      | TTAB_TWO  | VVV | ADD    | false        | VALUE:'333', OTHER:'Other VVV'                                                                                                             |
      | TTAB_TWO  | III | ADD    | false        | VALUE:'444', OTHER:'Other III'                                                                                                             |
      | TTAB_TWO  | JJJ | ADD    | true         | VALUE:'One', OTHER:'Other JJJ'                                                                                                             |
      | TTAB_TWO  | KKK | ADD    | true         | VALUE:'Two', OTHER:'Other KKK'                                                                                                             |
      | TTAB_FIVE | 1   | ADD    | true         | DATA:<a href="/ui/lob/WlZzM0wwUGlSVDd2emdZbEdDckFtcnFWbTY0M2RXMVp3c2haTlRObUVCYz0=" download="download">LOB</a>, SIMPLE:17.81              |
      | TTAB_FIVE | 2   | ADD    | true         | DATA:<a href="/ui/lob/TURPbmVyR2cwaWtGQVJLdmloWDBmRkQ4VjJtVXA0K0tIZnJqaTJCeVBLRT0=" download="download">LOB</a>, SIMPLE:17.82              |
      | TTAB_FIVE | 3   | ADD    | true         | DATA:<a href="/ui/lob/bUdiNG5wa1FiUnZSSnJKV3AvUUlwd0dQcVpURmtLaEkxRlU5bDlqTmoxTT0=" download="download">LOB</a>, SIMPLE:17.83              |
      | TTAB_SIX  | 7   | ADD    | true         | TEXT:<a href="/ui/lob/QXlvSkNtWk5RWHdrenQyWmNIZ0c4Y3B2VXVjc2JHbmFuVHVRdStwYUdPcz0=" download="download">TEXT</a>, DATE:2012-01-15 00:00:00 |
      | TTAB_SIX  | 8   | ADD    | true         | TEXT:<a href="/ui/lob/S09wazdEUDlpTG5BbHM1L1JvRjErS1JEeFdNRGFBK2VTazJiVUdvOGczZz0=" download="download">TEXT</a>, DATE:2005-07-08 00:00:00 |
      | TTAB_SIX  | 9   | ADD    | true         | TEXT:<a href="/ui/lob/K2Z1REFwVm0ycUh1OEJhU09Pa0tBdElDclRoYzVWTTlFU3pGTS9DL1ZHST0=" download="download">TEXT</a>, DATE:2021-12-25 00:00:00 |

  Scenario: Null keys are processed in merge using a special character - single key
    Given the commit ":tada: Test commit init" has been saved with all the identified initial diff content
    And a created parameter table with name "Table nullable key" for managed table "TTAB_THREE_KEYS" and columns selected as this :
      | name       | selection |
      | FIRST_KEY  | selected  |
      | SECOND_KEY | key       |
      | THIRD_KEY  | ignored   |
    And the existing data in managed table "TTAB_THREE_KEYS" :
      | secondKey    | firstKey         |
      | -null-       | something        |
      | -space-      | something else   |
      | -empty char- | something else 2 |
      | aaaa         | something aaaa   |
    And the user add new version "v2"
    And a new commit ":construction: Commit on v2" has been saved with all the new identified diff content
    And the user request an export of the commit with name ":construction: Commit on v2"
    And the user accesses to the destination environment with the same dictionary
    And no existing data in managed table "TTAB_TWO" in destination environment
    And no existing data in managed table "TTAB_FIVE" in destination environment
    And no existing data in managed table "TTAB_SIX" in destination environment
    And no existing data in managed table "TTAB_THREE_KEYS" in destination environment
    And a merge diff has already been launched with the available source package
    And the merge diff is completed
    When the user access to merge commit page
    Then the merge commit content is rendered with these identified changes :
      | Table           | Key          | Action | Need Resolve | Payload                      |
      | TTAB_THREE_KEYS |              | ADD    | true         | FIRST_KEY:'something'        |
      | TTAB_THREE_KEYS | -space-      | ADD    | true         | FIRST_KEY:'something else'   |
      | TTAB_THREE_KEYS | -empty char- | ADD    | true         | FIRST_KEY:'something else 2' |
      | TTAB_THREE_KEYS | aaaa         | ADD    | true         | FIRST_KEY:'something aaaa'   |

  Scenario: Null keys are processed in merge using a special character - composite key
    Given the commit ":tada: Test commit init" has been saved with all the identified initial diff content
    And a created parameter table with name "Table nullable key" for managed table "TTAB_THREE_KEYS" and columns selected as this :
      | name       | selection |
      | FIRST_KEY  | key       |
      | SECOND_KEY | key       |
      | THIRD_KEY  | key       |
    And the existing data in managed table "TTAB_THREE_KEYS" :
      | firstKey     | secondKey    | thirdKey     |
      | one          | -null-       | something    |
      | -space-      | -null-       | -empty char- |
      | -empty char- | -space-      | -null-       |
      | 4            | aaa          | -null-       |
      | 5            | 5            | 5            |
      | 6            | -empty char- | 6            |
    And the user add new version "v2"
    And a new commit ":construction: Commit on v2" has been saved with all the new identified diff content
    And the user request an export of the commit with name ":construction: Commit on v2"
    And the user accesses to the destination environment with the same dictionary
    And no existing data in managed table "TTAB_TWO" in destination environment
    And no existing data in managed table "TTAB_FIVE" in destination environment
    And no existing data in managed table "TTAB_SIX" in destination environment
    And no existing data in managed table "TTAB_THREE_KEYS" in destination environment
    And a merge diff has already been launched with the available source package
    And the merge diff is completed
    When the user access to merge commit page
    Then the merge commit content is rendered with these identified changes :
      | Table           | Key                   | Action | Need Resolve | Payload |
      | TTAB_THREE_KEYS | "one /   / something" | ADD    | true         |         |
      | TTAB_THREE_KEYS | "  /   / "            | ADD    | true         |         |
      | TTAB_THREE_KEYS | " /   /  "            | ADD    | true         |         |
      | TTAB_THREE_KEYS | 4 / aaa /             | ADD    | true         |         |
      | TTAB_THREE_KEYS | 5 / 5 / 5             | ADD    | true         |         |
      | TTAB_THREE_KEYS | 6 /  / 6              | ADD    | true         |         |

  Scenario: The dedicated merge diff is paginated and filtered - filtered by table sorted by key
    Given the commit ":tada: Test commit init source" has been saved and exported with all the identified initial diff content
    And the user accesses to the destination environment with the same dictionary
    And the existing data in managed table "TTAB_TWO" in destination environment :
      | key | value | other     |
      | VVV | 333   | Other VVV |
      | III | 444   | Other III |
    And a commit ":construction: Destination commit 1" has been saved with all the new identified diff content in destination environment
    And a merge diff has already been launched with the available source package
    And the merge diff is completed
    When the user access to merge commit page
    And apply a content filter criteria "TTAB_[^W]*" on "table"
    And apply a content sort criteria "ASC" on "key"
    Then the paginated merge commit content is rendered with these identified sorted changes :
      | Table     | Key | Action | Payload                                                                                                                                    |
      | TTAB_FIVE | 1   | ADD    | DATA:<a href="/ui/lob/WlZzM0wwUGlSVDd2emdZbEdDckFtcnFWbTY0M2RXMVp3c2haTlRObUVCYz0=" download="download">LOB</a>, SIMPLE:17.81              |
      | TTAB_FIVE | 2   | ADD    | DATA:<a href="/ui/lob/TURPbmVyR2cwaWtGQVJLdmloWDBmRkQ4VjJtVXA0K0tIZnJqaTJCeVBLRT0=" download="download">LOB</a>, SIMPLE:17.82              |
      | TTAB_FIVE | 3   | ADD    | DATA:<a href="/ui/lob/bUdiNG5wa1FiUnZSSnJKV3AvUUlwd0dQcVpURmtLaEkxRlU5bDlqTmoxTT0=" download="download">LOB</a>, SIMPLE:17.83              |
      | TTAB_SIX  | 7   | ADD    | TEXT:<a href="/ui/lob/QXlvSkNtWk5RWHdrenQyWmNIZ0c4Y3B2VXVjc2JHbmFuVHVRdStwYUdPcz0=" download="download">TEXT</a>, DATE:2012-01-15 00:00:00 |
      | TTAB_SIX  | 8   | ADD    | TEXT:<a href="/ui/lob/S09wazdEUDlpTG5BbHM1L1JvRjErS1JEeFdNRGFBK2VTazJiVUdvOGczZz0=" download="download">TEXT</a>, DATE:2005-07-08 00:00:00 |
      | TTAB_SIX  | 9   | ADD    | TEXT:<a href="/ui/lob/K2Z1REFwVm0ycUh1OEJhU09Pa0tBdElDclRoYzVWTTlFU3pGTS9DL1ZHST0=" download="download">TEXT</a>, DATE:2021-12-25 00:00:00 |

  Scenario: The dedicated merge diff content can be selected to prepare a merge commit
    Given the commit ":tada: Test commit init source" has been saved and exported with all the identified initial diff content
    And the user accesses to the destination environment with the same dictionary
    And the existing data in managed table "TTAB_TWO" in destination environment :
      | key | value | other     |
      | VVV | 333   | Other VVV |
      | III | 444   | Other III |
    And a commit ":construction: Destination commit 1" has been saved with all the new identified diff content in destination environment
    And a merge diff analysis has been started and completed with the available source package
    When the user select all prepared diff content for merge commit
    And the user accesses to preparation commit page
    Then all the diff preparation content is selected for commit
    And the commit type is "MERGED"

  Scenario: The selected merge diff content can be saved as a new merge commit
    Given the commit ":tada: Test commit init source" has been saved and exported with all the identified initial diff content
    And the user accesses to the destination environment with the same dictionary
    And the existing data in managed table "TTAB_TWO" in destination environment :
      | key | value | other     |
      | VVV | 333   | Other VVV |
      | III | 444   | Other III |
    And a commit ":construction: Destination commit 1" has been saved with all the new identified diff content in destination environment
    And a merge diff analysis has been started and completed with the available source package
    And the user has selected all content for merge commit
    And the user has specified a commit comment ":construction: merge commit test"
    When the user save the commit
    Then the commit ":construction: merge commit test" is added to commit list for current project
    And the commit ":construction: merge commit test" from current project is of type "MERGED"

  Scenario: The selected merge diff content updates are applied to destination table - simple add content with lobs
    Given the commit ":tada: Test commit init source" has been saved and exported with all the identified initial diff content
    And the user accesses to the destination environment with the same dictionary
    And the existing data in managed table "TTAB_TWO" in destination environment :
      | key | value | other     |
      | VVV | 333   | Other VVV |
      | III | 444   | Other III |
    And no existing data in managed table "TTAB_FIVE" in destination environment
    And no existing data in managed table "TTAB_SIX" in destination environment
    And a commit ":construction: Destination commit initial" has been saved with all the new identified diff content in destination environment
    And a merge diff analysis has been started and completed with the available source package
    And the user has selected all content for merge commit
    And the user has specified a commit comment ":construction: merge commit test with changes"
    When the user save the merge commit
    Then the saved merge commit content has these identified changes :
      | Table     | Key | Action | Payload                                                                                                                                    |
      | TTAB_TWO  | JJJ | ADD    | VALUE:'One', OTHER:'Other JJJ'                                                                                                             |
      | TTAB_TWO  | KKK | ADD    | VALUE:'Two', OTHER:'Other KKK'                                                                                                             |
      | TTAB_FIVE | 1   | ADD    | DATA:<a href="/ui/lob/WlZzM0wwUGlSVDd2emdZbEdDckFtcnFWbTY0M2RXMVp3c2haTlRObUVCYz0=" download="download">LOB</a>, SIMPLE:17.81              |
      | TTAB_FIVE | 2   | ADD    | DATA:<a href="/ui/lob/TURPbmVyR2cwaWtGQVJLdmloWDBmRkQ4VjJtVXA0K0tIZnJqaTJCeVBLRT0=" download="download">LOB</a>, SIMPLE:17.82              |
      | TTAB_FIVE | 3   | ADD    | DATA:<a href="/ui/lob/bUdiNG5wa1FiUnZSSnJKV3AvUUlwd0dQcVpURmtLaEkxRlU5bDlqTmoxTT0=" download="download">LOB</a>, SIMPLE:17.83              |
      | TTAB_SIX  | 7   | ADD    | TEXT:<a href="/ui/lob/QXlvSkNtWk5RWHdrenQyWmNIZ0c4Y3B2VXVjc2JHbmFuVHVRdStwYUdPcz0=" download="download">TEXT</a>, DATE:2012-01-15 00:00:00 |
      | TTAB_SIX  | 8   | ADD    | TEXT:<a href="/ui/lob/S09wazdEUDlpTG5BbHM1L1JvRjErS1JEeFdNRGFBK2VTazJiVUdvOGczZz0=" download="download">TEXT</a>, DATE:2005-07-08 00:00:00 |
      | TTAB_SIX  | 9   | ADD    | TEXT:<a href="/ui/lob/K2Z1REFwVm0ycUh1OEJhU09Pa0tBdElDclRoYzVWTTlFU3pGTS9DL1ZHST0=" download="download">TEXT</a>, DATE:2021-12-25 00:00:00 |
    And the data in managed table "TTAB_TWO" in destination environment is now :
      | key | value | other     |
      | JJJ | One   | Other JJJ |
      | KKK | Two   | Other KKK |
      | VVV | 333   | Other VVV |
      | III | 444   | Other III |
    And the data in managed table "TTAB_FIVE" in destination environment is now :
      | key | data                | simple |
      | 1   | ABCDEF1234567ABDDDD | 17.81  |
      | 2   | ABCDEF1234567ABEEEE | 17.82  |
      | 3   | ABCDEF1234567ABFFFF | 17.83  |
    And the data in managed table "TTAB_SIX" in destination environment is now :
      | identifier | text                                     | date       |
      | 7          | Ceci est un text enregistré dans un CLOB | 2012-01-15 |
      | 8          | Un autre text CLOB                       | 2005-07-08 |
      | 9          | Encore un autre                          | 2021-12-25 |

  Scenario: When a merge is started, a compatibility check on the dictionary can be processed - fail on different versions
    Given the existing data in managed table "TTAB_ONE" :
      | key | value | preset   | something |
      | 14  | AAA   | Preset 1 | AAA       |
      | 25  | BBB   | Preset 2 | BBB       |
      | 37  | CCC   | Preset 3 | CCC       |
      | 38  | DDD   | Preset 4 | DDD       |
      | 39  | EEE   | Preset 5 | EEE       |
    And the existing versions "v1"
    And this dictionary is added to current default domain :
      | entry name | table name | select clause | filter clause | key name | key type  |
      | Table One  | TTAB_ONE   | cur."VALUE"   | 1=1           | KEY      | PK_ATOMIC |
      | Table Two  | TTAB_TWO   | cur."VALUE"   | 1=1           | KEY      | PK_STRING |
    And the user add new version "v2"
    And this dictionary is modified to current default domain :
      | entry name | table name | select clause             | filter clause | key name | key type  |
      | Table One  | TTAB_ONE   | cur."VALUE", cur."PRESET" | 1=1           | KEY      | PK_ATOMIC |
      | Table Two  | TTAB_TWO   | cur."VALUE", cur."OTHER"  | 1=1           | KEY      | PK_STRING |
    And the user add new version "v3"
    And a new commit ":construction: Commit on v3" has been saved with all the new identified diff content
    And the user request an export of the commit with name ":construction: Commit on v3"
    And the user accesses to the destination environment with only the versions until "v2"
    And the feature "VALIDATE_VERSION_FOR_IMPORT" is enabled
    And the feature "CHECK_DICTIONARY_COMPATIBILITY_FOR_IMPORT" is disabled
    When the user import the available source package
    Then an error of type MERGE_DICT_NOT_COMPATIBLE is provided with this payload :
      """txt
      Referenced version "v3" is not managed locally for commit ":construction: Commit on v3"
      """

  Scenario: When a merge is started, a compatibility check on the dictionary can be processed - fail on incompatible tables
    Given the existing data in managed table "TTAB_ONE" :
      | key | value | preset   | something |
      | 14  | AAA   | Preset 1 | AAA       |
      | 25  | BBB   | Preset 2 | BBB       |
      | 37  | CCC   | Preset 3 | CCC       |
      | 38  | DDD   | Preset 4 | DDD       |
      | 39  | EEE   | Preset 5 | EEE       |
    And the existing versions "v1"
    And this dictionary is added to current default domain :
      | entry name | table name | select clause | filter clause | key name | key type  |
      | Table One  | TTAB_ONE   | cur."VALUE"   | 1=1           | KEY      | PK_ATOMIC |
      | Table Two  | TTAB_TWO   | cur."VALUE"   | 1=1           | KEY      | PK_STRING |
    And the user add new version "v2"
    And this dictionary is modified to current default domain :
      | entry name | table name | select clause             | filter clause | key name | key type  |
      | Table One  | TTAB_ONE   | cur."VALUE", cur."PRESET" | 1=1           | KEY      | PK_ATOMIC |
      | Table Two  | TTAB_TWO   | cur."VALUE", cur."OTHER"  | 1=1           | KEY      | PK_STRING |
    And the user add new version "v3"
    And a new commit ":construction: Commit on v3" has been saved with all the new identified diff content
    And the user request an export of the commit with name ":construction: Commit on v3"
    And the user accesses to the destination environment with only the versions until "v2"
    And the feature "VALIDATE_VERSION_FOR_IMPORT" is disabled
    And the feature "CHECK_DICTIONARY_COMPATIBILITY_FOR_IMPORT" is enabled
    When the user import the available source package
    Then an error of type MERGE_DICT_NOT_COMPATIBLE is provided with this payload :
      """txt
      Table "TTAB_ONE" : column "PRESET" used for commit ":construction: Commit on v3" has been modified,
      Table "TTAB_TWO" : column "OTHER" used for commit ":construction: Commit on v3" has been modified
      """

  Scenario: When a merge is started, a compatibility check on the dictionary can be processed - success on compatible tables even with different versions
    Given the existing data in managed table "TTAB_ONE" :
      | key | value | preset   | something |
      | 14  | AAA   | Preset 1 | AAA       |
      | 25  | BBB   | Preset 2 | BBB       |
      | 37  | CCC   | Preset 3 | CCC       |
      | 38  | DDD   | Preset 4 | DDD       |
      | 39  | EEE   | Preset 5 | EEE       |
    And the existing versions "v1"
    And this dictionary is added to current default domain :
      | entry name | table name | select clause | filter clause | key name | key type  |
      | Table One  | TTAB_ONE   | cur."VALUE"   | 1=1           | KEY      | PK_ATOMIC |
      | Table Two  | TTAB_TWO   | cur."VALUE"   | 1=1           | KEY      | PK_STRING |
    And the user add new version "v2"
    And this dictionary is modified to current default domain :
      | entry name | table name | select clause             | filter clause | key name | key type  |
      | Table One  | TTAB_ONE   | cur."VALUE", cur."PRESET" | 1=1           | KEY      | PK_ATOMIC |
      | Table Two  | TTAB_TWO   | cur."VALUE", cur."OTHER"  | 1=1           | KEY      | PK_STRING |
    And the user add new version "v3"
    And a new commit ":construction: Commit on v3" has been saved with all the new identified diff content
    And the user request an export of the commit with name ":construction: Commit on v3"
    And the user accesses to the destination environment with only the versions until "v2"
    And this dictionary on destination environment is modified to current default domain :
      | entry name | table name | select clause             | filter clause | key name | key type  |
      | Table One  | TTAB_ONE   | cur."VALUE", cur."PRESET" | 1=1           | KEY      | PK_ATOMIC |
      | Table Two  | TTAB_TWO   | cur."VALUE", cur."OTHER"  | 1=1           | KEY      | PK_STRING |
    And the user add new version "v3 dest" on destination environment
    And the feature "VALIDATE_VERSION_FOR_IMPORT" is disabled
    And the feature "CHECK_DICTIONARY_COMPATIBILITY_FOR_IMPORT" is enabled
    When the user import the available source package
    Then no error is provided
    And a merge diff is running

  Scenario: When a merge is started, a compatibility check on the dictionary can be processed - multiple failures
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
      | Table One  | TTAB_ONE   | cur."PRESET"  | 1=1           | VALUE    | PK_STRING |
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
    And this dictionary is modified to current default domain :
      | entry name | table name | select clause             | filter clause | key name | key type  |
      | Table One  | TTAB_ONE   | cur."VALUE", cur."PRESET" | 1=1           | KEY      | PK_ATOMIC |
      | Table Two  | TTAB_TWO   | cur."VALUE", cur."OTHER"  | 1=1           | KEY      | PK_STRING |
    And the user add new version "v3"
    And these changes are applied to table "TTAB_ONE" :
      | change | key | value | preset    | something |
      | add    | 78  | ZAA   | Preset 57 | ZAZ       |
      | add    | 79  | ZBB   | Preset 58 | ZAZ       |
    And a new commit ":construction: Commit on version v3 source" has been saved with all the new identified diff content
    And the user request an export of all the commits
    And the user accesses to the destination environment with only the versions until "v2"
    And this dictionary on destination environment is modified to current default domain :
      | entry name | table name | select clause             | filter clause | key name | key type  |
      | Table One  | TTAB_ONE   | cur."VALUE", cur."PRESET" | 1=1           | KEY      | PK_ATOMIC |
      | Table Two  | TTAB_TWO   | cur."OTHER"               | 1=1           | KEY      | PK_STRING |
    And the user add new version "v3 dest" on destination environment
    And the feature "VALIDATE_VERSION_FOR_IMPORT" is enabled
    And the feature "CHECK_DICTIONARY_COMPATIBILITY_FOR_IMPORT" is enabled
    When the user import the available source package
    Then an error of type MERGE_DICT_NOT_COMPATIBLE is provided with this payload :
      """txt
      Table "TTAB_ONE" : key definition used for commit ":construction: Commit on version v1 source" has been modified,
      Table "TTAB_TWO" : column "VALUE" used for commit ":construction: Commit on version v2 source" has been modified,
      Referenced version "v3" is not managed locally for commit ":construction: Commit on version v3 source"
      """

  Scenario: An option check if referenced commits are not present in destination environment - enabled fail on missing ref
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
    And the commit ":tada: Test commit init" has been saved with all the identified initial diff content
    And these changes are applied to table "TTAB_TWO" :
      | change | key | value | other     |
      | add    | LLL | Three | Other LLL |
      | add    | MMM | Four  | Other MMM |
    And a new commit ":construction: Update 1" has been saved with all the new identified diff content
    And the user has requested an export of the commit with name ":construction: Update 1"
    And the user accesses to the destination environment with the same dictionary
    And the feature "VALIDATE_MISSING_REF_COMMITS_FOR_IMPORT" is enabled
    When the user import the available source package
    Then an error is provided with this message :
      """txt
      Imported package is not compliant : the requested ref commit
      """
    And no diff is running

  Scenario: An option check if referenced commits are not present in destination environment - disabled allows missing ref
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
    And the commit ":tada: Test commit init" has been saved with all the identified initial diff content
    And these changes are applied to table "TTAB_TWO" :
      | change | key | value | other     |
      | add    | LLL | Three | Other LLL |
      | add    | MMM | Four  | Other MMM |
    And a new commit ":construction: Update 1" has been saved with all the new identified diff content
    And the user has requested an export of the commit with name ":construction: Update 1"
    And the user accesses to the destination environment with the same dictionary
    And the feature "VALIDATE_MISSING_REF_COMMITS_FOR_IMPORT" is disabled
    When the user import the available source package
    Then no error is provided
    And a merge diff is running



