#noinspection NonAsciiCharacters
Feature: Revert commits can be created from existing commits

  A revert commit can be created from existing commits

  Background:
    Given the existing data in managed table "TTAB_TWO" :
      | key | value    | other |
      | AAA | Preset 1 | aaa   |
      | BBB | Preset 2 | bbb   |
    And the commit ":tada: Init" has been saved with all the identified initial diff content
    And these changes are applied to table "TTAB_TWO" :
      | change | key | value            | other   |
      | delete | AAA | Preset 1         | aaa     |
      | update | BBB | Preset 2 changed | bbb chg |
      | add    | CCC | Preset 3         | ccc     |
    And a new commit ":construction: Update 1" has been saved with all the new identified diff content
    And these changes are applied to table "TTAB_TWO" :
      | change | key | value                | other   |
      | update | BBB | Preset 2 changed bis | bbb chg |
      | delete | CCC | Preset 3             | ccc     |
      | add    | DDD | Preset 4             | ddd     |
      | add    | EEE | Preset 5             | eee     |
    And a new commit ":construction: Update 2" has been saved with all the new identified diff content

  Scenario: A revert commit can be created only from the last "non reverted" commit
    When the user access to list of commits
    Then a revert commit can be created only for commit ":construction: Update 2"

  Scenario: A revert commit created from a compliant commit is managed with a standard diff selection screen
    Given the user access to list of commits
    When the user ask for a revert of commit ":construction: Update 2"
    Then the provided template is revert running

  Scenario: A revert commit contains the reversed changes of the select source commit
    Given the user access to list of commits
    When the user ask for a revert of commit ":construction: Update 2"
    And the diff is completed
    When the user access to revert commit page
    Then the commit content is rendered with these identified changes :
      | Table    | Key | Action | Payload                                          |
      | TTAB_TWO | BBB | UPDATE | VALUE:'Preset 2 changed bis'=>'Preset 2 changed' |
      | TTAB_TWO | CCC | ADD    | VALUE:'Preset 3', OTHER:'ccc'                    |
      | TTAB_TWO | DDD | REMOVE |                                                  |
      | TTAB_TWO | EEE | REMOVE |                                                  |
    And the commit type is "REVERT"

  Scenario: A stored revert commit apply the changes to the local database
    Given the user access to list of commits
    When the user ask for a revert of commit ":construction: Update 2"
    And the diff is completed
    And the user has selected all content for commit
    And the user has specified a commit comment ":rewind: Revert to update 1"
    When the user save the commit
    Then the data in managed table "TTAB_TWO" is now :
      | key | value            | other   |
      | BBB | Preset 2 changed | bbb chg |
      | CCC | Preset 3         | ccc     |

  Scenario: New commits coming after a revert has been created are processed as standard commits
    Given the user access to list of commits
    And the user have asked for a revert of commit ":construction: Update 2"
    And the proposed diff has been saved as commit ":rewind: Revert to update 1"
    And no changes are applied in current environment
    And a diff has already been launched
    And the diff is completed
    When the user access to diff commit page
    Then no commit content has been identified

