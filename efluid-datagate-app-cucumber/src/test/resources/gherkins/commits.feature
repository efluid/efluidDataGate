Feature: The commit can be saved and are historised

  After a diff, content of commit can be saved in application managed index. For each standard diff content line, user can say "keep, revert or ignore"

  Background:
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
    And the existing data in managed table "TTAB_THREE" :
      | key   | value | other   |
      | 11111 | A     | Other A |
      | 22222 | B     | Other B |
      | 33333 | C     | Other C |

  Scenario: From the commit diff the user can access to saving page for commit completion.
    Given a diff analysis has been started and completed
    When the user accesses to preparation commit page
    Then the provided template is commit saving
    And the commit comment is empty

  Scenario: The diff content is not selected by default for commit preparation
    Given a diff analysis has been started and completed
    When the user do not select any prepared diff content for commit
    And the user accesses to preparation commit page
    Then all the diff preparation content is ignored by default

  Scenario: The diff content can be fully selected for commit preparation
    Given a diff analysis has been started and completed
    When the user select all prepared diff content for commit
    And the user accesses to preparation commit page
    Then all the diff preparation content is selected for commit

  Scenario: The default preparing commit type is LOCAL
    Given a diff analysis has been started and completed
    When the user select all prepared diff content for commit
    And the user accesses to preparation commit page
    Then the commit type is "LOCAL"

  Scenario: The selected diff content can be saved as a new commit
    Given a diff analysis has been started and completed
    And the user has selected all content for commit
    And the user has specified a commit comment ":construction: Test commit"
    When the user save the merge commit
    Then the commit ":construction: Test commit" is added to commit list for current project
    And the saved commit content has these identified changes :
      | Table      | Key | Action | Payload                            |
      | TTAB_ONE   | AAA | ADD    | PRESET:'Preset 1', SOMETHING:'AAA' |
      | TTAB_ONE   | BBB | ADD    | PRESET:'Preset 2', SOMETHING:'BBB' |
      | TTAB_ONE   | CCC | ADD    | PRESET:'Preset 3', SOMETHING:'CCC' |
      | TTAB_ONE   | DDD | ADD    | PRESET:'Preset 4', SOMETHING:'DDD' |
      | TTAB_ONE   | EEE | ADD    | PRESET:'Preset 5', SOMETHING:'EEE' |
      | TTAB_TWO   | JJJ | ADD    | VALUE:'One', OTHER:'Other JJJ'     |
      | TTAB_TWO   | KKK | ADD    | VALUE:'Two', OTHER:'Other KKK'     |
      | TTAB_THREE | A   | ADD    | OTHER:'Other A'                    |
      | TTAB_THREE | B   | ADD    | OTHER:'Other B'                    |
      | TTAB_THREE | C   | ADD    | OTHER:'Other C'                    |

  Scenario: The lob data is associated to a saved commit content - blob content
    Given the existing data in managed table "TTAB_FIVE" :
      | key | data                | simple |
      | 1   | ABCDEF1234567ABDDDD | 17.81  |
      | 2   | ABCDEF1234567ABEEEE | 17.82  |
      | 3   | ABCDEF1234567ABFFFF | 17.83  |
    And a diff analysis has been started and completed
    And the user has selected all content for commit
    And the user has specified a commit comment ":construction: Test commit"
    When the user save the commit
    And the saved commit content has these associated lob data :
      | data                | hash                                         |
      | ABCDEF1234567ABDDDD | ZVs3L0PiRT7vzgYlGCrAmrqVm643dW1ZwshZNTNmEBc= |
      | ABCDEF1234567ABEEEE | MDOnerGg0ikFARKvihX0fFD8V2mUp4+KHfrji2ByPKE= |
      | ABCDEF1234567ABFFFF | mGb4npkQbRvRJrJWp/QIpwGPqZTFkKhI1FU9l9jNj1M= |

  Scenario: The lob data is associated to a saved commit content - clob content
    Given the existing data in managed table "TTAB_SIX" :
      | identifier | text                                     | date       |
      | 7          | Ceci est un text enregistré dans un CLOB | 2012-01-15 |
      | 8          | Un autre text CLOB                       | 2005-07-08 |
      | 9          | Encore un autre                          | 2021-12-25 |
    And a diff analysis has been started and completed
    And the user has selected all content for commit
    And the user has specified a commit comment ":construction: Test commit"
    When the user save the commit
    And the saved commit content has these associated lob data :
      | data                                     | hash                                         |
      | Ceci est un text enregistré dans un CLOB | AyoJCmZNQXwkzt2ZcHgG8cpvUucsbGnanTuQu+paGOs= |
      | Un autre text CLOB                       | KOpk7DP9iLnAls5/RoF1+KRDxWMDaA+eSk2bUGo8g3g= |
      | Encore un autre                          | +fuDApVm2qHu8BaSOOkKAtICrThc5VM9ESzFM/C/VGI= |

  Scenario: A saved commit can be associated to attached documents. The attached documents are then managed in backlog database
    Given a diff analysis has been started and completed
    And the user has selected all content for commit
    And the user has specified a commit comment ":construction: Test commit with attachment"
    And the user has attached these documents to the commit:
      | title         | type      | size  |
      | attachment.md | MD_FILE   | 1500  |
      | script.sql    | SQL_FILE  | 7800  |
      | something.txt | TEXT_FILE | 45000 |
    When the user save the commit
    Then the commit ":construction: Test commit with attachment" is added to commit list for current project
    And these attachment documents are associated to the commit in the current project backlog:
      | title         | type      |
      | attachment.md | MD_FILE   |
      | script.sql    | SQL_FILE  |
      | something.txt | TEXT_FILE |


Scenario:
Given a list of commit
When the user access to list of commits
Then the commits have to be ordered into their Created Time order