Feature: The dependencies between 2 commits can be displayed

  From 2 selected commits it is possible to have the matching similar diff entries (same table, same key), and to display them.
  It is also possible to select 2 different commit exported files (.par) and to compare their diff contents, in a similare way.

  Background:
    Given the existing data in managed table "TTAB_ONE" :
      | key | value | preset   | something |
      | 14  | AAA   | Preset 1 | AAA       |
      | 25  | BBB   | Preset 2 | BBB       |
      | 37  | CCC   | Preset 3 | CCC       |
    And the existing data in managed table "TTAB_TWO" :
      | key | value | other     |
      | JJJ | One   | Other JJJ |
      | VVV | Two   | Other VVV |
      | ZZZ | Three | Other ZZZ |
    And the existing data in managed table "TTAB_THREE" :
      | key   | value | other   |
      | 11111 | A     | Other A |
      | 22222 | B     | Other B |
      | 33333 | C     | Other C |
    Given the commit "Commit 1" has been saved with all the identified initial diff content
    And these changes are applied to table "TTAB_ONE" :
      | change | key | value | preset          | something         |
      | delete | 14  | AAA   |                 |                   |
      | update | 37  | CCC   | Preset 3 update | CCC updated       |
      | add    | 38  | DDD   | Recreate line   | DDD Recreate line |
      | add    | 99  | NEW   | New line        | New line          |
    And these changes are applied to table "TTAB_TWO" :
      | change | key  | value       | other              |
      | update | VVV  | TwoModified | Other VVV modified |
      | add    | JJJ2 | OneBis      | Other JJJ2         |
      | add    | VVV2 | TwoBis      | Other VVV2         |
      | add    | VVV3 | ThreeBis    | Other 333          |
    And a new commit "Commit 2" has been saved with all the new identified diff content
    And these changes are applied to table "TTAB_TWO" :
      | change | key  | value          | other                 |
      | update | VVV  | TwoModifiedBis | Other VVV modifiedBis |
      | update | ZZZ  | ThreeModified  | Other ZZZ modified    |
      | add    | JJJ2 | One            | Other JJJ2            |
      | add    | VVV2 | Two            | Other VVV2            |
      | add    | VVV3 | Three          | Other 333             |
    And these changes are applied to table "TTAB_THREE" :
      | change | key   | value | other           |
      | delete | 33333 | C     |                 |
      | update | 11111 | A     | Other A updated |
      | update | 22222 | B     | Other B updated |
      | add    | 44444 | VVVD  | Other VVVD      |
    And a new commit "Commit 3" has been saved with all the new identified diff content

  Scenario: The analysis for dependencies for two distinct commits can be started
    When the user access to list of commits
    And the user request a dependency analysis between "Commit 1" and "Commit 3"
    Then the provided template is dependency analysis running
    And a dependency analysis is running

  Scenario: The analysis for dependencies for two distinct commits can be accessed once completed - test 1
    When the user access to list of commits
    And the user request a dependency analysis between "Commit 1" and "Commit 3"
    And the dependency analysis has been completed
    Then the provided template is dependency analysis completed
    And these dependencies are identified between the commits :
      | Table      | Key | Action | Payload                                                                   |
      | TTAB_TWO   | VVV | UPDATE | VALUE:'Two'=>'TwoModifiedBis', OTHER:'Other VVV'=>'Other VVV modifiedBis' |
      | TTAB_TWO   | ZZZ | UPDATE | VALUE:'Three'=>'ThreeModified', OTHER:'Other ZZZ'=>'Other ZZZ modified'   |
      | TTAB_THREE | A   | UPDATE | OTHER:'Other A'=>'Other A updated'                                        |
      | TTAB_THREE | B   | UPDATE | OTHER:'Other B'=>'Other B updated'                                        |
      | TTAB_THREE | C   | REMOVE |                                                                           |

  Scenario: The analysis for dependencies for two distinct commits can be accessed once completed - test 2
    When the user access to list of commits
    And the user request a dependency analysis between "Commit 1" and "Commit 2"
    And the dependency analysis has been completed
    Then the provided template is dependency analysis completed
    And these dependencies are identified between the commits :
      | Table    | Key | Action | Payload                                                              |
      | TTAB_ONE | AAA | REMOVE |                                                                      |
      | TTAB_ONE | CCC | UPDATE | PRESET:'Preset 3'=>'Preset 3 update', SOMETHING:'CCC'=>'CCC updated' |
      | TTAB_TWO | VVV | UPDATE | VALUE:'Two'=>'TwoModified', OTHER:'Other VVV'=>'Other VVV modified'  |

  Scenario: The history for one dependency between 2 commits can be accessed - test 1
    When the user access to list of commits
    And the user request a dependency analysis between "Commit 1" and "Commit 3"
    And the dependency analysis has been completed
    And the user select the history for dependency on key "VVV" for table "TTAB_TWO"
    Then the provided template is dependency history
    And this history is provided for the selected dependency :
      | Commit   | Commit type | Change type | Change payload                                                                             |
      | Commit 2 | LOCAL       | UPDATE      | VALUE:'Two'=>'TwoModified', OTHER:'Other VVV'=>'Other VVV modified'                        |
      | Commit 3 | LOCAL       | UPDATE      | VALUE:'TwoModified'=>'TwoModifiedBis', OTHER:'Other VVV modified'=>'Other VVV modifiedBis' |

  Scenario: The history for one dependency between 2 commits can be accessed - test 2
    When the user access to list of commits
    And the user request a dependency analysis between "Commit 1" and "Commit 3"
    And the dependency analysis has been completed
    And the user select the history for dependency on key "C" for table "TTAB_THREE"
    Then the provided template is dependency history
    And this history is provided for the selected dependency :
      | Commit   | Commit type | Change type | Change payload |
      | Commit 3 | LOCAL       | REMOVE      |                |