Feature: The update on parameter tables can be followed checked and stored as commits

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
      | Table    | Key   | Action | Payload                                |
      | TTAB_ONE | One   | ADD    | PRESET:'Preset One', SOMETHING:'AAA'   |
      | TTAB_ONE | Two   | ADD    | PRESET:'Preset Two', SOMETHING:'BBB'   |
      | TTAB_ONE | Three | ADD    | PRESET:'Preset Three', SOMETHING:'CCC' |

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
      | Table     | Key | Action | Payload                                                                                                                       |
      | TTAB_FIVE | 1   | ADD    | DATA:<a href="/ui/lob/WlZzM0wwUGlSVDd2emdZbEdDckFtcnFWbTY0M2RXMVp3c2haTlRObUVCYz0=" download="download">LOB</a>, SIMPLE:17.81 |
      | TTAB_FIVE | 2   | ADD    | DATA:<a href="/ui/lob/TURPbmVyR2cwaWtGQVJLdmloWDBmRkQ4VjJtVXA0K0tIZnJqaTJCeVBLRT0=" download="download">LOB</a>, SIMPLE:17.82 |
      | TTAB_FIVE | 3   | ADD    | DATA:<a href="/ui/lob/bUdiNG5wa1FiUnZSSnJKV3AvUUlwd0dQcVpURmtLaEkxRlU5bDlqTmoxTT0=" download="download">LOB</a>, SIMPLE:17.83 |
    And the commit content has these associated lob data :
      | data                | hash                                         |
      | ABCDEF1234567ABDDDD | ZVs3L0PiRT7vzgYlGCrAmrqVm643dW1ZwshZNTNmEBc= |
      | ABCDEF1234567ABEEEE | MDOnerGg0ikFARKvihX0fFD8V2mUp4+KHfrji2ByPKE= |
      | ABCDEF1234567ABFFFF | mGb4npkQbRvRJrJWp/QIpwGPqZTFkKhI1FU9l9jNj1M= |

  Scenario: A first commit for a single table contains all table content - clob fields
    Given the existing data in managed table "TTAB_SIX" :
      | identifier | text                                     | date       |
      | 7          | Ceci est un text enregistré dans un CLOB | 2012-01-15 |
      | 8          | Un autre text CLOB                       | 2005-07-08 |
      | 9          | Encore un autre                          | 2021-12-25 |
    And a diff analysis can be started and completed
    And a diff has already been launched
    And the diff is completed
    When the user access to diff commit page
    Then the commit content is rendered with these identified changes :
      | Table    | Key | Action | Payload                                                                                                                                    |
      | TTAB_SIX | 7   | ADD    | TEXT:<a href="/ui/lob/QXlvSkNtWk5RWHdrenQyWmNIZ0c4Y3B2VXVjc2JHbmFuVHVRdStwYUdPcz0=" download="download">TEXT</a>, DATE:2012-01-15 00:00:00 |
      | TTAB_SIX | 8   | ADD    | TEXT:<a href="/ui/lob/S09wazdEUDlpTG5BbHM1L1JvRjErS1JEeFdNRGFBK2VTazJiVUdvOGczZz0=" download="download">TEXT</a>, DATE:2005-07-08 00:00:00 |
      | TTAB_SIX | 9   | ADD    | TEXT:<a href="/ui/lob/K2Z1REFwVm0ycUh1OEJhU09Pa0tBdElDclRoYzVWTTlFU3pGTS9DL1ZHST0=" download="download">TEXT</a>, DATE:2021-12-25 00:00:00 |
    And the commit content has these associated lob data :
      | data                                     | hash                                         |
      | Ceci est un text enregistré dans un CLOB | AyoJCmZNQXwkzt2ZcHgG8cpvUucsbGnanTuQu+paGOs= |
      | Un autre text CLOB                       | KOpk7DP9iLnAls5/RoF1+KRDxWMDaA+eSk2bUGo8g3g= |
      | Encore un autre                          | +fuDApVm2qHu8BaSOOkKAtICrThc5VM9ESzFM/C/VGI= |

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

  Scenario: After a commit, if no changes are applied to environment, no diff content will be provided - from initial commit
    Given the existing data in managed table "TTAB_ONE" :
      | key | value | preset   | something |
      | 14  | AAA   | Preset 1 | AAA       |
    And the commit ":construction: Test commit init" has been saved with all the identified initial diff content
    And no changes are applied in current environment
    And a diff has already been launched
    And the diff is completed
    When the user access to diff commit page
    Then no commit content has been identified

  Scenario: After a commit, if no changes are applied to environment, no diff content will be provided - from intermediate commit
    Given the existing data in managed table "TTAB_ONE" :
      | key | value | preset   | something |
      | 14  | AAA   | Preset 1 | AAA       |
    And the commit ":construction: Test commit init" has been saved with all the identified initial diff content
    And these changes are applied to table "TTAB_ONE" :
      | change | key | value  | preset   | something   |
      | add    | 33  | LL33   | Preset 2 | BBB         |
      | add    | 34  | VVV4   | Preset 3 | CCC         |
      | add    | 35  | VVV5   | Preset 4 | DDD         |
      | add    | 36  | LLVVV6 | Preset 5 | EEE         |
      | update | 14  | AAA    | Preset 1 | AAA updated |
    And a new commit ":construction: Commit changes" has been saved with all the new identified diff content
    And no changes are applied in current environment
    And a diff has already been launched
    And the diff is completed
    When the user access to diff commit page
    Then no commit content has been identified

  Scenario: The diff content for table using links on native FK displays references to other table data
    Given the existing data in managed table "TTAB_ONE" :
      | key | value | preset   | something |
      | 14  | AAA   | Preset 1 | AAA       |
      | 25  | BBB   | Preset 2 | BBB       |
      | 37  | CCC   | Preset 3 | CCC       |
      | 38  | DDD   | Preset 4 | DDD       |
      | 39  | EEE   | Preset 5 | EEE       |
    And the existing data in managed table "TTAB_FOUR" :
      | key  | otherTable | contentTime         | contentInt |
      | A001 | 14         | 2012-02-15 15:24:09 | 12         |
      | A002 | 25         | 2012-03-15 15:24:28 | 13         |
      | A003 | 25         | 2012-01-15 15:24:45 | 14         |
    And a diff analysis can be started and completed
    And a diff has already been launched
    And the diff is completed
    When the user access to diff commit page
    Then the commit content is rendered with these identified changes :
      | Table     | Key  | Action | Payload                                                                    |
      | TTAB_ONE  | AAA  | ADD    | PRESET:'Preset 1', SOMETHING:'AAA'                                         |
      | TTAB_ONE  | BBB  | ADD    | PRESET:'Preset 2', SOMETHING:'BBB'                                         |
      | TTAB_ONE  | CCC  | ADD    | PRESET:'Preset 3', SOMETHING:'CCC'                                         |
      | TTAB_ONE  | DDD  | ADD    | PRESET:'Preset 4', SOMETHING:'DDD'                                         |
      | TTAB_ONE  | EEE  | ADD    | PRESET:'Preset 5', SOMETHING:'EEE'                                         |
      | TTAB_FOUR | A001 | ADD    | LN_OTHER_TABLE_KEY:'AAA', CONTENT_TIME:2012-02-15 15:24:09, CONTENT_INT:12 |
      | TTAB_FOUR | A002 | ADD    | LN_OTHER_TABLE_KEY:'BBB', CONTENT_TIME:2012-03-15 15:24:28, CONTENT_INT:13 |
      | TTAB_FOUR | A003 | ADD    | LN_OTHER_TABLE_KEY:'BBB', CONTENT_TIME:2012-01-15 15:24:45, CONTENT_INT:14 |

  Scenario: The diff content for table using links on business keys displays references to other table data
    Given the existing data in managed table "TTAB_THREE" :
      | key    | value   | other   |
      | 00AA00 | THREE_A | Other A |
      | 00BB00 | THREE_B | Other B |
      | 00CC00 | THREE_C | Other C |
    And the existing data in managed table "TTAB_SEVEN" :
      | id | businessKey | otherTableValue | value  | enabled |
      | 1  | SEVEN_1     | THREE_A         | Truc   | true    |
      | 2  | SEVEN_2     | THREE_A         | Machin | true    |
      | 3  | SEVEN_3     | THREE_C         | Bidule | false   |
    And a diff analysis can be started and completed
    And a diff has already been launched
    And the diff is completed
    When the user access to diff commit page
    Then the commit content is rendered with these identified changes :
      | Table      | Key     | Action | Payload                                                       |
      | TTAB_THREE | THREE_A | ADD    | OTHER:'Other A'                                               |
      | TTAB_THREE | THREE_B | ADD    | OTHER:'Other B'                                               |
      | TTAB_THREE | THREE_C | ADD    | OTHER:'Other C'                                               |
      | TTAB_SEVEN | SEVEN_1 | ADD    | LN_OTHER_TABLE_VALUE:'THREE_A', VALUE:'Truc', ENABLED:true    |
      | TTAB_SEVEN | SEVEN_2 | ADD    | LN_OTHER_TABLE_VALUE:'THREE_A', VALUE:'Machin', ENABLED:true  |
      | TTAB_SEVEN | SEVEN_3 | ADD    | LN_OTHER_TABLE_VALUE:'THREE_C', VALUE:'Bidule', ENABLED:false |
    And there is no remarks on missing linked lines

  Scenario: If source table references lines which are missing in a linked table, then remarks are provided for each missing value
    Given the existing data in managed table "TTAB_THREE" :
      | key    | value   | other   |
      | 00AA00 | THREE_A | Other A |
      | 00BB00 | THREE_B | Other B |
      | 00CC00 | THREE_C | Other C |
    And the existing data in managed table "TTAB_SEVEN" :
      | id | businessKey | otherTableValue | value  | enabled |
      | 1  | SEVEN_1     | THREE_A         | Truc   | true    |
      | 2  | SEVEN_2     | THREE_MISSING_1 | Bidule | false   |
      | 3  | SEVEN_3     | THREE_B         | Machin | true    |
      | 4  | SEVEN_4     | THREE_C         | Bidule | false   |
      | 5  | SEVEN_5     | THREE_MISSING_2 | Bidule | false   |
    And the database doesn't support nullable join keys
    And a diff analysis can be started and completed
    And a diff has already been launched
    And the diff is completed
    When the user access to diff commit page
    Then these remarks on missing linked lines are rendered :
      | Table      | Key     | Payload                                                            |
      | TTAB_SEVEN | SEVEN_2 | VALUE:'Bidule', ENABLED:false, OTHER_TABLE_VALUE:'THREE_MISSING_1' |
      | TTAB_SEVEN | SEVEN_5 | VALUE:'Bidule', ENABLED:false, OTHER_TABLE_VALUE:'THREE_MISSING_2' |
    And the commit content is rendered with these identified changes :
      | Table      | Key     | Action | Payload                                                       |
      | TTAB_THREE | THREE_A | ADD    | OTHER:'Other A'                                               |
      | TTAB_THREE | THREE_B | ADD    | OTHER:'Other B'                                               |
      | TTAB_THREE | THREE_C | ADD    | OTHER:'Other C'                                               |
      | TTAB_SEVEN | SEVEN_1 | ADD    | LN_OTHER_TABLE_VALUE:'THREE_A', VALUE:'Truc', ENABLED:true    |
      | TTAB_SEVEN | SEVEN_3 | ADD    | LN_OTHER_TABLE_VALUE:'THREE_B', VALUE:'Machin', ENABLED:true  |
      | TTAB_SEVEN | SEVEN_4 | ADD    | LN_OTHER_TABLE_VALUE:'THREE_C', VALUE:'Bidule', ENABLED:false |

  Scenario: The diff content is paginated and filtered - full content
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
      | VVV | Two   | Other VVV |
    And the existing data in managed table "TTAB_THREE" :
      | key   | value | other   |
      | 11111 | A     | Other A |
      | 22222 | B     | Other B |
      | 33333 | C     | Other C |
    And a diff analysis can be started and completed
    And the commit ":tada: Test commit init" has been saved with all the identified initial diff content
    And these changes are applied to table "TTAB_ONE" :
      | change | key | value  | preset           | something   |
      | add    | 32  | LL32   | Preset 1         | AAA         |
      | add    | 33  | LL33   | Preset 2         | BBB         |
      | add    | 34  | VVV4   | Preset 3         | CCC         |
      | add    | 35  | VVV5   | Preset 4         | DDD         |
      | add    | 36  | LLVVV6 | Preset 5         | EEE         |
      | delete | 38  | DDD    |                  |             |
      | update | 39  | EEE    | Preset 5 updated | EEE updated |
    And these changes are applied to table "TTAB_TWO" :
      | change | key  | value | other      |
      | add    | JJJ2 | One   | Other JJJ2 |
      | add    | VVV2 | Two   | Other VVV2 |
      | add    | VVV3 | Three | Other 333  |
    And these changes are applied to table "TTAB_THREE" :
      | change | key   | value | other           |
      | delete | 33333 | C     |                 |
      | update | 22222 | B     | Other B updated |
      | add    | 44444 | VVVD  | Other VVVD      |
    And a diff has already been launched
    And the diff is completed
    When the user access to diff commit page
    Then the paginated commit content is rendered with these identified changes :
      | Table      | Key    | Action | Payload                                                               |
      | TTAB_ONE   | LL32   | ADD    | PRESET:'Preset 1', SOMETHING:'AAA'                                    |
      | TTAB_ONE   | LL33   | ADD    | PRESET:'Preset 2', SOMETHING:'BBB'                                    |
      | TTAB_ONE   | VVV4   | ADD    | PRESET:'Preset 3', SOMETHING:'CCC'                                    |
      | TTAB_ONE   | VVV5   | ADD    | PRESET:'Preset 4', SOMETHING:'DDD'                                    |
      | TTAB_ONE   | LLVVV6 | ADD    | PRESET:'Preset 5', SOMETHING:'EEE'                                    |
      | TTAB_ONE   | DDD    | REMOVE |                                                                       |
      | TTAB_ONE   | EEE    | UPDATE | PRESET:'Preset 5'=>'Preset 5 updated', SOMETHING:'EEE'=>'EEE updated' |
      | TTAB_TWO   | JJJ2   | ADD    | VALUE:'One', OTHER:'Other JJJ2'                                       |
      | TTAB_TWO   | VVV2   | ADD    | VALUE:'Two', OTHER:'Other VVV2'                                       |
      | TTAB_TWO   | VVV3   | ADD    | VALUE:'Three', OTHER:'Other 333'                                      |
      | TTAB_THREE | C      | REMOVE |                                                                       |
      | TTAB_THREE | B      | UPDATE | OTHER:'Other B'=>'Other B updated'                                    |
      | TTAB_THREE | VVVD   | ADD    | OTHER:'Other VVVD'                                                    |

  Scenario: The diff content is paginated and filtered - filtered by key
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
      | VVV | Two   | Other VVV |
    And the existing data in managed table "TTAB_THREE" :
      | key   | value | other   |
      | 11111 | A     | Other A |
      | 22222 | B     | Other B |
      | 33333 | C     | Other C |
    And a diff analysis can be started and completed
    And the commit ":tada: Test commit init" has been saved with all the identified initial diff content
    And these changes are applied to table "TTAB_ONE" :
      | change | key | value  | preset           | something   |
      | add    | 32  | LL32   | Preset 1         | AAA         |
      | add    | 33  | LL33   | Preset 2         | BBB         |
      | add    | 34  | VVV4   | Preset 3         | CCC         |
      | add    | 35  | VVV5   | Preset 4         | DDD         |
      | add    | 36  | LLVVV6 | Preset 5         | EEE         |
      | delete | 38  | DDD    |                  |             |
      | update | 39  | EEE    | Preset 5 updated | EEE updated |
    And these changes are applied to table "TTAB_TWO" :
      | change | key  | value | other      |
      | add    | JJJ2 | One   | Other JJJ2 |
      | add    | VVV2 | Two   | Other VVV2 |
      | add    | VVV3 | Three | Other 333  |
    And these changes are applied to table "TTAB_THREE" :
      | change | key   | value | other           |
      | delete | 33333 | C     |                 |
      | update | 22222 | B     | Other B updated |
      | add    | 44444 | VVVD  | Other VVVD      |
    And a diff has already been launched
    And the diff is completed
    When the user access to diff commit page
    And apply a content filter criteria "VVV\d" on "key"
    Then the paginated commit content is rendered with these identified changes :
      | Table    | Key  | Action | Payload                            |
      | TTAB_ONE | VVV4 | ADD    | PRESET:'Preset 3', SOMETHING:'CCC' |
      | TTAB_ONE | VVV5 | ADD    | PRESET:'Preset 4', SOMETHING:'DDD' |
      | TTAB_TWO | VVV2 | ADD    | VALUE:'Two', OTHER:'Other VVV2'    |
      | TTAB_TWO | VVV3 | ADD    | VALUE:'Three', OTHER:'Other 333'   |

  Scenario: The diff content is paginated and filtered - filtered by table + type and sorted by table and key
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
      | VVV | Two   | Other VVV |
    And the existing data in managed table "TTAB_THREE" :
      | key   | value | other   |
      | 11111 | A     | Other A |
      | 22222 | B     | Other B |
      | 33333 | C     | Other C |
    And a diff analysis can be started and completed
    And the commit ":tada: Test commit init" has been saved with all the identified initial diff content
    And these changes are applied to table "TTAB_ONE" :
      | change | key | value  | preset           | something   |
      | add    | 32  | LL32   | Preset 1         | AAA         |
      | add    | 33  | LL33   | Preset 2         | BBB         |
      | add    | 34  | VVV4   | Preset 3         | CCC         |
      | add    | 35  | VVV5   | Preset 4         | DDD         |
      | add    | 36  | LLVVV6 | Preset 5         | EEE         |
      | delete | 38  | DDD    |                  |             |
      | update | 39  | EEE    | Preset 5 updated | EEE updated |
    And these changes are applied to table "TTAB_TWO" :
      | change | key  | value | other      |
      | add    | JJJ2 | One   | Other JJJ2 |
      | add    | VVV2 | Two   | Other VVV2 |
      | add    | VVV3 | Three | Other 333  |
    And these changes are applied to table "TTAB_THREE" :
      | change | key   | value | other           |
      | delete | 33333 | C     |                 |
      | update | 22222 | B     | Other B updated |
      | add    | 44444 | VVVD  | Other VVVD      |
    And a diff has already been launched
    And the diff is completed
    When the user access to diff commit page
    And apply a content filter criteria "TTAB_[^W]*" on "table"
    And apply a content filter criteria "ADD" on "type"
    And apply a content sort criteria "DESC" on "table"
    And apply a content sort criteria "ASC" on "key"
    Then the paginated commit content is rendered with these identified sorted changes :
      | Table      | Key    | Action | Payload                            |
      | TTAB_THREE | VVVD   | ADD    | OTHER:'Other VVVD'                 |
      | TTAB_ONE   | LL32   | ADD    | PRESET:'Preset 1', SOMETHING:'AAA' |
      | TTAB_ONE   | LL33   | ADD    | PRESET:'Preset 2', SOMETHING:'BBB' |
      | TTAB_ONE   | LLVVV6 | ADD    | PRESET:'Preset 5', SOMETHING:'EEE' |
      | TTAB_ONE   | VVV4   | ADD    | PRESET:'Preset 3', SOMETHING:'CCC' |
      | TTAB_ONE   | VVV5   | ADD    | PRESET:'Preset 4', SOMETHING:'DDD' |

  Scenario: The diff content can be selected regarding the current filter - filtered by table
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
      | VVV | Two   | Other VVV |
    And the existing data in managed table "TTAB_THREE" :
      | key   | value | other   |
      | 11111 | A     | Other A |
      | 22222 | B     | Other B |
      | 33333 | C     | Other C |
    And a diff analysis can be started and completed
    And the commit ":tada: Test commit init" has been saved with all the identified initial diff content
    And these changes are applied to table "TTAB_ONE" :
      | change | key | value  | preset           | something   |
      | add    | 32  | LL32   | Preset 1         | AAA         |
      | add    | 33  | LL33   | Preset 2         | BBB         |
      | add    | 34  | VVV4   | Preset 3         | CCC         |
      | add    | 35  | VVV5   | Preset 4         | DDD         |
      | add    | 36  | LLVVV6 | Preset 5         | EEE         |
      | delete | 38  | DDD    |                  |             |
      | update | 39  | EEE    | Preset 5 updated | EEE updated |
    And these changes are applied to table "TTAB_TWO" :
      | change | key  | value | other      |
      | add    | JJJ2 | One   | Other JJJ2 |
      | add    | VVV2 | Two   | Other VVV2 |
      | add    | VVV3 | Three | Other 333  |
    And these changes are applied to table "TTAB_THREE" :
      | change | key   | value | other           |
      | delete | 33333 | C     |                 |
      | update | 22222 | B     | Other B updated |
      | add    | 44444 | VVVD  | Other VVVD      |
    And a diff has already been launched
    And the diff is completed
    When the user access to diff commit page
    And apply a content filter criteria "TTAB_[^W]*" on "table"
    And the user select the filtered diff content for commit
    Then the commit content is selected as this :
      | Table      | Key    | Action | Selection |
      | TTAB_ONE   | LL32   | ADD    | selected  |
      | TTAB_ONE   | LL33   | ADD    | selected  |
      | TTAB_ONE   | VVV4   | ADD    | selected  |
      | TTAB_ONE   | VVV5   | ADD    | selected  |
      | TTAB_ONE   | LLVVV6 | ADD    | selected  |
      | TTAB_ONE   | DDD    | REMOVE | selected  |
      | TTAB_ONE   | EEE    | UPDATE | selected  |
      | TTAB_TWO   | JJJ2   | ADD    | ignored   |
      | TTAB_TWO   | VVV2   | ADD    | ignored   |
      | TTAB_TWO   | VVV3   | ADD    | ignored   |
      | TTAB_THREE | C      | REMOVE | selected  |
      | TTAB_THREE | B      | UPDATE | selected  |
      | TTAB_THREE | VVVD   | ADD    | selected  |

  Scenario: Init diff for tables without values is processed without error
    Given the application is fully initialized with the wizard
    And a created parameter table with name "Table with 2 keys" for managed table "TTAB_ONLY_KEYS" and columns selected as this :
      | name      | selection |
      | ONE_KEY   | key       |
      | OTHER_KEY | key       |
    And a created parameter table with name "Table with 3 keys" for managed table "TTAB_THREE_KEYS" with filter "cur.SECOND_KEY is not null or cur.THIRD_KEY is not null" and columns selected as this :
      | name       | selection |
      | FIRST_KEY  | key       |
      | SECOND_KEY | key       |
      | THIRD_KEY  | key       |
    And the parameter table for managed table "TTAB_ONE" already exists
    And the parameter table for managed table "TTAB_TWO" already exists
    And the existing data in managed table "TTAB_ONE" :
      | key | value | preset   | something |
      | 14  | AAA   | Preset 1 | AAA       |
      | 25  | BBB   | Preset 2 | BBB       |
    And the existing data in managed table "TTAB_TWO" :
      | key | value | other     |
      | JJJ | One   | Other JJJ |
      | VVV | Two   | Other VVV |
    And the existing data in managed table "TTAB_ONLY_KEYS" :
      | oneKey | otherKey |
      | 1      | 1        |
      | 2      | 1        |
      | 3      | 1        |
      | 4      | 2        |
    And the existing data in managed table "TTAB_THREE_KEYS" :
      | firstKey | secondKey | thirdKey  |
      | one      |           | something |
      | two      | a         | a         |
      | three    |           |           |
      | 4        | aaa       |           |
      | 5        | 5         | 5         |
    And the user add new version "v2"
    And a diff has already been launched
    And the diff is completed
    When the user access to diff commit page
    Then the commit content is rendered with these identified changes :
      | Table           | Key                    | Action | Payload                            |
      | TTAB_ONE        | AAA                    | ADD    | PRESET:'Preset 1', SOMETHING:'AAA' |
      | TTAB_ONE        | BBB                    | ADD    | PRESET:'Preset 2', SOMETHING:'BBB' |
      | TTAB_TWO        | JJJ                    | ADD    | VALUE:'One', OTHER:'Other JJJ'     |
      | TTAB_TWO        | VVV                    | ADD    | VALUE:'Two', OTHER:'Other VVV'     |
      | TTAB_ONLY_KEYS  | 1 / 1                  | ADD    |                                    |
      | TTAB_ONLY_KEYS  | 2 / 1                  | ADD    |                                    |
      | TTAB_ONLY_KEYS  | 3 / 1                  | ADD    |                                    |
      | TTAB_ONLY_KEYS  | 4 / 2                  | ADD    |                                    |
      | TTAB_THREE_KEYS | 4 / aaa / null         | ADD    |                                    |
      | TTAB_THREE_KEYS | 5 / 5 / 5              | ADD    |                                    |
      | TTAB_THREE_KEYS | one / null / something | ADD    |                                    |
      | TTAB_THREE_KEYS | two / a / a            | ADD    |                                    |

  Scenario: Update diff for tables without values is processed without error
    Given the application is fully initialized with the wizard
    And a created parameter table with name "Table with 2 keys" for managed table "TTAB_ONLY_KEYS" and columns selected as this :
      | name      | selection |
      | ONE_KEY   | key       |
      | OTHER_KEY | key       |
    And a created parameter table with name "Table with 3 keys" for managed table "TTAB_THREE_KEYS" with filter "cur.SECOND_KEY is not null or cur.THIRD_KEY is not null" and columns selected as this :
      | name       | selection |
      | FIRST_KEY  | key       |
      | SECOND_KEY | key       |
      | THIRD_KEY  | key       |
    And the parameter table for managed table "TTAB_ONE" already exists
    And the parameter table for managed table "TTAB_TWO" already exists
    And the existing data in managed table "TTAB_ONE" :
      | key | value | preset   | something |
      | 14  | AAA   | Preset 1 | AAA       |
      | 25  | BBB   | Preset 2 | BBB       |
    And the existing data in managed table "TTAB_TWO" :
      | key | value | other     |
      | JJJ | One   | Other JJJ |
      | VVV | Two   | Other VVV |
    And the existing data in managed table "TTAB_ONLY_KEYS" :
      | oneKey | otherKey |
      | 1      | 1        |
      | 2      | 1        |
      | 3      | 1        |
      | 4      | 2        |
    And the existing data in managed table "TTAB_THREE_KEYS" :
      | firstKey | secondKey | thirdKey  |
      | one      |           | something |
      | two      | a         | a         |
      | three    |           |           |
      | 4        | aaa       |           |
      | 5        | 5         | 5         |
    And the user add new version "v2"
    And a new commit "init 1" has been saved with all the new identified diff content
    And these changes are applied to table "TTAB_ONE" :
      | change | key | value | preset   | something |
      | add    | 27  | LL32  | Preset 1 | AAA       |
      | update | 25  | BBB   | Preset 2 | updated   |
    And these changes are applied to table "TTAB_ONLY_KEYS" :
      | change | oneKey | otherKey |
      | add    | 5      | 1        |
      | add    | 6      | 3        |
      | update | 3      | 3        |
      | delete | 4      | 2        |
    And these changes are applied to table "TTAB_THREE_KEYS" :
      | change | firstKey | secondKey | thirdKey |
      | add    | 6        | 6         | 6        |
      | add    | 7        |           |          |
      | add    | other    | other     |          |
      | add    | tttt     |           | tttttttt |
      | update | 5        | 5         | 6        |
      | update | three    | three     | three    |
      | delete | 4        | aaa       |          |
    And a diff has already been launched
    And the diff is completed
    When the user access to diff commit page
    Then the commit content is rendered with these identified changes :
      | Table           | Key                    | Action | Payload                            |
      | TTAB_ONE        | LL32                   | ADD    | PRESET:'Preset 1', SOMETHING:'AAA' |
      | TTAB_ONE        | BBB                    | UPDATE | SOMETHING:'BBB'=>'updated'         |
      | TTAB_ONLY_KEYS  | 5 / 1                  | ADD    |                                    |
      | TTAB_ONLY_KEYS  | 6 / 3                  | ADD    |                                    |
      | TTAB_ONLY_KEYS  | 3 / 1                  | REMOVE |                                    |
      | TTAB_ONLY_KEYS  | 3 / 3                  | ADD    |                                    |
      | TTAB_ONLY_KEYS  | 4 / 2                  | REMOVE |                                    |
      | TTAB_THREE_KEYS | 6 / 6 / 6              | ADD    |                                    |
      | TTAB_THREE_KEYS | tttt / null / tttttttt | ADD    |                                    |
      | TTAB_THREE_KEYS | 5 / 5 / 5              | REMOVE |                                    |
      | TTAB_THREE_KEYS | 5 / 5 / 6              | ADD    |                                    |
      | TTAB_THREE_KEYS | other / other / null   | ADD    |                                    |
      | TTAB_THREE_KEYS | three / three / three  | ADD    |                                    |
      | TTAB_THREE_KEYS | 4 / aaa / null         | REMOVE |                                    |

  Scenario: Update diff for newly managed tables without values is processed without error
    Given the application is fully initialized with the wizard
    And a created parameter table with name "Table with 2 keys" for managed table "TTAB_ONLY_KEYS" and columns selected as this :
      | name      | selection |
      | ONE_KEY   | key       |
      | OTHER_KEY | key       |
    And the parameter table for managed table "TTAB_ONE" already exists
    And the parameter table for managed table "TTAB_TWO" already exists
    And the existing data in managed table "TTAB_ONE" :
      | key | value | preset   | something |
      | 14  | AAA   | Preset 1 | AAA       |
      | 25  | BBB   | Preset 2 | BBB       |
    And the existing data in managed table "TTAB_TWO" :
      | key | value | other     |
      | JJJ | One   | Other JJJ |
      | VVV | Two   | Other VVV |
    And the existing data in managed table "TTAB_ONLY_KEYS" :
      | oneKey | otherKey |
      | 1      | 1        |
      | 2      | 1        |
      | 3      | 1        |
      | 4      | 2        |
    And the user add new version "v2"
    And a new commit "init 1" has been saved with all the new identified diff content
    And a created parameter table with name "Table with 3 keys" for managed table "TTAB_THREE_KEYS" with filter "cur.SECOND_KEY is not null or cur.THIRD_KEY is not null" and columns selected as this :
      | name       | selection |
      | FIRST_KEY  | key       |
      | SECOND_KEY | key       |
      | THIRD_KEY  | key       |
    And the existing data in managed table "TTAB_THREE_KEYS" :
      | firstKey | secondKey | thirdKey  |
      | one      |           | something |
      | two      | a         | a         |
      | three    |           |           |
      | 4        | aaa       |           |
      | 5        | 5         | 5         |
    And these changes are applied to table "TTAB_ONE" :
      | change | key | value | preset   | something |
      | add    | 27  | LL32  | Preset 1 | AAA       |
      | update | 25  | BBB   | Preset 2 | updated   |
    And these changes are applied to table "TTAB_ONLY_KEYS" :
      | change | oneKey | otherKey |
      | add    | 5      | 1        |
      | add    | 6      | 3        |
      | update | 3      | 3        |
      | delete | 4      | 2        |
    And the user add new version "v3"
    And a diff has already been launched
    And the diff is completed
    When the user access to diff commit page
    Then the commit content is rendered with these identified changes :
      | Table           | Key                    | Action | Payload                            |
      | TTAB_ONE        | LL32                   | ADD    | PRESET:'Preset 1', SOMETHING:'AAA' |
      | TTAB_ONE        | BBB                    | UPDATE | SOMETHING:'BBB'=>'updated'         |
      | TTAB_ONLY_KEYS  | 5 / 1                  | ADD    |                                    |
      | TTAB_ONLY_KEYS  | 6 / 3                  | ADD    |                                    |
      | TTAB_ONLY_KEYS  | 3 / 1                  | REMOVE |                                    |
      | TTAB_ONLY_KEYS  | 3 / 3                  | ADD    |                                    |
      | TTAB_ONLY_KEYS  | 4 / 2                  | REMOVE |                                    |
      | TTAB_THREE_KEYS | 4 / aaa / null         | ADD    |                                    |
      | TTAB_THREE_KEYS | 5 / 5 / 5              | ADD    |                                    |
      | TTAB_THREE_KEYS | one / null / something | ADD    |                                    |
      | TTAB_THREE_KEYS | two / a / a            | ADD    |                                    |
