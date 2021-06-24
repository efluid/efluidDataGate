Feature: The commit can be saved and are historised

  After a diff, content of commit can be saved in application managed index. For each standard diff content line, user can say "keep, revert or ignore"

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
      | VVV | Two   | Other VVV |
    And the existing data in managed table "TTAB_THREE" :
      | key   | value | other   |
      | 11111 | A     | Other A |
      | 22222 | B     | Other B |
      | 33333 | C     | Other C |

  Scenario: From the commit diff the user can access to saving page for commit completion.
    Given a diff analysis has been started and completed
    When the user accesses to preparation commit page
    Then the provided template is commit saving
    And the commit comment is empty

  Scenario: The diff content is not selected by default for commit preparation
    Given a diff analysis has been started and completed
    When the user do not select any prepared diff content for commit
    And the user accesses to preparation commit page
    Then all the diff preparation content is ignored by default

  Scenario: The diff content can be fully selected for commit preparation
    Given a diff analysis has been started and completed
    When the user select all prepared diff content for commit
    And the user accesses to preparation commit page
    Then all the diff preparation content is selected for commit

  Scenario: The default preparing commit type is LOCAL
    Given a diff analysis has been started and completed
    When the user select all prepared diff content for commit
    And the user accesses to preparation commit page
    Then the commit type is "LOCAL"

  Scenario: The selected diff content can be saved as a new commit
    Given a diff analysis has been started and completed
    And the user has selected all content for commit
    And the user has specified a commit comment ":construction: Test commit"
    When the user save the commit
    Then the commit ":construction: Test commit" is added to commit list for current project
    And the saved commit content has these identified changes :
      | Table      | Key | Action | Payload                            |
      | TTAB_ONE   | AAA | ADD    | PRESET:'Preset 1', SOMETHING:'AAA' |
      | TTAB_ONE   | BBB | ADD    | PRESET:'Preset 2', SOMETHING:'BBB' |
      | TTAB_ONE   | CCC | ADD    | PRESET:'Preset 3', SOMETHING:'CCC' |
      | TTAB_ONE   | DDD | ADD    | PRESET:'Preset 4', SOMETHING:'DDD' |
      | TTAB_ONE   | EEE | ADD    | PRESET:'Preset 5', SOMETHING:'EEE' |
      | TTAB_TWO   | JJJ | ADD    | VALUE:'One', OTHER:'Other JJJ'     |
      | TTAB_TWO   | VVV | ADD    | VALUE:'Two', OTHER:'Other VVV'     |
      | TTAB_THREE | A   | ADD    | OTHER:'Other A'                    |
      | TTAB_THREE | B   | ADD    | OTHER:'Other B'                    |
      | TTAB_THREE | C   | ADD    | OTHER:'Other C'                    |

  Scenario: The lob data is associated to a saved commit content - blob content
    Given the existing data in managed table "TTAB_FIVE" :
      | key | data                | simple |
      | 1   | ABCDEF1234567ABDDDD | 17.81  |
      | 2   | ABCDEF1234567ABEEEE | 17.82  |
      | 3   | ABCDEF1234567ABFFFF | 17.83  |
    And a diff analysis has been started and completed
    And the user has selected all content for commit
    And the user has specified a commit comment ":construction: Test commit"
    When the user save the commit
    And the saved commit content has these associated lob data :
      | data                | hash                                         |
      | ABCDEF1234567ABDDDD | ZVs3L0PiRT7vzgYlGCrAmrqVm643dW1ZwshZNTNmEBc= |
      | ABCDEF1234567ABEEEE | MDOnerGg0ikFARKvihX0fFD8V2mUp4+KHfrji2ByPKE= |
      | ABCDEF1234567ABFFFF | mGb4npkQbRvRJrJWp/QIpwGPqZTFkKhI1FU9l9jNj1M= |

  Scenario: The lob data is associated to a saved commit content - clob content
    Given the existing data in managed table "TTAB_SIX" :
      | identifier | text                                     | date       |
      | 7          | Ceci est un text enregistré dans un CLOB | 2012-01-15 |
      | 8          | Un autre text CLOB                       | 2005-07-08 |
      | 9          | Encore un autre                          | 2021-12-25 |
    And a diff analysis has been started and completed
    And the user has selected all content for commit
    And the user has specified a commit comment ":construction: Test commit"
    When the user save the commit
    And the saved commit content has these associated lob data :
      | data                                     | hash                                         |
      | Ceci est un text enregistré dans un CLOB | AyoJCmZNQXwkzt2ZcHgG8cpvUucsbGnanTuQu+paGOs= |
      | Un autre text CLOB                       | KOpk7DP9iLnAls5/RoF1+KRDxWMDaA+eSk2bUGo8g3g= |
      | Encore un autre                          | +fuDApVm2qHu8BaSOOkKAtICrThc5VM9ESzFM/C/VGI= |

  Scenario: A saved commit can be associated to attached documents. The attached documents are then managed in backlog database
    Given a diff analysis has been started and completed
    And the user has selected all content for commit
    And the user has specified a commit comment ":construction: Test commit with attachment"
    And the user has attached these documents to the commit:
      | title         | type      | size  |
      | attachment.md | MD_FILE   | 1500  |
      | script.sql    | SQL_FILE  | 7800  |
      | something.txt | TEXT_FILE | 45000 |
    When the user save the commit
    Then the commit ":construction: Test commit with attachment" is added to commit list for current project
    And these attachment documents are associated to the commit in the current project backlog:
      | title         | type      |
      | attachment.md | MD_FILE   |
      | script.sql    | SQL_FILE  |
      | something.txt | TEXT_FILE |

  Scenario: A list of saved commits is available
    Given the commit ":tada: Test commit init" has been saved with all the identified initial diff content
    And these changes are applied to table "TTAB_ONE" :
      | change | key | value | preset   | something |
      | add    | 32  | LL32  | Preset 1 | AAA       |
      | add    | 33  | LL33  | Preset 2 | BBB       |
      | add    | 34  | LL34  | Preset 3 | CCC       |
      | add    | 35  | LL35  | Preset 4 | DDD       |
      | add    | 36  | LL36  | Preset 5 | EEE       |
    And a new commit ":construction: Update 1" has been saved with all the new identified diff content
    And these changes are applied to table "TTAB_TWO" :
      | change | key  | value | other      |
      | add    | JJJ2 | One   | Other JJJ2 |
      | add    | VVV2 | Two   | Other VVV2 |
    And a new commit ":construction: Update 2" has been saved with all the new identified diff content
    When the user access to list of commits
    Then the provided template is list of commits
    And the list of commits is :
      | comment                 | author      |
      | :tada: Test commit init | any@test.fr |
      | :construction: Update 1 | any@test.fr |
      | :construction: Update 2 | any@test.fr |

  Scenario: A commit name can be modified
    Given the commit ":tada: Test commit init" has been saved with all the identified initial diff content
    And these changes are applied to table "TTAB_ONE" :
      | change | key | value | preset   | something |
      | add    | 32  | LL32  | Preset 1 | AAA       |
      | add    | 33  | LL33  | Preset 2 | BBB       |
      | add    | 34  | LL34  | Preset 3 | CCC       |
      | add    | 35  | LL35  | Preset 4 | DDD       |
    And a new commit ":construction: Update 1" has been saved with all the new identified diff content
    And these changes are applied to table "TTAB_TWO" :
      | change | key  | value | other      |
      | add    | JJJ2 | One   | Other JJJ2 |
      | add    | VVV2 | Two   | Other VVV2 |
    And a new commit ":construction: Update 2" has been saved with all the new identified diff content
    When the user access to list of commits
    And the user select the details of commit ":construction: Update 1"
    And the user update the commit details with name ":bug: This was something else"
    And the user return to list of commits
    Then the list of commits is :
      | comment                       | author      |
      | :tada: Test commit init       | any@test.fr |
      | :bug: This was something else | any@test.fr |
      | :construction: Update 2       | any@test.fr |

  Scenario: The details for an existing commit can be displayed
    Given the commit ":tada: Test commit init" has been saved with all the identified initial diff content
    And these changes are applied to table "TTAB_ONE" :
      | change | key | value | preset   | something |
      | add    | 32  | LL32  | Preset 1 | AAA       |
      | add    | 33  | LL33  | Preset 2 | BBB       |
      | add    | 34  | LL34  | Preset 3 | CCC       |
      | add    | 35  | LL35  | Preset 4 | DDD       |
      | add    | 36  | LL36  | Preset 5 | EEE       |
    And a new commit ":construction: Update 1" has been saved with all the new identified diff content
    And these changes are applied to table "TTAB_TWO" :
      | change | key  | value | other      |
      | add    | JJJ2 | One   | Other JJJ2 |
      | add    | VVV2 | Two   | Other VVV2 |
    And a new commit ":construction: Update 2" has been saved with all the new identified diff content
    When the user access to list of commits
    And the user select the details of commit ":construction: Update 2"
    Then the provided template is detail of an existing commit
    And the commit details are displayed with this content :
      | Table    | Key  | Action | Payload                         |
      | TTAB_TWO | JJJ2 | ADD    | VALUE:'One', OTHER:'Other JJJ2' |
      | TTAB_TWO | VVV2 | ADD    | VALUE:'Two', OTHER:'Other VVV2' |

  Scenario: The details for an existing commit can be displayed and filtered - default no filter
    Given the commit ":tada: Test commit init" has been saved with all the identified initial diff content
    And these changes are applied to table "TTAB_ONE" :
      | change | key | value | preset           | something   |
      | add    | 32  | LL32  | Preset 1         | AAA         |
      | add    | 33  | LL33  | Preset 2         | BBB         |
      | add    | 34  | LL34  | Preset 3         | CCC         |
      | add    | 35  | LL35  | Preset 4         | DDD         |
      | add    | 36  | LL36  | Preset 5         | EEE         |
      | delete | 38  | DDD   |                  |             |
      | update | 39  | EEE   | Preset 5 updated | EEE updated |
    And these changes are applied to table "TTAB_TWO" :
      | change | key  | value | other      |
      | add    | JJJ2 | One   | Other JJJ2 |
      | add    | VVV2 | Two   | Other VVV2 |
      | add    | VVV3 | Three | Other 333  |
    And these changes are applied to table "TTAB_THREE" :
      | change | key   | value | other           |
      | delete | 33333 | C     |                 |
      | update | 22222 | B     | Other B updated |
      | add    | 44444 | D     | Other D         |
    And a new commit ":construction: Update 1" has been saved with all the new identified diff content
    When the user access to list of commits
    And the user select the details of commit ":construction: Update 1"
    Then the commit details are displayed with this content :
      | Table      | Key  | Action | Payload                                                               |
      | TTAB_ONE   | LL32 | ADD    | PRESET:'Preset 1', SOMETHING:'AAA'                                    |
      | TTAB_ONE   | LL33 | ADD    | PRESET:'Preset 2', SOMETHING:'BBB'                                    |
      | TTAB_ONE   | LL34 | ADD    | PRESET:'Preset 3', SOMETHING:'CCC'                                    |
      | TTAB_ONE   | LL35 | ADD    | PRESET:'Preset 4', SOMETHING:'DDD'                                    |
      | TTAB_ONE   | LL36 | ADD    | PRESET:'Preset 5', SOMETHING:'EEE'                                    |
      | TTAB_ONE   | DDD  | REMOVE |                                                                       |
      | TTAB_ONE   | EEE  | UPDATE | PRESET:'Preset 5'=>'Preset 5 updated', SOMETHING:'EEE'=>'EEE updated' |
      | TTAB_TWO   | JJJ2 | ADD    | VALUE:'One', OTHER:'Other JJJ2'                                       |
      | TTAB_TWO   | VVV2 | ADD    | VALUE:'Two', OTHER:'Other VVV2'                                       |
      | TTAB_TWO   | VVV3 | ADD    | VALUE:'Three', OTHER:'Other 333'                                      |
      | TTAB_THREE | C    | REMOVE |                                                                       |
      | TTAB_THREE | B    | UPDATE | OTHER:'Other B'=>'Other B updated'                                    |
      | TTAB_THREE | D    | ADD    | OTHER:'Other D'                                                       |

  Scenario: The details for an existing commit can be displayed and filtered - regexp on table
    Given the commit ":tada: Test commit init" has been saved with all the identified initial diff content
    And these changes are applied to table "TTAB_ONE" :
      | change | key | value | preset           | something   |
      | add    | 32  | LL32  | Preset 1         | AAA         |
      | add    | 33  | LL33  | Preset 2         | BBB         |
      | add    | 34  | LL34  | Preset 3         | CCC         |
      | add    | 35  | LL35  | Preset 4         | DDD         |
      | add    | 36  | LL36  | Preset 5         | EEE         |
      | delete | 38  | DDD   |                  |             |
      | update | 39  | EEE   | Preset 5 updated | EEE updated |
    And these changes are applied to table "TTAB_TWO" :
      | change | key  | value | other      |
      | add    | JJJ2 | One   | Other JJJ2 |
      | add    | VVV2 | Two   | Other VVV2 |
      | add    | VVV3 | Three | Other 333  |
    And these changes are applied to table "TTAB_THREE" :
      | change | key   | value | other           |
      | delete | 33333 | C     |                 |
      | update | 22222 | B     | Other B updated |
      | add    | 44444 | D     | Other D         |
    And a new commit ":construction: Update 1" has been saved with all the new identified diff content
    When the user access to list of commits
    And the user select the details of commit ":construction: Update 1"
    And apply a content filter criteria "TTA._T.*" on "table"
    Then the commit details are displayed with this content :
      | Table      | Key  | Action | Payload                            |
      | TTAB_TWO   | JJJ2 | ADD    | VALUE:'One', OTHER:'Other JJJ2'    |
      | TTAB_TWO   | VVV2 | ADD    | VALUE:'Two', OTHER:'Other VVV2'    |
      | TTAB_TWO   | VVV3 | ADD    | VALUE:'Three', OTHER:'Other 333'   |
      | TTAB_THREE | C    | REMOVE |                                    |
      | TTAB_THREE | B    | UPDATE | OTHER:'Other B'=>'Other B updated' |
      | TTAB_THREE | D    | ADD    | OTHER:'Other D'                    |

  Scenario: The details for an existing commit can be displayed and filtered - regexp key
    Given the commit ":tada: Test commit init" has been saved with all the identified initial diff content
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
    And a new commit ":construction: Update 1" has been saved with all the new identified diff content
    When the user access to list of commits
    And the user select the details of commit ":construction: Update 1"
    And apply a content filter criteria "VVV\d" on "key"
    Then the commit details are displayed with this content :
      | Table      | Key  | Action | Payload                            |
      | TTAB_ONE   | VVV4 | ADD    | PRESET:'Preset 3', SOMETHING:'CCC' |
      | TTAB_ONE   | VVV5 | ADD    | PRESET:'Preset 4', SOMETHING:'DDD' |
      | TTAB_TWO   | VVV2 | ADD    | VALUE:'Two', OTHER:'Other VVV2'    |
      | TTAB_TWO   | VVV3 | ADD    | VALUE:'Three', OTHER:'Other 333'   |
      | TTAB_THREE | VVVD | ADD    | OTHER:'Other VVVD'                 |

  Scenario: The details for an existing commit can be displayed and filtered - value type
    Given the commit ":tada: Test commit init" has been saved with all the identified initial diff content
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
    And a new commit ":construction: Update 1" has been saved with all the new identified diff content
    When the user access to list of commits
    And the user select the details of commit ":construction: Update 1"
    And apply a content filter criteria "REMOVE" on "type"
    Then the commit details are displayed with this content :
      | Table      | Key | Action | Payload |
      | TTAB_ONE   | DDD | REMOVE |         |
      | TTAB_THREE | C   | REMOVE |         |

  Scenario: The details for an existing commit can be displayed and filtered - regexp combined key and table
    Given the commit ":tada: Test commit init" has been saved with all the identified initial diff content
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
    And a new commit ":construction: Update 1" has been saved with all the new identified diff content
    When the user access to list of commits
    And the user select the details of commit ":construction: Update 1"
    And apply a content filter criteria "VVV\d" on "key"
    And apply a content filter criteria "TTA._T.*" on "table"
    Then the commit details are displayed with this content :
      | Table      | Key  | Action | Payload                          |
      | TTAB_TWO   | VVV2 | ADD    | VALUE:'Two', OTHER:'Other VVV2'  |
      | TTAB_TWO   | VVV3 | ADD    | VALUE:'Three', OTHER:'Other 333' |
      | TTAB_THREE | VVVD | ADD    | OTHER:'Other VVVD'               |

  Scenario: The details for an existing commit can be displayed and sorted - sort by key
    Given the commit ":tada: Test commit init" has been saved with all the identified initial diff content
    And these changes are applied to table "TTAB_ONE" :
      | change | key | value | preset           | something   |
      | add    | 32  | LL32  | Preset 1         | AAA         |
      | add    | 33  | LL33  | Preset 2         | BBB         |
      | add    | 34  | LL34  | Preset 3         | CCC         |
      | add    | 35  | LL35  | Preset 4         | DDD         |
      | add    | 36  | LL36  | Preset 5         | EEE         |
      | delete | 38  | DDD   |                  |             |
      | update | 39  | EEE   | Preset 5 updated | EEE updated |
    And these changes are applied to table "TTAB_TWO" :
      | change | key  | value | other      |
      | add    | JJJ2 | One   | Other JJJ2 |
      | add    | VVV2 | Two   | Other VVV2 |
      | add    | VVV3 | Three | Other 333  |
    And these changes are applied to table "TTAB_THREE" :
      | change | key   | value | other           |
      | delete | 33333 | C     |                 |
      | update | 22222 | B     | Other B updated |
      | add    | 44444 | D     | Other D         |
    And a new commit ":construction: Update 1" has been saved with all the new identified diff content
    When the user access to list of commits
    And the user select the details of commit ":construction: Update 1"
    And apply a content sort criteria "ASC" on "key"
    Then the commit details are displayed with this sorted content :
      | Table      | Key  | Action | Payload                                                               |
      | TTAB_THREE | B    | UPDATE | OTHER:'Other B'=>'Other B updated'                                    |
      | TTAB_THREE | C    | REMOVE |                                                                       |
      | TTAB_THREE | D    | ADD    | OTHER:'Other D'                                                       |
      | TTAB_ONE   | DDD  | REMOVE |                                                                       |
      | TTAB_ONE   | EEE  | UPDATE | PRESET:'Preset 5'=>'Preset 5 updated', SOMETHING:'EEE'=>'EEE updated' |
      | TTAB_TWO   | JJJ2 | ADD    | VALUE:'One', OTHER:'Other JJJ2'                                       |
      | TTAB_ONE   | LL32 | ADD    | PRESET:'Preset 1', SOMETHING:'AAA'                                    |
      | TTAB_ONE   | LL33 | ADD    | PRESET:'Preset 2', SOMETHING:'BBB'                                    |
      | TTAB_ONE   | LL34 | ADD    | PRESET:'Preset 3', SOMETHING:'CCC'                                    |
      | TTAB_ONE   | LL35 | ADD    | PRESET:'Preset 4', SOMETHING:'DDD'                                    |
      | TTAB_ONE   | LL36 | ADD    | PRESET:'Preset 5', SOMETHING:'EEE'                                    |
      | TTAB_TWO   | VVV2 | ADD    | VALUE:'Two', OTHER:'Other VVV2'                                       |
      | TTAB_TWO   | VVV3 | ADD    | VALUE:'Three', OTHER:'Other 333'                                      |

  Scenario: The details for an existing commit can be displayed and sorted - combined sort table and key
    Given the commit ":tada: Test commit init" has been saved with all the identified initial diff content
    And these changes are applied to table "TTAB_ONE" :
      | change | key | value | preset           | something   |
      | add    | 32  | LL32  | Preset 1         | AAA         |
      | add    | 33  | LL33  | Preset 2         | BBB         |
      | add    | 34  | LL34  | Preset 3         | CCC         |
      | add    | 35  | LL35  | Preset 4         | DDD         |
      | add    | 36  | LL36  | Preset 5         | EEE         |
      | delete | 38  | DDD   |                  |             |
      | update | 39  | EEE   | Preset 5 updated | EEE updated |
    And these changes are applied to table "TTAB_TWO" :
      | change | key  | value | other      |
      | add    | JJJ2 | One   | Other JJJ2 |
      | add    | VVV2 | Two   | Other VVV2 |
      | add    | VVV3 | Three | Other 333  |
    And these changes are applied to table "TTAB_THREE" :
      | change | key   | value | other           |
      | delete | 33333 | C     |                 |
      | update | 22222 | B     | Other B updated |
      | add    | 44444 | D     | Other D         |
    And a new commit ":construction: Update 1" has been saved with all the new identified diff content
    When the user access to list of commits
    And the user select the details of commit ":construction: Update 1"
    And apply a content sort criteria "DESC" on "table"
    And apply a content sort criteria "ASC" on "key"
    Then the commit details are displayed with this sorted content :
      | Table      | Key  | Action | Payload                                                               |
      | TTAB_TWO   | JJJ2 | ADD    | VALUE:'One', OTHER:'Other JJJ2'                                       |
      | TTAB_TWO   | VVV2 | ADD    | VALUE:'Two', OTHER:'Other VVV2'                                       |
      | TTAB_TWO   | VVV3 | ADD    | VALUE:'Three', OTHER:'Other 333'                                      |
      | TTAB_THREE | B    | UPDATE | OTHER:'Other B'=>'Other B updated'                                    |
      | TTAB_THREE | C    | REMOVE |                                                                       |
      | TTAB_THREE | D    | ADD    | OTHER:'Other D'                                                       |
      | TTAB_ONE   | DDD  | REMOVE |                                                                       |
      | TTAB_ONE   | EEE  | UPDATE | PRESET:'Preset 5'=>'Preset 5 updated', SOMETHING:'EEE'=>'EEE updated' |
      | TTAB_ONE   | LL32 | ADD    | PRESET:'Preset 1', SOMETHING:'AAA'                                    |
      | TTAB_ONE   | LL33 | ADD    | PRESET:'Preset 2', SOMETHING:'BBB'                                    |
      | TTAB_ONE   | LL34 | ADD    | PRESET:'Preset 3', SOMETHING:'CCC'                                    |
      | TTAB_ONE   | LL35 | ADD    | PRESET:'Preset 4', SOMETHING:'DDD'                                    |
      | TTAB_ONE   | LL36 | ADD    | PRESET:'Preset 5', SOMETHING:'EEE'                                    |

  Scenario: The details for an existing commit is available through pagination
    # configured over 10000
    Given the commit ":tada: Test commit init" has been saved with all the identified initial diff content
    And the 9999 generated data in managed table "TTAB_TWO" :
      | key  | value | other     |
      | K_%% | LL%%  | Preset %% |
    And a new commit ":construction: Update 1" has been saved with all the new identified diff content
    And the 9999 generated data in managed table "TTAB_TWO" :
      | key   | value | other     |
      | KB_%% | LL%%  | Preset %% |
    And a new commit ":construction: Update 2" has been saved with all the new identified diff content
    When the user access to list of commits
    And the user select the details of commit ":construction: Update 2"
    Then the commit details are displayed with 9999 payloads

  Scenario: The commit indexes keep previous payload at each change - simple commit
    Given the commit ":tada: Test commit init" has been saved with all the identified initial diff content
    And these changes are applied to table "TTAB_ONE" :
      | change | key | value | preset           | something   |
      | delete | 38  | DDD   |                  |             |
      | update | 39  | EEE   | Preset 5 updated | EEE updated |
    And a new commit ":construction: Update 1" has been saved with all the new identified diff content
    When the user access to list of commits
    And the user select the details of commit ":construction: Update 1"
    Then the commit index has these managed technical payloads :
      | Table    | Key | Action | Current payload                                    | Previous payload                   |
      | TTAB_ONE | DDD | REMOVE |                                                    | PRESET:'Preset 4', SOMETHING:'DDD' |
      | TTAB_ONE | EEE | UPDATE | PRESET:'Preset 5 updated', SOMETHING:'EEE updated' | PRESET:'Preset 5', SOMETHING:'EEE' |

  Scenario: The commit indexes keep previous payload at each change - multiple commits
    Given the commit ":tada: Test commit init" has been saved with all the identified initial diff content
    And these changes are applied to table "TTAB_ONE" :
      | change | key | value | preset           | something   |
      | delete | 38  | DDD   |                  |             |
      | update | 39  | EEE   | Preset 5 updated | EEE updated |
    And a new commit ":construction: Update 1" has been saved with all the new identified diff content
    And these changes are applied to table "TTAB_ONE" :
      | change | key | value | preset                 | something         |
      | delete | 14  | AAA   |                        |                   |
      | update | 39  | EEE   | Preset 5 updated again | EEE updated again |
      | add    | 38  | DDD   | Recreate line          | DDD Recreate line |
      | add    | 99  | NEW   | New line               | New line          |
    And a new commit ":construction: Update 2" has been saved with all the new identified diff content
    And these changes are applied to table "TTAB_ONE" :
      | change | key | value | preset               | something                |
      | delete | 39  | EEE   |                      |                          |
      | update | 38  | DDD   | Recreate line update | DDD Recreate line update |
      | delete | 99  | NEW   |                      |                          |
      | add    | 14  | AAA   | Recreate aaa         | DDD Recreate aaa         |
    And a new commit ":construction: Update 3" has been saved with all the new identified diff content
    When we check content in datagate database
    Then the index for commit ":construction: Update 1" has these managed technical payloads :
      | Table    | Key | Action | Current payload                                    | Previous payload                   |
      | TTAB_ONE | DDD | REMOVE |                                                    | PRESET:'Preset 4', SOMETHING:'DDD' |
      | TTAB_ONE | EEE | UPDATE | PRESET:'Preset 5 updated', SOMETHING:'EEE updated' | PRESET:'Preset 5', SOMETHING:'EEE' |
    And the index for commit ":construction: Update 2" has these managed technical payloads :
      | Table    | Key | Action | Current payload                                                | Previous payload                                   |
      | TTAB_ONE | AAA | REMOVE |                                                                | PRESET:'Preset 1', SOMETHING:'AAA'                 |
      | TTAB_ONE | EEE | UPDATE | PRESET:'Preset 5 updated again', SOMETHING:'EEE updated again' | PRESET:'Preset 5 updated', SOMETHING:'EEE updated' |
      | TTAB_ONE | DDD | ADD    | PRESET:'Recreate line', SOMETHING:'DDD Recreate line'          |                                                    |
      | TTAB_ONE | NEW | ADD    | PRESET:'New line', SOMETHING:'New line'                        |                                                    |
    And the index for commit ":construction: Update 3" has these managed technical payloads :
      | Table    | Key | Action | Current payload                                                     | Previous payload                                               |
      | TTAB_ONE | EEE | REMOVE |                                                                     | PRESET:'Preset 5 updated again', SOMETHING:'EEE updated again' |
      | TTAB_ONE | DDD | UPDATE | PRESET:'Recreate line update', SOMETHING:'DDD Recreate line update' | PRESET:'Recreate line', SOMETHING:'DDD Recreate line'          |
      | TTAB_ONE | NEW | REMOVE |                                                                     | PRESET:'New line', SOMETHING:'New line'                        |
      | TTAB_ONE | AAA | ADD    | PRESET:'Recreate aaa', SOMETHING:'DDD Recreate aaa'                 |                                                                |

  Scenario: The changes can be selected to be rollbacked during a commit - apply on null value updates
    Given the commit ":tada: Test commit init" has been saved with all the identified initial diff content
    And these changes are applied to table "TTAB_ONE" :
      | change | key | value | preset | something |
      | add    | 32  | LL32  |        | with null |
    And a new commit ":construction: Update 1" has been saved with all the new identified diff content
    And these changes are applied to table "TTAB_ONE" :
      | change | key | value | preset | something |
      | update | 32  | LL32  | value  | with null |
    And a new diff analysis has been started and completed
    And the user has asked rollback on all content for commit
    And the user has specified a commit comment ":construction: All rollback"
    When the user save the commit
    Then the request is a success
    And the data in managed table "TTAB_ONE" is now :
      | key | value | preset   | something |
      | 14  | AAA   | Preset 1 | AAA       |
      | 25  | BBB   | Preset 2 | BBB       |
      | 37  | CCC   | Preset 3 | CCC       |
      | 38  | DDD   | Preset 4 | DDD       |
      | 39  | EEE   | Preset 5 | EEE       |
      | 32  | LL32  |          | with null |

  Scenario: Null keys are stored in commits using a special character - single key
    Given the commit ":tada: Test commit init" has been saved with all the identified initial diff content
    And a created parameter table with name "Table nullable key" for managed table "TTAB_THREE_KEYS" and columns selected as this :
      | name       | selection |
      | FIRST_KEY  | selected  |
      | SECOND_KEY | key       |
      | THIRD_KEY  | ignored   |
    And the existing data in managed table "TTAB_THREE_KEYS" :
      | secondKey    | firstKey         |
      | -null-       | something        |
      | -space-      | something else   |
      | -empty char- | something else 2 |
      | aaaa         | something aaaa   |
    And the user add new version "v2"
    And a new commit ":construction: Commit on v2" has been saved with all the new identified diff content
    Then the commit ":construction: Commit on v2" is added to commit list for current project
    And the saved commit content has these identified changes :
      | Table           | Key          | Action | Payload                      |
      | TTAB_THREE_KEYS | -null-       | ADD    | FIRST_KEY:'something'        |
      | TTAB_THREE_KEYS | -space-      | ADD    | FIRST_KEY:'something else'   |
      | TTAB_THREE_KEYS | -empty char- | ADD    | FIRST_KEY:'something else 2' |
      | TTAB_THREE_KEYS | aaaa         | ADD    | FIRST_KEY:'something aaaa'   |

  Scenario: Null keys are stored in commits using a special character - composite key
    Given the commit ":tada: Test commit init" has been saved with all the identified initial diff content
    And a created parameter table with name "Table nullable key" for managed table "TTAB_THREE_KEYS" and columns selected as this :
      | name       | selection |
      | FIRST_KEY  | key       |
      | SECOND_KEY | key       |
      | THIRD_KEY  | key       |
    And the existing data in managed table "TTAB_THREE_KEYS" :
      | firstKey     | secondKey    | thirdKey     |
      | one          | -null-       | something    |
      | -space-      | -null-       | -empty char- |
      | -empty char- | -space-      | -null-       |
      | 4            | aaa          | -null-       |
      | 5            | 5            | 5            |
      | 6            | -empty char- | 6            |
    And the user add new version "v2"
    And a new commit ":construction: Commit on v2" has been saved with all the new identified diff content
    Then the commit ":construction: Commit on v2" is added to commit list for current project
    And the saved commit content has these identified changes :
      | Table           | Key                        | Action | Payload |
      | TTAB_THREE_KEYS | "one / -null- / something" | ADD    |         |
      | TTAB_THREE_KEYS | "  / -null- / "            | ADD    |         |
      | TTAB_THREE_KEYS | " /   / -null-"            | ADD    |         |
      | TTAB_THREE_KEYS | 4 / aaa / -null-           | ADD    |         |
      | TTAB_THREE_KEYS | 5 / 5 / 5                  | ADD    |         |
      | TTAB_THREE_KEYS | 6 /  / 6                   | ADD    |         |
