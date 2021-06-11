#noinspection NonAsciiCharacters
Feature: Some warnings are recorded from various entry points and are available to a technical user

  Scenario: The merge names with warnings from merge resolution rules are available through a dedicated REST service
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
    And the user has selected all content for merge commit
    And the user has specified a commit comment ":construction: merged with warnings"
    And the user has saved the merge commit
    When the user access to the merge anomaly names rest service
    Then there are the commit with listed merge anomalies:
      | exported commit |
      | source update 2 |

  Scenario: The warnings from merge resolution rules for a processed merge are available through a dedicated REST service
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
    And the user has selected all content for merge commit
    And the user has specified a commit comment ":construction: merged with warnings"
    And the user has saved the merge commit
    When the user asks for the merge anomalies rest service for the exported commit "source update 2"
    Then there are the listed merge anomalies:
      | Table    | Key | Code                                                       | Message                                                                                                                                            |
      | TTAB_TWO | A   | Warning from UPDATE - different value - different previous | UPDATE "OTHER:'Different a upd'=>'Same a upd3'" : La valeur de la ligne modifiée est différente entre les données locales et les données importées |
      | TTAB_TWO | B   | Warning from ADD - different value                         | UPDATE "OTHER:'Different b add'=>'Same b add'" : La ligne est déjà ajoutée avec une valeur différente                                              |
      | TTAB_TWO | C   | Warning from UPDATE - only their - not exists              | no action : La ligne mise à jour n'existe pas localement                                                                                           |
      | TTAB_TWO | D   | Warning from REMOVE - only their - not exists              | no action : La ligne supprimée n'existait pas                                                                                                      |


