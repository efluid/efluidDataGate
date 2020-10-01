#noinspection NonAsciiCharacters
Feature: The imported backlog is merged regarding specified resolution rules

  Scenario: Merge are resolved using specified rules - commit without history - same source
    Given the existing data in managed table "TTAB_TWO" :
      | key | value | other  |
      | A   | 1     | Same a |
      | B   | 2     | Same b |
    And the commit "source init" has been saved with all the identified initial diff content
    And these changes are applied to table "TTAB_TWO" :
      | change | key | value | other      |
      | update | A   | 1     | Same a upd |
      | delete | B   |       |            |
    And a new commit "source update 1" has been saved with all the new identified diff content
    And the user has requested an export of the commit with name "source update 1"
    And the user accesses to the destination environment with the same dictionary
    And the existing data in managed table "TTAB_TWO" in destination environment :
      | key | value | other  |
      | A   | 1     | Same a |
      | B   | 2     | Same b |
    And a commit "destination init" has been saved with all the new identified diff content in destination environment
    And these changes are applied to table "TTAB_TWO" in destination environment :
      | change | key | value | other      |
      | update | A   | 1     | Same a upd |
      | delete | B   |       |            |
    And a commit "destination update 1" has been saved with all the new identified diff content in destination environment
    And a merge diff analysis has been started and completed with the available source package
    When the user access to merge commit page
    Then the merge commit content has these resolution details :
      | Table    | Key | Their Act | Mine Act | Res. Act | Res. Payload                  | Res. Previous | Need Act | Rule                              |
      | TTAB_TWO | A   | UPDATE    | ADD      | ADD      | VALUE:'1', OTHER:'Same a upd' |               | false    | UPDATE their - ADD mine - similar |