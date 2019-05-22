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

  Scenario: The selected diff content can be saved as a new commit
    Given a diff analysis has been started and completed
    And the user has selected all content for commit
    And the user has specified a commit comment ":construction: Test commit"
    When the user save the commit
    Then the commit ":construction: Test commit" is added to commit list for current project
