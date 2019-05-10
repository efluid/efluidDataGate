Feature: The update on parameter tables can be followed, checked and stored as commits.

  The features are similar to the way GIT can be used to manage updates on source files, but adapted
  for database contents. The content is first identified, by looking on changes and comparing them
  to a local index, and then "stored" in commits. The commits can be shared with export / import files
  (similar to GIT push / pull processes).

  A diff analysis can be started if :
  * Dictionary is initialized
  * Valid version is set
  * User is authenticated

  Scenario: A diff analysis can be launched
    Given a diff analysis can be started
    And no diff is running
    When the user access to diff launch page
    Then the provided template is diff running
    And a diff is running

  Scenario: Only one diff can be launched for current project
    Given a diff analysis can be started
    And a diff has already been launched
    When the user access to diff launch page
    Then an alert says that the diff is still running

  Scenario: A first commit for a single table contains all table content
    Given the existing data in managed table "TTAB_ONE" :
      | key | value | preset       | something |
      | 1   | One   | Preset One   | AAA       |
      | 2   | Two   | Preset Two   | BBB       |
      | 3   | Three | Preset Three | CCC       |
    And a diff analysis can be started and completed
    And a diff has already been launched
    And the diff is completed
    When the user access to diff commit page
    Then the commit content is rendered with these identified changes :
      | Table    | Key   | Payload                                |
      | TTAB_ONE | One   | PRESET:'Preset One', SOMETHING:'AAA'   |
      | TTAB_ONE | Two   | PRESET:'Preset Two', SOMETHING:'BBB'   |
      | TTAB_ONE | Three | PRESET:'Preset Three', SOMETHING:'CCC' |

  Scenario: A first commit for multiple tables contains all table contents
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
    And a diff analysis can be started and completed
    And a diff has already been launched
    And the diff is completed
    When the user access to diff commit page
    Then the commit content is rendered with these identified changes :
      | Table      | Key | Payload                            |
      | TTAB_ONE   | AAA | PRESET:'Preset 1', SOMETHING:'AAA' |
      | TTAB_ONE   | BBB | PRESET:'Preset 2', SOMETHING:'BBB' |
      | TTAB_ONE   | CCC | PRESET:'Preset 3', SOMETHING:'CCC' |
      | TTAB_ONE   | DDD | PRESET:'Preset 4', SOMETHING:'DDD' |
      | TTAB_ONE   | EEE | PRESET:'Preset 5', SOMETHING:'EEE' |
      | TTAB_TWO   | JJJ | VALUE:'One', OTHER:'Other JJJ'     |
      | TTAB_TWO   | KKK | VALUE:'Two', OTHER:'Other KKK'     |
      | TTAB_THREE | A   | OTHER:'Other A'                    |
      | TTAB_THREE | B   | OTHER:'Other B'                    |
      | TTAB_THREE | C   | OTHER:'Other C'                    |
