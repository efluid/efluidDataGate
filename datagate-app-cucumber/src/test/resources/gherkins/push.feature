Feature: The backlog can be exported

  The backlog content (commit with indexes, lob content and attachments) can be exported in a standardized format, ready to be important in
  a distant managed instance

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

  Scenario: All the existing commits from the backlog can be exported in a single archive
    Given the commit ":tada: Test commit init" has been saved with all the identified initial diff content
    And these changes are applied to table "TTAB_ONE" :
      | change | key | value | preset   | something |
      | add    | 14  | AAA   | Preset 1 | AAA       |
      | add    | 25  | BBB   | Preset 2 | BBB       |
      | delete | 37  | CCC   | Preset 3 | CCC       |
      | update | 38  | DDD   | Preset 4 | DDD       |
    And these changes are applied to table "TTAB_TWO" :
      | change | key | value | other     |
      | add    | JJJ | One   | Other JJJ |
      | add    | KKK | Two   | Other KKK |
    And a new commit ":construction: Update 1" has been saved with all the new identified diff content
    When the user request an export of all the commits
    Then the export package contains 2 commit contents

  Scenario: A saved commit can be exported alone
    Given the commit ":tada: Test commit init" has been saved with all the identified initial diff content
    And these changes are applied to table "TTAB_ONE" :
      | change | key | value | preset   | something |
      | add    | 14  | AAA   | Preset 1 | AAA       |
      | add    | 25  | BBB   | Preset 2 | BBB       |
      | delete | 37  | CCC   | Preset 3 | CCC       |
      | update | 38  | DDD   | Preset 4 | DDD       |
    And these changes are applied to table "TTAB_TWO" :
      | change | key | value | other     |
      | add    | JJJ | One   | Other JJJ |
      | add    | KKK | Two   | Other KKK |
    And a new commit ":construction: Update 1" has been saved with all the new identified diff content
    When the user request an export of the commit with name ":construction: Update 1"
    Then the export package contains 1 commit contents
    And the export package content has these identified changes for commit with name ":construction: Update 2" :
      | Table    | Key | Action | Payload                                |
      | TTAB_ONE | ZZZ | ADD    | PRESET:'Preset 76', SOMETHING:'ZZZX'   |
      | TTAB_ONE | CCC | REMOVE |                                        |
      | TTAB_ONE | DDD | UPDATE | PRESET:'Preset 4'=>'Preset 4 updated'  |
      | TTAB_TWO | JJJ | UPDATE | OTHER:'Other JJJ'=>'Other JJJ updated' |
      | TTAB_TWO | IJK | ADD    | VALUE:'Le new', OTHER:'newnew'         |

  Scenario: All blob content from a commit is exported
    Given the existing data in managed table "TTAB_FIVE" :
      | key | data                | simple |
      | 1   | ABCDEF1234567ABDDDD | 17.81  |
      | 2   | ABCDEF1234567ABEEEE | 17.82  |
      | 3   | ABCDEF1234567ABFFFF | 17.83  |
    And the commit ":tada: Test commit init" has been saved with all the identified initial diff content
    When the user request an export of the commit with name ":tada: Test commit init"
    Then the export package contains 1 commit contents
    And the export package content has these identified changes for commit with name ":tada: Test commit init" :
      | Table      | Key | Action | Payload                                                                                                        |
      | TTAB_ONE   | AAA | ADD    | PRESET:'Preset 1', SOMETHING:'AAA'                                                                             |
      | TTAB_ONE   | BBB | ADD    | PRESET:'Preset 2', SOMETHING:'BBB'                                                                             |
      | TTAB_ONE   | CCC | ADD    | PRESET:'Preset 3', SOMETHING:'CCC'                                                                             |
      | TTAB_ONE   | DDD | ADD    | PRESET:'Preset 4', SOMETHING:'DDD'                                                                             |
      | TTAB_ONE   | EEE | ADD    | PRESET:'Preset 5', SOMETHING:'EEE'                                                                             |
      | TTAB_TWO   | JJJ | ADD    | VALUE:'One', OTHER:'Other JJJ'                                                                                 |
      | TTAB_TWO   | KKK | ADD    | VALUE:'Two', OTHER:'Other KKK'                                                                                 |
      | TTAB_THREE | A   | ADD    | OTHER:'Other A'                                                                                                |
      | TTAB_THREE | B   | ADD    | OTHER:'Other B'                                                                                                |
      | TTAB_THREE | C   | ADD    | OTHER:'Other C'                                                                                                |
      | TTAB_FIVE  | 1   | ADD    | DATA:<a href="/lob/ZVs3L0PiRT7vzgYlGCrAmrqVm643dW1ZwshZNTNmEBc%3D" download="download">LOB</a>, SIMPLE:17.81   |
      | TTAB_FIVE  | 2   | ADD    | DATA:<a href="/lob/MDOnerGg0ikFARKvihX0fFD8V2mUp4%2BKHfrji2ByPKE%3D" download="download">LOB</a>, SIMPLE:17.82 |
      | TTAB_FIVE  | 3   | ADD    | DATA:<a href="/lob/mGb4npkQbRvRJrJWp%2FQIpwGPqZTFkKhI1FU9l9jNj1M%3D" download="download">LOB</a>, SIMPLE:17.83 |
    And the export package content has these associated lob data for commit with name ":tada: Test commit init" :
      | data                | hash                                         |
      | ABCDEF1234567ABDDDD | ZVs3L0PiRT7vzgYlGCrAmrqVm643dW1ZwshZNTNmEBc= |
      | ABCDEF1234567ABEEEE | MDOnerGg0ikFARKvihX0fFD8V2mUp4+KHfrji2ByPKE= |
      | ABCDEF1234567ABFFFF | mGb4npkQbRvRJrJWp/QIpwGPqZTFkKhI1FU9l9jNj1M= |

  Scenario: All clob content from a commit is exported
    Given the existing data in managed table "TTAB_SIX" :
      | identifier | text                                     | date       |
      | 7          | Ceci est un text enregistré dans un CLOB | 2012-01-15 |
      | 8          | Un autre text CLOB                       | 2005-07-08 |
      | 9          | Encore un autre                          | 2021-12-25 |
    And the commit ":tada: Test commit init clob" has been saved with all the identified initial diff content
    When the user request an export of the commit with name ":tada: Test commit init clob"
    Then the export package contains 1 commit contents
    And the export package content has these identified changes for commit with name ":tada: Test commit init clob" :
      | Table      | Key | Action | Payload                                                                                                                         |
      | TTAB_ONE   | AAA | ADD    | PRESET:'Preset 1', SOMETHING:'AAA'                                                                                              |
      | TTAB_ONE   | BBB | ADD    | PRESET:'Preset 2', SOMETHING:'BBB'                                                                                              |
      | TTAB_ONE   | CCC | ADD    | PRESET:'Preset 3', SOMETHING:'CCC'                                                                                              |
      | TTAB_ONE   | DDD | ADD    | PRESET:'Preset 4', SOMETHING:'DDD'                                                                                              |
      | TTAB_ONE   | EEE | ADD    | PRESET:'Preset 5', SOMETHING:'EEE'                                                                                              |
      | TTAB_TWO   | JJJ | ADD    | VALUE:'One', OTHER:'Other JJJ'                                                                                                  |
      | TTAB_TWO   | KKK | ADD    | VALUE:'Two', OTHER:'Other KKK'                                                                                                  |
      | TTAB_THREE | A   | ADD    | OTHER:'Other A'                                                                                                                 |
      | TTAB_THREE | B   | ADD    | OTHER:'Other B'                                                                                                                 |
      | TTAB_THREE | C   | ADD    | OTHER:'Other C'                                                                                                                 |
      | TTAB_SIX   | 7   | ADD    | TEXT:<a href="/lob/AyoJCmZNQXwkzt2ZcHgG8cpvUucsbGnanTuQu%2BpaGOs%3D" download="download">TEXT</a>, DATE:2012-01-15 00:00:00     |
      | TTAB_SIX   | 8   | ADD    | TEXT:<a href="/lob/KOpk7DP9iLnAls5%2FRoF1%2BKRDxWMDaA%2BeSk2bUGo8g3g%3D" download="download">TEXT</a>, DATE:2005-07-08 00:00:00 |
      | TTAB_SIX   | 9   | ADD    | TEXT:<a href="/lob/%2BfuDApVm2qHu8BaSOOkKAtICrThc5VM9ESzFM%2FC%2FVGI%3D" download="download">TEXT</a>, DATE:2021-12-25 00:00:00 |
    And the export package content has these associated lob data for commit with name ":tada: Test commit init clob" :
      | data                                     | hash                                         |
      | Ceci est un text enregistré dans un CLOB | AyoJCmZNQXwkzt2ZcHgG8cpvUucsbGnanTuQu+paGOs= |
      | Un autre text CLOB                       | KOpk7DP9iLnAls5/RoF1+KRDxWMDaA+eSk2bUGo8g3g= |
      | Encore un autre                          | +fuDApVm2qHu8BaSOOkKAtICrThc5VM9ESzFM/C/VGI= |



 ## Scenario: All attachment documents from a commit are exported