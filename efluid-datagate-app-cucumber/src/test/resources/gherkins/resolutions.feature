#noinspection NonAsciiCharacters
Feature: The imported backlog is merged regarding specified resolution rules

  Scenario: Merge are resolved using specified rules - commit without history - same source, same change
    Given the existing data in managed table "TTAB_TWO" :
      | key | value | other  |
      | A   | 1     | Same a |
      | B   | 2     | Same b |
    And the commit "source init" has been saved with all the identified initial diff content
    And these changes are applied to table "TTAB_TWO" :
      | change | key | value | other      |
      | update | A   | 1     | Same a upd |
      | delete | B   |       |            |
      | add    | C   | 3     | Same c add |
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
      | add    | C   | 3     | Same c add |
    And a commit "destination update 1" has been saved with all the new identified diff content in destination environment
    And a merge diff analysis has been started and completed with the available source package
    When the user access to merge commit page
    Then the merge commit content has these resolution details :
      | Table    | Key | Their Act | Mine Act | Res. Act | Res. Payload                  | Res. Previous | Need Act | Rule                              |
      | TTAB_TWO | A   | UPDATE    | ADD      | ADD      | VALUE:'1', OTHER:'Same a upd' |               | false    | UPDATE their - ADD mine - similar |
      | TTAB_TWO | C   | ADD       | ADD      | ADD      | VALUE:'3', OTHER:'Same c add' |               | false    | ADD - same value                  |

  Scenario: Merge are resolved using specified rules - commit without history - same source, different change
    Given the existing data in managed table "TTAB_TWO" :
      | key | value | other  |
      | A   | 1     | Same a |
      | B   | 2     | Same b |
    And the commit "source init" has been saved with all the identified initial diff content
    And these changes are applied to table "TTAB_TWO" :
      | change | key | value | other      |
      | update | A   | 1     | Same a upd |
      | delete | B   |       |            |
      | add    | C   | 3     | Same c add |
    And a new commit "source update 1" has been saved with all the new identified diff content
    And the user has requested an export of the commit with name "source update 1"
    And the user accesses to the destination environment with the same dictionary
    And the existing data in managed table "TTAB_TWO" in destination environment :
      | key | value | other  |
      | A   | 1     | Same a |
      | B   | 2     | Same b |
    And a commit "destination init" has been saved with all the new identified diff content in destination environment
    And these changes are applied to table "TTAB_TWO" in destination environment :
      | change | key | value | other           |
      | update | A   | 1     | Different a upd |
      | delete | B   |       |                 |
      | add    | C   | 3     | Different c add |
    And a commit "destination update 1" has been saved with all the new identified diff content in destination environment
    And a merge diff analysis has been started and completed with the available source package
    When the user access to merge commit page
    Then the merge commit content has these resolution details :
      | Table    | Key | Their Act | Mine Act | Res. Act | Res. Payload                          | Res. Previous                      | Need Act | Rule                                |
      | TTAB_TWO | A   | UPDATE    | ADD      | UPDATE   | OTHER:'Different a upd'=>'Same a upd' | VALUE:'1', OTHER:'Different a upd' | true     | UPDATE their - ADD mine - different |
      | TTAB_TWO | C   | ADD       | ADD      | UPDATE   | OTHER:'Different c add'=>'Same c add' | VALUE:'3', OTHER:'Different c add' | true     | ADD - different value               |

  Scenario: Merge are resolved using specified rules - commit with history - various changes
    Given the existing data in managed table "TTAB_TWO" :
      | key | value | other  |
      | A   | 1     | Same a |
      | B   | 2     | Same b |
      | C   | 3     | Same c |
    And the commit "source init" has been saved with all the identified initial diff content
    And the user has requested an export starting by the commit with name "source init"
    And these changes are applied to table "TTAB_TWO" :
      | change | key | value | other       |
      | update | A   | 1     | Same a upd2 |
      | update | C   | 3     | Same c upd2 |
      | add    | D   | 4     | Same d add  |
    And a new commit "source update 1" has been saved with all the new identified diff content
    And these changes are applied to table "TTAB_TWO" :
      | change | key | value | other       |
      | update | A   | 1     | Same a upd3 |
      | delete | C   |       |             |
      | update | D   | 4     | Same d upd2 |
    And a new commit "source update 2" has been saved with all the new identified diff content
    And the user has requested an export of the commit with name "source update 2"
    And the user accesses to the destination environment with the same dictionary
    And the exported package for commit "source init" has been merged in destination environment
    And these changes are applied to table "TTAB_TWO" in destination environment :
      | change | key | value | other            |
      | update | A   | 1     | Different a upd1 |
      | delete | C   |       |                  |
      | add    | D   | 4     | Same d add       |
    And a commit "destination update 1" has been saved with all the new identified diff content in destination environment
    And a merge diff analysis has been started and completed with the package of commit "source update 2" created a moment after
    When the user access to merge commit page
    Then the merge commit content has these resolution details :
      | Table    | Key | Their Act | Mine Act | Res. Act | Res. Payload                            | Res. Previous                       | Need Act | Rule                                          |
      | TTAB_TWO | A   | UPDATE    | UPDATE   | UPDATE   | OTHER:'Different a upd1'=>'Same a upd3' | VALUE:'1', OTHER:'Different a upd1' | true     | UPDATE - different value - different previous |
      | TTAB_TWO | D   | UPDATE    | ADD      | UPDATE   | OTHER:'Same d add'=>'Same d upd2'       | VALUE:'4', OTHER:'Same d add'       | true     | UPDATE their - ADD mine - different           |

  Scenario: Some resolution rules requires to record a specific warning on processed merge - requested cases
    Given the existing data in managed table "TTAB_TWO" :
      | key | value | other  |
      | A   | 1     | Same a |
    And the commit "source init" has been saved with all the identified initial diff content
    And the user has requested an export starting by the commit with name "source init"
    And these changes are applied to table "TTAB_TWO" :
      | change | key | value | other       |
      | update | A   | 1     | Same a upd2 |
      | add    | C   | 3     | Same c add  |
      | add    | D   | 4     | Same d add  |
    And a new commit "source update 1" has been saved with all the new identified diff content
    And these changes are applied to table "TTAB_TWO" :
      | change | key | value | other       |
      | update | A   | 1     | Same a upd3 |
      | add    | B   | 2     | Same b add  |
      | update | C   | 3     | Same c upd2 |
      | delete | D   |       |             |
    And a new commit "source update 2" has been saved with all the new identified diff content
    And the user has requested an export of the commit with name "source update 2"
    And the user accesses to the destination environment with the same dictionary
    And the feature "RECORD_IMPORT_WARNINGS" is enabled
    And the exported package for commit "source init" has been merged in destination environment
    And these changes are applied to table "TTAB_TWO" in destination environment :
      | change | key | value | other           |
      | update | A   | 1     | Different a upd |
      | add    | B   | 2     | Different b add |
    And a commit "destination update 1" has been saved with all the new identified diff content in destination environment
    And a merge diff analysis has been started and completed with the package of commit "source update 2" created a moment after
    When the user access to merge commit page
    Then these warnings are recorded for the merge of export of commit "source update 2":
      | Table    | Key | Code                                                       | Message                                                                                                                                            |
      | TTAB_TWO | A   | Warning from UPDATE - different value - different previous | UPDATE "OTHER:'Different a upd'=>'Same a upd3'" : La valeur de la ligne modifiée est différente entre les données locales et les données importées |
      | TTAB_TWO | B   | Warning from ADD - different value                         | UPDATE "OTHER:'Different b add'=>'Same b add'" : La ligne existait déjà avec une valeur différente                                                 |
      | TTAB_TWO | C   | Warning from UPDATE - only their - not exists              | no action : La ligne mise à jour n'existe pas localement                                                                                           |
      | TTAB_TWO | D   | Warning from REMOVE - only their - not exists              | no action : La ligne supprimée n'existait pas                                                                                                      |