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

  Scenario: The export is prepared in 2 steps - details on export all
    Given the commit ":tada: Test commit init" has been saved with all the identified initial diff content
    And these changes are applied to table "TTAB_ONE" :
      | change | key | value | preset   | something |
      | add    | 14  | AAA   | Preset 1 | AAA       |
      | add    | 25  | BBB   | Preset 2 | BBB       |
      | delete | 37  | CCC   | Preset 3 | CCC       |
      | update | 38  | DDD   | Preset 4 | DDD       |
    And a new commit ":construction: Update 1" has been saved with all the new identified diff content
    And these changes are applied to table "TTAB_TWO" :
      | change | key | value | other     |
      | add    | JJJ | One   | Other JJJ |
      | add    | KKK | Two   | Other KKK |
    And a new commit ":construction: Update 2" has been saved with all the new identified diff content
    When the user request to prepare an export of all commits
    Then the provided template is preparation of an export
    And the preparing export is displayed as type "RANGE_FROM" for commit "ALL"
    And no transformers are listed for customization

  Scenario: The export is prepared in 2 steps - details on export range
    Given the commit ":tada: Test commit init" has been saved with all the identified initial diff content
    And these changes are applied to table "TTAB_ONE" :
      | change | key | value | preset   | something |
      | add    | 44  | AAA   | Preset 1 | AAA       |
      | add    | 45  | BBB   | Preset 2 | BBB       |
      | delete | 37  | CCC   | Preset 3 | CCC       |
      | update | 38  | DDD   | Modif 4  | DDD       |
    And a new commit ":construction: Update 1" has been saved with all the new identified diff content
    And these changes are applied to table "TTAB_TWO" :
      | change | key | value | other     |
      | add    | LLL | One   | Other JJJ |
      | add    | MMM | Two   | Other KKK |
    And a new commit ":construction: Update 2" has been saved with all the new identified diff content
    When the user request to prepare an export starting by the commit with name ":construction: Update 1"
    Then the provided template is preparation of an export
    And the preparing export is displayed as type "RANGE_FROM" for commit ":construction: Update 1"
    And no transformers are listed for customization

  Scenario: The export is prepared in 2 steps - details on export single
    Given the commit ":tada: Test commit init" has been saved with all the identified initial diff content
    And these changes are applied to table "TTAB_ONE" :
      | change | key | value | preset   | something |
      | add    | 44  | AAA   | Preset 1 | AAA       |
      | add    | 45  | BBB   | Preset 2 | BBB       |
      | delete | 37  | CCC   | Preset 3 | CCC       |
      | update | 38  | DDD   | Modif 4  | DDD       |
    And a new commit ":construction: Update 1" has been saved with all the new identified diff content
    And these changes are applied to table "TTAB_TWO" :
      | change | key | value | other     |
      | add    | JJJ | One   | Other JJJ |
      | add    | KKK | Two   | Other KKK |
    And a new commit ":construction: Update 2" has been saved with all the new identified diff content
    When the user request to prepare an export of the commit with name ":construction: Update 1"
    Then the provided template is preparation of an export
    And the preparing export is displayed as type "SINGLE_ONE" for commit ":construction: Update 1"
    And no transformers are listed for customization

  Scenario: The export is prepared in 2 steps - export content is downloaded after preparation
    Given the commit ":tada: Test commit init" has been saved with all the identified initial diff content
    And these changes are applied to table "TTAB_ONE" :
      | change | key | value | preset           | something |
      | add    | 76  | ZZZ   | Preset 76        | ZZZX      |
      | add    | 55  | HHH   | Preset 2         | BBB       |
      | delete | 37  | CCC   | Preset 3         | CCC       |
      | update | 38  | DDD   | Preset 4 updated | DDD2      |
    And a new commit ":construction: Update 1" has been saved with all the new identified diff content
    And these changes are applied to table "TTAB_TWO" :
      | change | key | value | other     |
      | add    | JJJ | One   | Other JJJ |
      | add    | KKK | Two   | Other KKK |
    And a new commit ":construction: Update 2" has been saved with all the new identified diff content
    When the user request to prepare an export of the commit with name ":construction: Update 1"
    And the user validate the prepared export
    Then the export download start automatically
    Then the export package contains 1 commit contents
    And the export package content has these identified changes for commit with name ":construction: Update 1" :
      | Table    | Key | Action | Payload                                                        |
      | TTAB_ONE | ZZZ | ADD    | PRESET:'Preset 76', SOMETHING:'ZZZX'                           |
      | TTAB_ONE | HHH | ADD    | PRESET:'Preset 2', SOMETHING:'BBB'                             |
      | TTAB_ONE | CCC | REMOVE |                                                                |
      | TTAB_ONE | DDD | UPDATE | PRESET:'Preset 4'=>'Preset 4 updated', SOMETHING:'DDD'=>'DDD2' |
    And the provided template is selection of commits to export

  Scenario: All the existing commits from the backlog can be exported in a single archive
    Given the commit ":tada: Test commit init" has been saved with all the identified initial diff content
    And these changes are applied to table "TTAB_ONE" :
      | change | key | value | preset           | something |
      | add    | 76  | ZZZ   | Preset 76        | ZZZX      |
      | add    | 55  | HHH   | Preset 2         | BBB       |
      | delete | 37  | CCC   | Preset 3         | CCC       |
      | update | 38  | DDD   | Preset 4 updated | DDD       |
    And a new commit ":construction: Update 1" has been saved with all the new identified diff content
    And these changes are applied to table "TTAB_TWO" :
      | change | key  | value | other     |
      | add    | JJJ2 | One   | Other JJJ |
      | add    | KKK2 | Two   | Other KKK |
    And a new commit ":construction: Update 2" has been saved with all the new identified diff content
    When the user request an export of all the commits
    Then the export package contains 3 commit contents

  Scenario: A saved commit can be exported alone
    Given the commit ":tada: Test commit init" has been saved with all the identified initial diff content
    And these changes are applied to table "TTAB_ONE" :
      | change | key | value | preset           | something |
      | add    | 76  | ZZZ   | Preset 76        | ZZZX      |
      | add    | 55  | HHH   | Preset 2         | BBB       |
      | delete | 37  | CCC   | Preset 3         | CCC       |
      | update | 38  | DDD   | Preset 4 updated | DDD       |
    And these changes are applied to table "TTAB_TWO" :
      | change | key | value | other     |
      | add    | LLL | One   | Other JJJ |
      | add    | MMM | Two   | Other KKK |
    And a new commit ":construction: Update 1" has been saved with all the new identified diff content
    When the user request an export of the commit with name ":construction: Update 1"
    Then the export package contains 1 commit contents
    And the export package content has these identified changes for commit with name ":construction: Update 1" :
      | Table    | Key | Action | Payload                               |
      | TTAB_ONE | ZZZ | ADD    | PRESET:'Preset 76', SOMETHING:'ZZZX'  |
      | TTAB_ONE | HHH | ADD    | PRESET:'Preset 2', SOMETHING:'BBB'    |
      | TTAB_ONE | CCC | REMOVE |                                       |
      | TTAB_ONE | DDD | UPDATE | PRESET:'Preset 4'=>'Preset 4 updated' |
      | TTAB_TWO | LLL | ADD    | VALUE:'One', OTHER:'Other JJJ'        |
      | TTAB_TWO | MMM | ADD    | VALUE:'Two', OTHER:'Other KKK'        |

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
      | Table      | Key | Action | Payload                                                                                                                       |
      | TTAB_ONE   | AAA | ADD    | PRESET:'Preset 1', SOMETHING:'AAA'                                                                                            |
      | TTAB_ONE   | BBB | ADD    | PRESET:'Preset 2', SOMETHING:'BBB'                                                                                            |
      | TTAB_ONE   | CCC | ADD    | PRESET:'Preset 3', SOMETHING:'CCC'                                                                                            |
      | TTAB_ONE   | DDD | ADD    | PRESET:'Preset 4', SOMETHING:'DDD'                                                                                            |
      | TTAB_ONE   | EEE | ADD    | PRESET:'Preset 5', SOMETHING:'EEE'                                                                                            |
      | TTAB_TWO   | JJJ | ADD    | VALUE:'One', OTHER:'Other JJJ'                                                                                                |
      | TTAB_TWO   | KKK | ADD    | VALUE:'Two', OTHER:'Other KKK'                                                                                                |
      | TTAB_THREE | A   | ADD    | OTHER:'Other A'                                                                                                               |
      | TTAB_THREE | B   | ADD    | OTHER:'Other B'                                                                                                               |
      | TTAB_THREE | C   | ADD    | OTHER:'Other C'                                                                                                               |
      | TTAB_FIVE  | 1   | ADD    | DATA:<a href="/ui/lob/WlZzM0wwUGlSVDd2emdZbEdDckFtcnFWbTY0M2RXMVp3c2haTlRObUVCYz0=" download="download">LOB</a>, SIMPLE:17.81 |
      | TTAB_FIVE  | 2   | ADD    | DATA:<a href="/ui/lob/TURPbmVyR2cwaWtGQVJLdmloWDBmRkQ4VjJtVXA0K0tIZnJqaTJCeVBLRT0=" download="download">LOB</a>, SIMPLE:17.82 |
      | TTAB_FIVE  | 3   | ADD    | DATA:<a href="/ui/lob/bUdiNG5wa1FiUnZSSnJKV3AvUUlwd0dQcVpURmtLaEkxRlU5bDlqTmoxTT0=" download="download">LOB</a>, SIMPLE:17.83 |
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
      | Table      | Key | Action | Payload                                                                                                                                    |
      | TTAB_ONE   | AAA | ADD    | PRESET:'Preset 1', SOMETHING:'AAA'                                                                                                         |
      | TTAB_ONE   | BBB | ADD    | PRESET:'Preset 2', SOMETHING:'BBB'                                                                                                         |
      | TTAB_ONE   | CCC | ADD    | PRESET:'Preset 3', SOMETHING:'CCC'                                                                                                         |
      | TTAB_ONE   | DDD | ADD    | PRESET:'Preset 4', SOMETHING:'DDD'                                                                                                         |
      | TTAB_ONE   | EEE | ADD    | PRESET:'Preset 5', SOMETHING:'EEE'                                                                                                         |
      | TTAB_TWO   | JJJ | ADD    | VALUE:'One', OTHER:'Other JJJ'                                                                                                             |
      | TTAB_TWO   | KKK | ADD    | VALUE:'Two', OTHER:'Other KKK'                                                                                                             |
      | TTAB_THREE | A   | ADD    | OTHER:'Other A'                                                                                                                            |
      | TTAB_THREE | B   | ADD    | OTHER:'Other B'                                                                                                                            |
      | TTAB_THREE | C   | ADD    | OTHER:'Other C'                                                                                                                            |
      | TTAB_SIX   | 7   | ADD    | TEXT:<a href="/ui/lob/QXlvSkNtWk5RWHdrenQyWmNIZ0c4Y3B2VXVjc2JHbmFuVHVRdStwYUdPcz0=" download="download">TEXT</a>, DATE:2012-01-15 00:00:00 |
      | TTAB_SIX   | 8   | ADD    | TEXT:<a href="/ui/lob/S09wazdEUDlpTG5BbHM1L1JvRjErS1JEeFdNRGFBK2VTazJiVUdvOGczZz0=" download="download">TEXT</a>, DATE:2005-07-08 00:00:00 |
      | TTAB_SIX   | 9   | ADD    | TEXT:<a href="/ui/lob/K2Z1REFwVm0ycUh1OEJhU09Pa0tBdElDclRoYzVWTTlFU3pGTS9DL1ZHST0=" download="download">TEXT</a>, DATE:2021-12-25 00:00:00 |
    And the export package content has these associated lob data for commit with name ":tada: Test commit init clob" :
      | data                                     | hash                                         |
      | Ceci est un text enregistré dans un CLOB | AyoJCmZNQXwkzt2ZcHgG8cpvUucsbGnanTuQu+paGOs= |
      | Un autre text CLOB                       | KOpk7DP9iLnAls5/RoF1+KRDxWMDaA+eSk2bUGo8g3g= |
      | Encore un autre                          | +fuDApVm2qHu8BaSOOkKAtICrThc5VM9ESzFM/C/VGI= |


 ## Scenario: All attachment documents from a commit are exported