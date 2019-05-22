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

  Scenario: A first commit for a single table contains all table content - simple fields
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

  Scenario: A first commit for a single table contains all table content - blob fields
    Given the existing data in managed table "TTAB_FIVE" :
      | key | data                | simple |
      | 1   | ABCDEF1234567ABDDDD | 17.81  |
      | 2   | ABCDEF1234567ABEEEE | 17.82  |
      | 3   | ABCDEF1234567ABFFFF | 17.83  |
    And a diff analysis can be started and completed
    And a diff has already been launched
    And the diff is completed
    When the user access to diff commit page
    Then the commit content is rendered with these identified changes :
      | Table     | Key | Action | Payload                                                                                                        |
      | TTAB_FIVE | 1   | ADD    | DATA:<a href="/lob/ZVs3L0PiRT7vzgYlGCrAmrqVm643dW1ZwshZNTNmEBc%3D" download="download">LOB</a>, SIMPLE:17.81   |
      | TTAB_FIVE | 2   | ADD    | DATA:<a href="/lob/MDOnerGg0ikFARKvihX0fFD8V2mUp4%2BKHfrji2ByPKE%3D" download="download">LOB</a>, SIMPLE:17.82 |
      | TTAB_FIVE | 3   | ADD    | DATA:<a href="/lob/mGb4npkQbRvRJrJWp%2FQIpwGPqZTFkKhI1FU9l9jNj1M%3D" download="download">LOB</a>, SIMPLE:17.83 |
    And the commit content has these associated lob data :
      | data                | hash                                         |
      | ABCDEF1234567ABDDDD | ZVs3L0PiRT7vzgYlGCrAmrqVm643dW1ZwshZNTNmEBc= |
      | ABCDEF1234567ABEEEE | MDOnerGg0ikFARKvihX0fFD8V2mUp4+KHfrji2ByPKE= |
      | ABCDEF1234567ABFFFF | mGb4npkQbRvRJrJWp/QIpwGPqZTFkKhI1FU9l9jNj1M= |

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

  Scenario: A new commit added after a initial diff will contains the identified changes
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
    And the commit ":construction: Test commit with attachment" has been saved with all the identified initial diff content
    And these changes are applied to table "TTAB_ONE" :
      | change | key | value | preset           | something |
      | add    | 76  | ZZZ   | Preset 76        | ZZZX      |
      | delete | 37  |       |                  |           |
      | update | 38  | DDD   | Preset 4 updated | DDD       |
    And these changes are applied to table "TTAB_TWO" :
      | change | key | value  | other             |
      | update | JJJ | One    | Other JJJ updated |
      | add    | IJK | Le new | newnew            |
    And a diff has already been launched
    And the diff is completed
    When the user access to diff commit page
    Then the commit content is rendered with these identified changes :
      | Table    | Key | Action | Payload                                |
      | TTAB_ONE | ZZZ | ADD    | PRESET:'Preset 76', SOMETHING:'ZZZX'   |
      | TTAB_ONE | CCC | REMOVE |                                        |
      | TTAB_ONE | DDD | UPDATE | PRESET:'Preset 4'=>'Preset 4 updated'  |
      | TTAB_TWO | JJJ | UPDATE | OTHER:'Other JJJ'=>'Other JJJ updated' |
      | TTAB_TWO | IJK | ADD    | VALUE:'Le new', OTHER:'newnew'         |
