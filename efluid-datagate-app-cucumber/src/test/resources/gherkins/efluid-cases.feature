Feature: A complete set of test case are specified for Efluid needs

  The tests cases here are copy of efluid internal tests

  @TestStandard
  Scenario: Efluid merge 1
    Given the test is an Efluid standard scenario
    And the existing data in managed table "TTEST1" :
      | id        | col1           |
      | $testa_d  | testa delete   |
      | $testa_i1 | testa insert 1 |
      | $testa_u  | testa update 1 |
    And the commit ":tada: Test commit init" has been saved with all the identified initial diff content
    And these changes are applied to table "TTEST1" :
      | change | id        | col1             |
      | delete | $testa_d  |                  |
      | add    | $testa_i2 | testa insert 2   |
      | update | $testa_u  | testa update 1 2 |
    And a new commit ":construction: Update 1" has been saved with all the new identified diff content
    And the user has requested an export of the commit with name ":construction: Update 1"
    And the user accesses to the destination environment with the same dictionary
    And the existing data in managed table "TTEST1" in destination environment :
      | id        | col1           |
      | $testa_d  | testa delete   |
      | $testa_i1 | testa insert 1 |
      | $testa_u  | testa update 1 |
    And a commit ":construction: Destination commit initial" has been saved with all the new identified diff content in destination environment
    And a merge diff analysis has been started and completed with the available source package
    When the user access to merge commit page
    Then the merge commit content has these resolution details for table "TTEST1" on key "$testa_i2" :
      | Type       | Action | Payload               |
      | their      | ADD    | COL1:'testa insert 2' |
      | mine       |        |                       |
      | resolution | ADD    | COL1:'testa insert 2' |
    And the merge commit content has these resolution details for table "TTEST1" on key "$testa_d" :
      | Type       | Action | Payload             |
      | their      | REMOVE |                     |
      | mine       |     |  |
      | resolution | REMOVE |                     |
    And the merge commit content is rendered with these identified changes :
      | Table  | Key       | Action | Payload                                   |
      | TTEST1 | $testa_d  | REMOVE |                                           |
      | TTEST1 | $testa_u  | UPDATE | COL1:'testa update 1'=>'testa update 1 2' |
      | TTEST1 | $testa_i2 | ADD    | COL1:'testa insert 2'                     |


  @TestNumber
  Scenario: Efluid merge cas number
    Given the test is an Efluid standard scenario
    And the existing data in managed table "EFLUIDTESTNUMBER" :
      | id        | col1           | col2      |
      | $testa_d  | testa delete   | 123456789 |
      | $testa_i1 | testa insert 1 | 123456789 |
      | $testa_u  | testa update 1 | 123456789 |
    And the commit ":tada: Test commit init" has been saved with all the identified initial diff content
    And these changes are applied to table "EFLUIDTESTNUMBER" :
      | change | id        | col1             | col2      |
      | delete | $testa_d  |                  |           |
      | add    | $testa_i2 | testa insert 2   | 123456789 |
      | update | $testa_u  | testa update 1 2 | 987654321 |
    And a new commit ":construction: Update 1" has been saved with all the new identified diff content
    And the user has requested an export of the commit with name ":construction: Update 1"
    And the user accesses to the destination environment with the same dictionary
    And the existing data in managed table "EFLUIDTESTNUMBER" in destination environment :
      | id        | col1           | col2      |
      | $testa_d  | testa delete   | 123456789 |
      | $testa_i1 | testa insert 1 | 123456789 |
      | $testa_u  | testa update 1 | 123456789 |
    And a commit ":construction: Destination commit initial" has been saved with all the new identified diff content in destination environment
    And a merge diff analysis has been started and completed with the available source package
    When the user access to merge commit page
    Then the merge commit content is rendered with these identified changes :
      | Table            | Key       | Action | Payload                                                              |
      | EFLUIDTESTNUMBER | $testa_d  | REMOVE |                                                                      |
      | EFLUIDTESTNUMBER | $testa_u  | UPDATE | COL1:'testa update 1'=>'testa update 1 2', COL2:123456789=>987654321 |
      | EFLUIDTESTNUMBER | $testa_i2 | ADD    | COL1:'testa insert 2', COL2:123456789                                |


  @TestMultiDataType
  Scenario: Efluid merge multi data type
    Given the test is an Efluid standard scenario
    And the existing data in managed table "TTESTMULTIDATATYPE" :
      | id        | col1           | col2           | col3      | col4       | col5                | col6 | col7        |
      | $testj_d  | testj delete   | testj varchar2 | 123456789 | 2012-01-15 | 2012-01-15 00:00:00 | y    | clob delete |
      | $testj_i1 | testj insert 1 | testj varchar2 | 123456789 | 2012-01-15 | 2012-01-15 00:00:00 | n    | clob insert |
      | $testj_u  | testj update 1 | testj varchar2 | 123456789 | 2012-01-15 | 2012-01-15 00:00:00 | y    | clob update |
    And the commit ":tada: Test commit init" has been saved with all the identified initial diff content
    And these changes are applied to table "TTESTMULTIDATATYPE" :
      | change | id        | col1             | col2           | col3      | col4       | col5                | col6 | col7        |
      | delete | $testj_d  |                  |                |           |            |                     |      |             |
      | add    | $testj_i2 | testj insert 2   |                |           |            |                     |      |             |
      | update | $testj_u  | testj update 1 2 | testj varchar2 | 123456789 | 2012-01-15 | 2012-01-15 00:00:00 | y    | clob update |
    And a new commit ":construction: Update 1" has been saved with all the new identified diff content
    And the user has requested an export of the commit with name ":construction: Update 1"
    And the user accesses to the destination environment with the same dictionary
    And the existing data in managed table "TTESTMULTIDATATYPE" in destination environment :
      | id        | col1           | col2           | col3      | col4       | col5                | col6 | col7        |
      | $testj_d  | testj delete   | testj varchar2 | 123456789 | 2012-01-15 | 2012-01-15 00:00:00 | y    | clob delete |
      | $testj_i1 | testj insert 1 | testj varchar2 | 123456789 | 2012-01-15 | 2012-01-15 00:00:00 | n    | clob insert |
      | $testj_u  | testj update 1 | testj varchar2 | 123456789 | 2012-01-15 | 2012-01-15 00:00:00 | y    | clob update |
    And a commit ":construction: Destination commit initial" has been saved with all the new identified diff content in destination environment
    And a merge diff analysis has been started and completed with the available source package
    When the user access to merge commit page
    Then the merge commit content is rendered with these identified changes :
      | Table              | Key       | Action | Payload                                                               |
      | TTESTMULTIDATATYPE | $testj_d  | REMOVE |                                                                       |
      | TTESTMULTIDATATYPE | $testj_u  | UPDATE | COL1:'testj update 1'=>'testj update 1 2'                             |
      | TTESTMULTIDATATYPE | $testj_i2 | ADD    | COL1:'testj insert 2', COL2:'', COL3:, COL4:, COL5:, COL6:'', COL7:'' |

  @TestFusionLot1et2Insert
  Scenario: Efluid test fusion lot 1 insert + lot 2 insert
    Given the test is an Efluid standard scenario
    And the existing data in managed table "TTEST1" :
      | id        | col1           |
      | $testa_d1 | testa delete 1 |
      | $testa_d2 | testa delete 2 |
      | $testa_d3 | testa delete 3 |
      | $testa_i1 | testa insert 1 |
      | $testa_i2 | testa insert 2 |
      | $testa_i3 | testa insert 3 |
      | $testa_u1 | testa update 1 |
      | $testa_u2 | testa update 2 |
      | $testa_u3 | testa update 3 |
    And the commit ":tada: Test commit init" has been saved with all the identified initial diff content
    And these changes are applied to table "TTEST1" :
      | change | id        | col1                                           |
      | add    | $testa_i4 | testk : insert lot 2 + insert lot 3 ---> lot 2 |
    And a new commit ":construction: Update 1" has been saved with all the new identified diff content
    And these changes are applied to table "TTEST1" :
      | change | id        | col1                                           |
      | add    | $testa_i5 | testk : insert lot 2 + insert lot 3 ---> lot 3 |
    And a new commit ":construction: Update 2" has been saved with all the new identified diff content
    And the user has requested an export starting by the commit with name ":construction: Update 1"
    And the user accesses to the destination environment with the same dictionary
    And the existing data in managed table "TTEST1" in destination environment :
      | id        | col1           |
      | $testa_d1 | testa delete 1 |
      | $testa_d2 | testa delete 2 |
      | $testa_d3 | testa delete 3 |
      | $testa_i1 | testa insert 1 |
      | $testa_i2 | testa insert 2 |
      | $testa_i3 | testa insert 3 |
      | $testa_u1 | testa update 1 |
      | $testa_u2 | testa update 2 |
      | $testa_u3 | testa update 3 |
    And a commit ":construction: Destination commit initial" has been saved with all the new identified diff content in destination environment
    And a merge diff analysis has been started and completed with the available source package
    When the user access to merge commit page
    Then the merge commit content is rendered with these identified changes :
      | Table  | Key       | Action | Payload                                               |
      | TTEST1 | $testa_i4 | ADD    | COL1:'testk : insert lot 2 + insert lot 3 ---> lot 2' |
      | TTEST1 | $testa_i5 | ADD    | COL1:'testk : insert lot 2 + insert lot 3 ---> lot 3' |

  @TestFusionLot1et2InsertUpdate
  Scenario: Efluid test fusion lot 1 insert + lot 2 update
    Given the test is an Efluid standard scenario
    And the existing data in managed table "TTEST1" :
      | id        | col1           |
      | $testa_d1 | testa delete 1 |
      | $testa_d2 | testa delete 2 |
      | $testa_d3 | testa delete 3 |
      | $testa_i1 | testa insert 1 |
      | $testa_i2 | testa insert 2 |
      | $testa_i3 | testa insert 3 |
      | $testa_u1 | testa update 1 |
      | $testa_u2 | testa update 2 |
      | $testa_u3 | testa update 3 |
    And the commit ":tada: Test commit init" has been saved with all the identified initial diff content
    And these changes are applied to table "TTEST1" :
      | change | id        | col1                                           |
      | add    | $testa_i4 | testk : insert lot 2 + insert lot 3 ---> lot 2 |
    And a new commit ":construction: Update 1" has been saved with all the new identified diff content
    And these changes are applied to table "TTEST1" :
      | change | id        | col1                                           |
      | update | $testa_i4 | testk : insert lot 2 + insert lot 3 ---> lot 3 |
    And a new commit ":construction: Update 2" has been saved with all the new identified diff content
    And the user has requested an export starting by the commit with name ":construction: Update 1"
    And the user accesses to the destination environment with the same dictionary
    And the existing data in managed table "TTEST1" in destination environment :
      | id        | col1           |
      | $testa_d1 | testa delete 1 |
      | $testa_d2 | testa delete 2 |
      | $testa_d3 | testa delete 3 |
      | $testa_i1 | testa insert 1 |
      | $testa_i2 | testa insert 2 |
      | $testa_i3 | testa insert 3 |
      | $testa_u1 | testa update 1 |
      | $testa_u2 | testa update 2 |
      | $testa_u3 | testa update 3 |
    And a commit ":construction: Destination commit initial" has been saved with all the new identified diff content in destination environment
    And a merge diff analysis has been started and completed with the available source package
    When the user access to merge commit page
    Then the merge commit content is rendered with these identified changes :
      | Table  | Key       | Action | Payload                                               |
      | TTEST1 | $testa_i4 | ADD    | COL1:'testk : insert lot 2 + insert lot 3 ---> lot 3' |


  @TestFusionLot1et2InsertDelete
  Scenario: Efluid test fusion lot 1 insert + lot 2 delete
    Given the test is an Efluid standard scenario
    And the existing data in managed table "TTEST1" :
      | id        | col1           |
      | $testa_d1 | testa delete 1 |
      | $testa_d2 | testa delete 2 |
      | $testa_d3 | testa delete 3 |
      | $testa_i1 | testa insert 1 |
      | $testa_i2 | testa insert 2 |
      | $testa_i3 | testa insert 3 |
      | $testa_u1 | testa update 1 |
      | $testa_u2 | testa update 2 |
      | $testa_u3 | testa update 3 |
    And the commit ":tada: Test commit init" has been saved with all the identified initial diff content
    And these changes are applied to table "TTEST1" :
      | change | id        | col1                                           |
      | add    | $testa_i4 | testk : insert lot 2 + insert lot 3 ---> lot 2 |
    And a new commit ":construction: Update 1" has been saved with all the new identified diff content
    And these changes are applied to table "TTEST1" :
      | change | id        | col1 |
      | delete | $testa_i4 |      |
    And a new commit ":construction: Update 2" has been saved with all the new identified diff content
    And the user has requested an export starting by the commit with name ":construction: Update 1"
    And the user accesses to the destination environment with the same dictionary
    And the existing data in managed table "TTEST1" in destination environment :
      | id        | col1           |
      | $testa_d1 | testa delete 1 |
      | $testa_d2 | testa delete 2 |
      | $testa_d3 | testa delete 3 |
      | $testa_i1 | testa insert 1 |
      | $testa_i2 | testa insert 2 |
      | $testa_i3 | testa insert 3 |
      | $testa_u1 | testa update 1 |
      | $testa_u2 | testa update 2 |
      | $testa_u3 | testa update 3 |
    And a commit ":construction: Destination commit initial" has been saved with all the new identified diff content in destination environment
    And a merge diff analysis has been started and completed with the available source package
    When the user access to merge commit page
    Then the merge commit content is rendered with these identified changes :
      | Table | Key | Action | Payload |


  @TestFusionLot1et2UpdateUpdate
  Scenario: Efluid test fusion lot 1 update + lot 2 update
    Given the test is an Efluid standard scenario
    And the existing data in managed table "TTEST1" :
      | id        | col1           |
      | $testa_d1 | testa delete 1 |
      | $testa_d2 | testa delete 2 |
      | $testa_d3 | testa delete 3 |
      | $testa_i1 | testa insert 1 |
      | $testa_i2 | testa insert 2 |
      | $testa_i3 | testa insert 3 |
      | $testa_u1 | testa update 1 |
      | $testa_u2 | testa update 2 |
      | $testa_u3 | testa update 3 |
    And the commit ":tada: Test commit init" has been saved with all the identified initial diff content
    And these changes are applied to table "TTEST1" :
      | change | id        | col1                                           |
      | update | $testa_u3 | testn : update lot 2 + update lot 3 ---> lot 2 |
    And a new commit ":construction: Update 1" has been saved with all the new identified diff content
    And these changes are applied to table "TTEST1" :
      | change | id        | col1                                           |
      | update | $testa_u3 | testn : update lot 2 + update lot 3 ---> lot 3 |
    And a new commit ":construction: Update 2" has been saved with all the new identified diff content
    And the user has requested an export starting by the commit with name ":construction: Update 1"
    And the user accesses to the destination environment with the same dictionary
    And the existing data in managed table "TTEST1" in destination environment :
      | id        | col1           |
      | $testa_d1 | testa delete 1 |
      | $testa_d2 | testa delete 2 |
      | $testa_d3 | testa delete 3 |
      | $testa_i1 | testa insert 1 |
      | $testa_i2 | testa insert 2 |
      | $testa_i3 | testa insert 3 |
      | $testa_u1 | testa update 1 |
      | $testa_u2 | testa update 2 |
      | $testa_u3 | testa update 3 |
    And a commit ":construction: Destination commit initial" has been saved with all the new identified diff content in destination environment
    And a merge diff analysis has been started and completed with the available source package
    When the user access to merge commit page
    Then the merge commit content is rendered with these identified changes :
      | Table  | Key       | Action | Payload                                                                 |
      | TTEST1 | $testa_u3 | UPDATE | COL1:'testa update 3'=>'testn : update lot 2 + update lot 3 ---> lot 3' |

  @TestFusionLot1et2UpdateDelete
  Scenario: Efluid test fusion lot 1 update + lot 2 delete
    Given the test is an Efluid standard scenario
    And the existing data in managed table "TTEST1" :
      | id        | col1           |
      | $testa_d1 | testa delete 1 |
      | $testa_d2 | testa delete 2 |
      | $testa_d3 | testa delete 3 |
      | $testa_i1 | testa insert 1 |
      | $testa_i2 | testa insert 2 |
      | $testa_i3 | testa insert 3 |
      | $testa_u1 | testa update 1 |
      | $testa_u2 | testa update 2 |
      | $testa_u3 | testa update 3 |
    And the commit ":tada: Test commit init" has been saved with all the identified initial diff content
    And these changes are applied to table "TTEST1" :
      | change | id        | col1                                           |
      | update | $testa_u3 | testn : update lot 2 + update lot 3 ---> lot 2 |
    And a new commit ":construction: Update 1" has been saved with all the new identified diff content
    And these changes are applied to table "TTEST1" :
      | change | id        | col1 |
      | delete | $testa_u3 |      |
    And a new commit ":construction: Update 2" has been saved with all the new identified diff content
    And the user has requested an export starting by the commit with name ":construction: Update 1"
    And the user accesses to the destination environment with the same dictionary
    And the existing data in managed table "TTEST1" in destination environment :
      | id        | col1           |
      | $testa_d1 | testa delete 1 |
      | $testa_d2 | testa delete 2 |
      | $testa_d3 | testa delete 3 |
      | $testa_i1 | testa insert 1 |
      | $testa_i2 | testa insert 2 |
      | $testa_i3 | testa insert 3 |
      | $testa_u1 | testa update 1 |
      | $testa_u2 | testa update 2 |
      | $testa_u3 | testa update 3 |
    And a commit ":construction: Destination commit initial" has been saved with all the new identified diff content in destination environment
    And a merge diff analysis has been started and completed with the available source package
    When the user access to merge commit page
    Then the merge commit content is rendered with these identified changes :
      | Table  | Key       | Action | Payload |
      | TTEST1 | $testa_u3 | REMOVE |         |

  @TestFusionLot1et2DeleteInsert
  Scenario: Efluid test fusion lot 1 delete + lot 2 insert
    Given the test is an Efluid standard scenario
    And the existing data in managed table "TTEST1" :
      | id        | col1           |
      | $testa_d1 | testa delete 1 |
      | $testa_d2 | testa delete 2 |
      | $testa_d3 | testa delete 3 |
      | $testa_i1 | testa insert 1 |
      | $testa_i2 | testa insert 2 |
      | $testa_i3 | testa insert 3 |
      | $testa_u1 | testa update 1 |
      | $testa_u2 | testa update 2 |
      | $testa_u3 | testa update 3 |
    And the commit ":tada: Test commit init" has been saved with all the identified initial diff content
    And these changes are applied to table "TTEST1" :
      | change | id        | col1 |
      | delete | $testa_d3 |      |
    And a new commit ":construction: Update 1" has been saved with all the new identified diff content
    And these changes are applied to table "TTEST1" :
      | change | id        | col1                                           |
      | add    | $testa_d3 | testp : delete lot 2 + insert lot 3 ---> lot 3 |
    And a new commit ":construction: Update 2" has been saved with all the new identified diff content
    And the user has requested an export starting by the commit with name ":construction: Update 1"
    And the user accesses to the destination environment with the same dictionary
    And the existing data in managed table "TTEST1" in destination environment :
      | id        | col1           |
      | $testa_d1 | testa delete 1 |
      | $testa_d2 | testa delete 2 |
      | $testa_d3 | testa delete 3 |
      | $testa_i1 | testa insert 1 |
      | $testa_i2 | testa insert 2 |
      | $testa_i3 | testa insert 3 |
      | $testa_u1 | testa update 1 |
      | $testa_u2 | testa update 2 |
      | $testa_u3 | testa update 3 |
    And a commit ":construction: Destination commit initial" has been saved with all the new identified diff content in destination environment
    And a merge diff analysis has been started and completed with the available source package
    When the user access to merge commit page
    Then the merge commit content is rendered with these identified changes :
      | Table  | Key       | Action | Payload                                                                 |
      | TTEST1 | $testa_d3 | UPDATE | COL1:'testa delete 3'=>'testp : delete lot 2 + insert lot 3 ---> lot 3' |

  @TestDoublePk
  Scenario: Efluid merge cas double Pk - commit unitaire
    Given the test is an Efluid standard scenario
    And the existing data in managed table "EFLUIDTESTPKCOMPOSITE" :
      | id            | id2           | col1           |
      | $testb_id1_d  | $testb_id1_d  | testb delete   |
      | $testb_id1_i1 | $testb_id2_i1 | testb insert 1 |
      | $testb_id1_u  | $testb_id2_u  | testb update 1 |
    And the commit ":tada: Test commit init" has been saved with all the identified initial diff content
    And these changes are applied to table "EFLUIDTESTPKCOMPOSITE" :
      | change | id            | id2           | col1             |
      | delete | $testb_id1_d  | $testb_id1_d  | testb delete     |
      | add    | $testb_id1_i2 | $testb_id2_i2 | testb insert 2   |
      | update | $testb_id1_u  | $testb_id2_u  | testb update 1 2 |
    And a new commit ":construction: Update 1" has been saved with all the new identified diff content
    And the user has requested an export of the commit with name ":construction: Update 1"
    And the user accesses to the destination environment with the same dictionary
    And the existing data in managed table "EFLUIDTESTPKCOMPOSITE" in destination environment :
      | id            | id2           | col1           |
      | $testb_id1_d  | $testb_id1_d  | testb delete   |
      | $testb_id1_i1 | $testb_id2_i1 | testb insert 1 |
      | $testb_id1_u  | $testb_id2_u  | testb update 1 |
    And a commit ":construction: Destination commit initial" has been saved with all the new identified diff content in destination environment
    And a merge diff analysis has been started and completed with the available source package
    When the user access to merge commit page
    Then the merge commit content is rendered with these identified changes :
      | Table                 | Key                           | Action | Payload                                   |
      | EFLUIDTESTPKCOMPOSITE | $testb_id1_d / $testb_id1_d   | REMOVE |                                           |
      | EFLUIDTESTPKCOMPOSITE | $testb_id1_u / $testb_id2_u   | UPDATE | COL1:'testb update 1'=>'testb update 1 2' |
      | EFLUIDTESTPKCOMPOSITE | $testb_id1_i2 / $testb_id2_i2 | ADD    | COL1:'testb insert 2'                     |

  @TestDoublePk
  Scenario: Efluid merge cas double Pk - tous les commits
    Given the test is an Efluid standard scenario
    And the existing data in managed table "EFLUIDTESTPKCOMPOSITE" :
      | id            | id2           | col1           |
      | $testb_id1_d  | $testb_id1_d  | testb delete   |
      | $testb_id1_i1 | $testb_id2_i1 | testb insert 1 |
      | $testb_id1_u  | $testb_id2_u  | testb update 1 |
    And the commit ":tada: Test commit init" has been saved with all the identified initial diff content
    And these changes are applied to table "EFLUIDTESTPKCOMPOSITE" :
      | change | id            | id2           | col1             |
      | delete | $testb_id1_d  | $testb_id1_d  | testb delete     |
      | add    | $testb_id1_i2 | $testb_id2_i2 | testb insert 2   |
      | update | $testb_id1_u  | $testb_id2_u  | testb update 1 2 |
    And a new commit ":construction: Update 1" has been saved with all the new identified diff content
    And the user has requested an export starting by the commit with name ":tada: Test commit init"
    And the user accesses to the destination environment with the same dictionary
    And the existing data in managed table "EFLUIDTESTPKCOMPOSITE" in destination environment :
      | id            | id2           | col1           |
      | $testb_id1_d  | $testb_id1_d  | testb delete   |
      | $testb_id1_i1 | $testb_id2_i1 | testb insert 1 |
      | $testb_id1_u  | $testb_id2_u  | testb update 1 |
    And a commit ":construction: Destination commit initial" has been saved with all the new identified diff content in destination environment
    And a merge diff analysis has been started and completed with the available source package
    When the user access to merge commit page
    Then the merge commit content is rendered with these identified changes :
      | Table                 | Key                           | Action | Need Resolve | Payload                                   |
      | EFLUIDTESTPKCOMPOSITE | $testb_id1_d / $testb_id1_d   | REMOVE | true         |                                           |
      | EFLUIDTESTPKCOMPOSITE | $testb_id1_i1 / $testb_id2_i1 | ADD    | false        | COL1:'testb insert 1'                     |
      | EFLUIDTESTPKCOMPOSITE | $testb_id1_u / $testb_id2_u   | UPDATE | true         | COL1:'testb update 1'=>'testb update 1 2' |
      | EFLUIDTESTPKCOMPOSITE | $testb_id1_i2 / $testb_id2_i2 | ADD    | true         | COL1:'testb insert 2'                     |

  @TestDoublePk
  Scenario: Efluid merge cas double Pk - tous les commits puis save et autre update
    Given the test is an Efluid standard scenario
    And the existing data in managed table "EFLUIDTESTPKCOMPOSITE" :
      | id            | id2           | col1           |
      | $testb_id1_d  | $testb_id1_d  | testb delete   |
      | $testb_id1_i1 | $testb_id2_i1 | testb insert 1 |
      | $testb_id1_u  | $testb_id2_u  | testb update 1 |
    And the commit ":tada: Test commit init" has been saved with all the identified initial diff content
    And these changes are applied to table "EFLUIDTESTPKCOMPOSITE" :
      | change | id            | id2           | col1             |
      | delete | $testb_id1_d  | $testb_id1_d  | testb delete     |
      | add    | $testb_id1_i2 | $testb_id2_i2 | testb insert 2   |
      | update | $testb_id1_u  | $testb_id2_u  | testb update 1 2 |
    And a new commit ":construction: Update 1" has been saved with all the new identified diff content
    And the user has requested an export starting by the commit with name ":tada: Test commit init"
    And the user accesses to the destination environment with the same dictionary
    And the existing data in managed table "EFLUIDTESTPKCOMPOSITE" in destination environment :
      | id            | id2           | col1           |
      | $testb_id1_d  | $testb_id1_d  | testb delete   |
      | $testb_id1_i1 | $testb_id2_i1 | testb insert 1 |
      | $testb_id1_u  | $testb_id2_u  | testb update 1 |
    And a commit ":construction: Destination commit initial" has been saved with all the new identified diff content in destination environment
    And a merge diff analysis has been started and completed with the available source package
    And the user has selected all content for merge commit
    And the user has specified a commit comment ":construction: merge commit test with changes"
    And the user save the merge commit
    And the saved merge commit content has these identified changes :
      | Table                 | Key                           | Action | Payload                                   |
      | EFLUIDTESTPKCOMPOSITE | $testb_id1_d / $testb_id1_d   | REMOVE |                                           |
      | EFLUIDTESTPKCOMPOSITE | $testb_id1_u / $testb_id2_u   | UPDATE | COL1:'testb update 1'=>'testb update 1 2' |
      | EFLUIDTESTPKCOMPOSITE | $testb_id1_i2 / $testb_id2_i2 | ADD    | COL1:'testb insert 2'                     |
    And the data in managed table "EFLUIDTESTPKCOMPOSITE" in destination environment is now :
      | id            | id2           | col1             |
      | $testb_id1_i1 | $testb_id2_i1 | testb insert 1   |
      | $testb_id1_i2 | $testb_id2_i2 | testb insert 2   |
      | $testb_id1_u  | $testb_id2_u  | testb update 1 2 |
    And these changes are applied to table "TTEST1" :
      | change | id       | col1  |
      | add    | $testa_1 | test1 |
      | add    | $testa_2 | test2 |
    And a diff has already been launched
    And the diff is completed
    When the user access to diff commit page
    Then the commit content is rendered with these identified changes :
      | Table  | Key      | Action | Payload      |
      | TTEST1 | $testa_1 | ADD    | COL1:'test1' |
      | TTEST1 | $testa_2 | ADD    | COL1:'test2' |

  @TestFusionLot1et2InsertInsert
  Scenario: Efluid test fusion lot 1 insert + lot 2 insert
    Given the test is an Efluid standard scenario
    And the existing data in managed table "TTEST1" :
      | id        | col1           |
      | $testa_d1 | testa delete 1 |
      | $testa_d2 | testa delete 2 |
      | $testa_d3 | testa delete 3 |
      | $testa_i1 | testa insert 1 |
      | $testa_i2 | testa insert 2 |
      | $testa_i3 | testa insert 3 |
      | $testa_u1 | testa update 1 |
      | $testa_u2 | testa update 2 |
      | $testa_u3 | testa update 3 |
    And the commit ":tada: Test commit init" has been saved with all the identified initial diff content
    And these changes are applied to table "TTEST1" :
      | change | id        | col1                                           |
      | add    | $testa_i4 | testp : delete lot 2 + insert lot 3 ---> lot 3 |
    And a new commit ":construction: Update 1" has been saved with all the new identified diff content
    And these changes are applied to table "TTEST1" :
      | change | id        | col1                                           |
      | add    | $testa_i5 | testp : delete lot 2 + insert lot 3 ---> lot 3 |
    And a new commit ":construction: Update 2" has been saved with all the new identified diff content
    And the user has requested an export starting by the commit with name ":tada: Test commit init"
    And the user accesses to the destination environment with the same dictionary
    And the existing data in managed table "TTEST1" in destination environment :
      | id        | col1           |
      | $testa_d1 | testa delete 1 |
      | $testa_d2 | testa delete 2 |
      | $testa_d3 | testa delete 3 |
      | $testa_i1 | testa insert 1 |
      | $testa_i2 | testa insert 2 |
      | $testa_i3 | testa insert 3 |
      | $testa_u1 | testa update 1 |
      | $testa_u2 | testa update 2 |
      | $testa_u3 | testa update 3 |
    And a commit ":construction: Destination commit initial" has been saved with all the new identified diff content in destination environment
    And a merge diff analysis has been started and completed with the available source package
    When the user access to merge commit page
    Then the merge commit content is rendered with these identified changes :
      | Table  | Key       | Action | Payload                                               |
      | TTEST1 | $testa_i4 | ADD    | COL1:'testp : delete lot 2 + insert lot 3 ---> lot 3' |
      | TTEST1 | $testa_i5 | ADD    | COL1:'testp : delete lot 2 + insert lot 3 ---> lot 3' |


  @TestDiffTableVide
  Scenario: Efluid diff table vide 1
    Given the test is an Efluid standard scenario
    And the existing data in managed table "TTEST1" :
      | id | col1 |
    And a diff analysis can be started and completed
    And a diff has already been launched
    And the diff is completed
    When the user access to diff commit page
    Then no diff content has been found

  @TestDiffTableDataVide
  Scenario: Efluid diff table vide 1 - data null standard
    Given the test is an Efluid standard scenario
    And the existing data in managed table "TTEST1" :
      | id | col1 |
      | 1  |      |
      | 2  |      |
      | 3  |      |
      | 4  |      |
      | 5  |      |
      | 6  |      |
      | 7  |      |
      | 8  |      |
      | 9  |      |
      | 10 |      |
      | 11 |      |
      | 12 |      |
      | 13 |      |
    And a diff analysis can be started and completed
    And a diff has already been launched
    And the diff is completed
    When the user access to diff commit page
    Then the commit content is rendered with these identified changes :
      | Table  | Key | Action | Payload |
      | TTEST1 | 1   | ADD    | COL1:'' |
      | TTEST1 | 2   | ADD    | COL1:'' |
      | TTEST1 | 3   | ADD    | COL1:'' |
      | TTEST1 | 4   | ADD    | COL1:'' |
      | TTEST1 | 5   | ADD    | COL1:'' |
      | TTEST1 | 6   | ADD    | COL1:'' |
      | TTEST1 | 7   | ADD    | COL1:'' |
      | TTEST1 | 8   | ADD    | COL1:'' |
      | TTEST1 | 9   | ADD    | COL1:'' |
      | TTEST1 | 10  | ADD    | COL1:'' |
      | TTEST1 | 11  | ADD    | COL1:'' |
      | TTEST1 | 12  | ADD    | COL1:'' |
      | TTEST1 | 13  | ADD    | COL1:'' |

  @TestDiffTableDataVideNonRedite
  Scenario: Efluid diff table vide 2 - non redite
    Given the test is an Efluid standard scenario
    And the existing data in managed table "TTEST1" :
      | id | col1 |
      | 1  |      |
      | 2  |      |
      | 3  |      |
      | 4  |      |
      | 5  |      |
      | 6  |      |
      | 7  |      |
      | 8  |      |
      | 9  |      |
      | 10 |      |
      | 11 |      |
      | 12 |      |
      | 13 |      |
    And the commit ":tada: Test commit init with empty data" has been saved with all the identified initial diff content
    And a diff analysis can be started and completed
    And a diff has already been launched
    And the diff is completed
    When the user access to diff commit page
    Then no diff content has been found

  @TestBigData
  Scenario: Efluid diff large 1 - test loader
    Given the test is an Efluid standard scenario
    And the 5 generated data in managed table "TTESTMULTIDATATYPE" :
      | id         | col1     | col2           | col3 | col4       | col5                | col6 | col7    |
      | $testj_d%% | testj %% | testj varchar2 | 12%% | 2012-01-15 | 2012-01-15 00:00:00 | y    | clob %% |
    And a diff analysis can be started and completed
    And a diff has already been launched
    And the diff is completed
    When the user access to diff commit page
    Then the commit content is rendered with these identified changes :
      | Table              | Key       | Action | Payload                                                                                                                      |
      | TTESTMULTIDATATYPE | $testj_d1 | ADD    | COL1:'testj 1', COL2:'testj varchar2', COL3:121, COL4:2012-01-15 00:00:00, COL5:2012-01-15 00:00:00, COL6:'y', COL7:'clob 1' |
      | TTESTMULTIDATATYPE | $testj_d2 | ADD    | COL1:'testj 2', COL2:'testj varchar2', COL3:122, COL4:2012-01-15 00:00:00, COL5:2012-01-15 00:00:00, COL6:'y', COL7:'clob 2' |
      | TTESTMULTIDATATYPE | $testj_d3 | ADD    | COL1:'testj 3', COL2:'testj varchar2', COL3:123, COL4:2012-01-15 00:00:00, COL5:2012-01-15 00:00:00, COL6:'y', COL7:'clob 3' |
      | TTESTMULTIDATATYPE | $testj_d4 | ADD    | COL1:'testj 4', COL2:'testj varchar2', COL3:124, COL4:2012-01-15 00:00:00, COL5:2012-01-15 00:00:00, COL6:'y', COL7:'clob 4' |
      | TTESTMULTIDATATYPE | $testj_d5 | ADD    | COL1:'testj 5', COL2:'testj varchar2', COL3:125, COL4:2012-01-15 00:00:00, COL5:2012-01-15 00:00:00, COL6:'y', COL7:'clob 5' |

  @TestBigData
  Scenario: Efluid diff large 2 - loader use
    Given the test is an Efluid standard scenario
    And the 50000 generated data in managed table "TTESTMULTIDATATYPE" :
      | id         | col1     | col2           | col3 | col4       | col5                | col6 | col7    |
      | $testj_d%% | testj %% | testj varchar2 | 12%% | 2012-01-15 | 2012-01-15 00:00:00 | y    | clob %% |
    And a diff analysis can be started and completed
    And a diff has already been launched
    And the diff is completed
    Then the commit content has 50000 entries for managed table "TTESTMULTIDATATYPE"

  @TestEfluidDatabaseRules
  Scenario: On Efluid tables, linked table lines can use a null biz link key. With a specific rule the nullable linked entries should be valid - rule disabled
    Given the test is an Efluid standard scenario
    And the database doesn't support nullable join keys
    And the existing data in managed table "T_NULL_LINK_DEMO_SRC" :
      | id   | value     | destBizKey |
      | SRC1 | SRC_VAL_1 | DEST1_BIZ  |
      | SRC2 | SRC_VAL_2 | DEST2_BIZ  |
      | SRC3 | SRC_VAL_3 | DEST1_BIZ  |
      | SRC4 | SRC_VAL_4 | -null-     |
    And the existing data in managed table "T_NULL_LINK_DEMO_DEST" :
      | techKey | code        | bizKey    |
      | DEST1   | DEST_CODE_1 | DEST1_BIZ |
      | DEST2   | DEST_CODE_2 | DEST2_BIZ |
      | DEST3   | DEST_CODE_3 | DEST3_BIZ |
      | DEST4   | DEST_CODE_4 | DEST4_BIZ |
      | DEST5   | DEST_CODE_5 | -null-    |
    And a diff analysis can be started and completed
    And a diff has already been launched
    And the diff is completed
    When the user access to diff commit page
    Then these remarks on missing linked lines are rendered :
      | Table                | Key  | Payload           |
      | T_NULL_LINK_DEMO_SRC | SRC4 | VALUE:'SRC_VAL_4' |
    And the commit content is rendered with these identified changes :
      | Table                 | Key   | Action | Payload                                    |
      | T_NULL_LINK_DEMO_SRC  | SRC1  | ADD    | LN_DEST_BIZ_KEY:'DEST1', VALUE:'SRC_VAL_1' |
      | T_NULL_LINK_DEMO_SRC  | SRC2  | ADD    | LN_DEST_BIZ_KEY:'DEST2', VALUE:'SRC_VAL_2' |
      | T_NULL_LINK_DEMO_SRC  | SRC3  | ADD    | LN_DEST_BIZ_KEY:'DEST1', VALUE:'SRC_VAL_3' |
      | T_NULL_LINK_DEMO_DEST | DEST1 | ADD    | BIZ_KEY:'DEST1_BIZ', CODE:'DEST_CODE_1'    |
      | T_NULL_LINK_DEMO_DEST | DEST2 | ADD    | BIZ_KEY:'DEST2_BIZ', CODE:'DEST_CODE_2'    |
      | T_NULL_LINK_DEMO_DEST | DEST3 | ADD    | BIZ_KEY:'DEST3_BIZ', CODE:'DEST_CODE_3'    |
      | T_NULL_LINK_DEMO_DEST | DEST4 | ADD    | BIZ_KEY:'DEST4_BIZ', CODE:'DEST_CODE_4'    |
      | T_NULL_LINK_DEMO_DEST | DEST5 | ADD    | BIZ_KEY:'', CODE:'DEST_CODE_5'             |

  @TestEfluidDatabaseRules
  Scenario: On Efluid tables, linked table lines can use a null biz link key. With a specific rule the nullable linked entries should be valid - rule enabled
    Given the test is an Efluid standard scenario
    And the database does support nullable join keys
    And the existing data in managed table "T_NULL_LINK_DEMO_SRC" :
      | id   | value     | destBizKey |
      | SRC1 | SRC_VAL_1 | DEST1_BIZ  |
      | SRC2 | SRC_VAL_2 | DEST2_BIZ  |
      | SRC3 | SRC_VAL_3 | DEST1_BIZ  |
      | SRC4 | SRC_VAL_4 | -null-     |
    And the existing data in managed table "T_NULL_LINK_DEMO_DEST" :
      | techKey | code        | bizKey    |
      | DEST1   | DEST_CODE_1 | DEST1_BIZ |
      | DEST2   | DEST_CODE_2 | DEST2_BIZ |
      | DEST3   | DEST_CODE_3 | DEST3_BIZ |
      | DEST4   | DEST_CODE_4 | DEST4_BIZ |
      | DEST5   | DEST_CODE_5 | -null-    |
    And a diff analysis can be started and completed
    And a diff has already been launched
    And the diff is completed
    When the user access to diff commit page
    Then there is no remarks on missing linked lines
    And the commit content is rendered with these identified changes :
      | Table                 | Key   | Action | Payload                                    |
      | T_NULL_LINK_DEMO_SRC  | SRC1  | ADD    | LN_DEST_BIZ_KEY:'DEST1', VALUE:'SRC_VAL_1' |
      | T_NULL_LINK_DEMO_SRC  | SRC2  | ADD    | LN_DEST_BIZ_KEY:'DEST2', VALUE:'SRC_VAL_2' |
      | T_NULL_LINK_DEMO_SRC  | SRC3  | ADD    | LN_DEST_BIZ_KEY:'DEST1', VALUE:'SRC_VAL_3' |
      | T_NULL_LINK_DEMO_SRC  | SRC4  | ADD    | LN_DEST_BIZ_KEY:'DEST5', VALUE:'SRC_VAL_4' |
      | T_NULL_LINK_DEMO_DEST | DEST1 | ADD    | BIZ_KEY:'DEST1_BIZ', CODE:'DEST_CODE_1'    |
      | T_NULL_LINK_DEMO_DEST | DEST2 | ADD    | BIZ_KEY:'DEST2_BIZ', CODE:'DEST_CODE_2'    |
      | T_NULL_LINK_DEMO_DEST | DEST3 | ADD    | BIZ_KEY:'DEST3_BIZ', CODE:'DEST_CODE_3'    |
      | T_NULL_LINK_DEMO_DEST | DEST4 | ADD    | BIZ_KEY:'DEST4_BIZ', CODE:'DEST_CODE_4'    |
      | T_NULL_LINK_DEMO_DEST | DEST5 | ADD    | BIZ_KEY:'', CODE:'DEST_CODE_5'             |

  @TestEfluidDatabaseRules
  Scenario: On Efluid tables, linked table lines can use a null biz link key. Even if rule to allow it is enabled, the missing lines are seen in remarks
    Given the test is an Efluid standard scenario
    And the database does support nullable join keys
    And the existing data in managed table "T_NULL_LINK_DEMO_SRC" :
      | id   | value     | destBizKey |
      | SRC1 | SRC_VAL_1 | DEST1_BIZ  |
      | SRC2 | SRC_VAL_2 | DEST2_BIZ  |
      | SRC3 | SRC_VAL_3 | DEST1_BIZ  |
      | SRC4 | SRC_VAL_4 | DEST5_BIZ  |
    And the existing data in managed table "T_NULL_LINK_DEMO_DEST" :
      | techKey | code        | bizKey    |
      | DEST1   | DEST_CODE_1 | DEST1_BIZ |
      | DEST2   | DEST_CODE_2 | DEST2_BIZ |
      | DEST3   | DEST_CODE_3 | DEST3_BIZ |
      | DEST4   | DEST_CODE_4 | DEST4_BIZ |
    And a diff analysis can be started and completed
    And a diff has already been launched
    And the diff is completed
    When the user access to diff commit page
    Then these remarks on missing linked lines are rendered :
      | Table                | Key  | Payload                                     |
      | T_NULL_LINK_DEMO_SRC | SRC4 | VALUE:'SRC_VAL_4', DEST_BIZ_KEY:'DEST5_BIZ' |
    And the commit content is rendered with these identified changes :
      | Table                 | Key   | Action | Payload                                    |
      | T_NULL_LINK_DEMO_SRC  | SRC1  | ADD    | LN_DEST_BIZ_KEY:'DEST1', VALUE:'SRC_VAL_1' |
      | T_NULL_LINK_DEMO_SRC  | SRC2  | ADD    | LN_DEST_BIZ_KEY:'DEST2', VALUE:'SRC_VAL_2' |
      | T_NULL_LINK_DEMO_SRC  | SRC3  | ADD    | LN_DEST_BIZ_KEY:'DEST1', VALUE:'SRC_VAL_3' |
      | T_NULL_LINK_DEMO_DEST | DEST1 | ADD    | BIZ_KEY:'DEST1_BIZ', CODE:'DEST_CODE_1'    |
      | T_NULL_LINK_DEMO_DEST | DEST2 | ADD    | BIZ_KEY:'DEST2_BIZ', CODE:'DEST_CODE_2'    |
      | T_NULL_LINK_DEMO_DEST | DEST3 | ADD    | BIZ_KEY:'DEST3_BIZ', CODE:'DEST_CODE_3'    |
      | T_NULL_LINK_DEMO_DEST | DEST4 | ADD    | BIZ_KEY:'DEST4_BIZ', CODE:'DEST_CODE_4'    |

  @TestEfluidAuditTransformerRules
  Scenario: The audit transformer apply generated audit data on specified values - simple
    Given the test is an Efluid standard scenario
    And the configured transformers for project "Default" :
      | name  | type         | priority | configuration                                                                                        |
      | Audit | EFLUID_AUDIT | 1        | {"tablePattern":".*","appliedKeyPatterns":[".*"],"dateUpdates":{},"actorUpdates":{"ACTEUR.*":"bob"}} |
    And the existing data in managed table "T_EFLUID_TEST_AUDIT" :
      | id | value  | etatObjet    | dateSuppression | dateModification | dateCreation | acteurSuppression | acteurModification | acteurCreation |
      | 1  | INIT_1 | TODO_DELETED | 2018-02-17      | 2018-02-16       | 2018-02-15   | admin_del_src     | admin_src          | admin_src      |
      | 2  | INIT_2 | TODO_UPDATE  | 2018-02-17      | 2018-02-16       | 2018-02-15   | admin_del_src2    | admin_src2         | admin_src2     |
    And a new commit ":construction: Update 1" has been saved with all the new identified diff content
    And the user has requested an export of the commit with name ":construction: Update 1" and this customization for transformer "Audit" :
      """json
      {
          "tablePattern":"T_EFLUID_TEST_AUDIT",
          "appliedKeyPatterns":[".*"],
          "appliedValueFilterPatterns":{"ETAT_OBJET":".*"},
          "dateUpdates":{"DATE_.*":"2020-05-11"},
          "actorUpdates":{
            "ACTEUR_.*":"evt 154654"
           }
      }
      """
    And the user accesses to the destination environment with the same dictionary
    And the existing data in managed table "T_EFLUID_TEST_AUDIT" in destination environment :
      | id | value  | etatObjet | dateSuppression | dateModification | dateCreation | acteurSuppression | acteurModification | acteurCreation |
      | 2  | INIT_2 | OK        | 2020-05-11      | 2020-05-11       | 2020-05-11   | evt 154654        | evt 154654         | evt 154654     |
    And a commit ":construction: Destination commit initial" has been saved with all the new identified diff content in destination environment
    And a merge diff analysis has been started and completed with the available source package
    When the user access to merge commit page
    Then the merge commit content is rendered with these identified changes :
      | Table               | Key | Action | Need Resolve | Payload                                                                                                                                                                                                                                                    |
      | T_EFLUID_TEST_AUDIT | 1   | ADD    | true         | VALUE:'INIT_1', ETAT_OBJET:'TODO_DELETED', DATE_SUPPRESSION:2020-05-11 00:00:00, DATE_MODIFICATION:2020-05-11 00:00:00, DATE_CREATION:2020-05-11 00:00:00, ACTEUR_SUPPRESSION:'evt 154654', ACTEUR_MODIFICATION:'evt 154654', ACTEUR_CREATION:'evt 154654' |
      | T_EFLUID_TEST_AUDIT | 2   | UPDATE | true         | ETAT_OBJET:'OK'=>'TODO_UPDATE'                                                                                                                                                                                                                             |

  @TestEfluidAuditTransformerRules
  Scenario: The audit transformer apply generated audit data on specified values - complexe
    Given the test is an Efluid standard scenario
    And the configured transformers for project "Default" :
      | name  | type         | priority | configuration                                                                                        |
      | Audit | EFLUID_AUDIT | 1        | {"tablePattern":".*","appliedKeyPatterns":[".*"],"dateUpdates":{},"actorUpdates":{"ACTEUR.*":"bob"}} |
    And the existing data in managed table "T_EFLUID_TEST_AUDIT" :
      | id       | value | etatObjet       | dateSuppression | dateModification | dateCreation | acteurSuppression | acteurModification | acteurCreation |
      | CHG_1    | 11    | TODO_DELETED    | 2018-02-17      | 2018-02-16       | 2018-02-15   | admin_del_src     | admin_src          | admin_src      |
      | CHG_2    | 22    | NOT_TODO_UPDATE | 2018-02-17      | 2018-02-16       | 2018-02-15   | admin_del_src2    | admin_src2         | admin_src2     |
      | CHG_3    | 33    | NOT_TODO_UPDATE | 2018-02-17      | 2018-02-16       | 2018-02-15   | admin_del_src2    | admin_src2         | admin_src2     |
      | CHG_4    | 44    | TODO_UPDATE     | 2018-02-17      | 2018-02-16       | 2018-02-15   | admin_del_src2    | admin_src2         | admin_src2     |
      | NO_CHG_1 | 55    | TODO_UPDATE     | 2018-02-17      | 2018-02-16       | 2018-02-15   | admin_del_src2    | admin_src2         | admin_src2     |
      | NO_CHG_2 | 66    | TODO_UPDATE     | 2018-02-17      | 2018-02-16       | 2018-02-15   | admin_del_src2    | admin_src2         | admin_src2     |
      | CHG_5    | 77    | TODO_ADD        | 2018-02-17      | 2018-02-16       | 2018-02-15   | admin_del_src2    | admin_src2         | admin_src2     |
      | NO_CHG_3 | 88    | TODO_ADD        | 2018-02-17      | 2018-02-16       | 2018-02-15   | admin_del_src2    | admin_src2         | admin_src2     |
    And a new commit ":construction: Update 1" has been saved with all the new identified diff content
    And the user has requested an export of the commit with name ":construction: Update 1" and this customization for transformer "Audit" :
      """json
      {
          "tablePattern":"T_EFLUID_TEST_AUDIT",
          "appliedKeyPatterns":["CHG.*"],
          "appliedValueFilterPatterns":{"ETAT_OBJET":"TODO_.*"},
          "dateUpdates":{"DATE_.*TION":"2020-05-11"},
          "actorUpdates":{
            "ACTEUR_SUPPRESSION":"evt SUPPR",
            "ACTEUR_MODIFICATION":"evt MOD",
            "ACTEUR_CREATION":"evt CRE"
           }
      }
      """
    And the user accesses to the destination environment with the same dictionary
    And the existing data in managed table "T_EFLUID_TEST_AUDIT" in destination environment :
      | id       | value | etatObjet       | dateSuppression | dateModification | dateCreation | acteurSuppression | acteurModification | acteurCreation |
      | CHG_1    | 11    | TODO_DELETED    | 2018-02-17      | 2020-05-11       | 2020-05-11   | evt SUPPR         | evt MOD            | evt CRE        |
      | CHG_2    | 22    | NOT_TODO_UPDATE | 2020-05-11      | 2020-05-11       | 2018-02-15   | admin_del_src2    | admin_src2         | admin_src2     |
      | CHG_3    | 33    | NOT_TODO_UPDATE | 2020-05-11      | 2018-02-16       | 2018-02-15   | admin_del_src2    | admin_src2         | admin_src2     |
      | CHG_4    | 44    | TODO_UPDATE     | 2020-05-11      | 2018-02-16       | 2018-02-15   | admin_del_src2    | admin_src2         | evt CRE        |
      | NO_CHG_1 | 55    | TODO_UPDATE     | 2020-05-11      | 2018-02-16       | 2018-02-15   | admin_del_src1    | admin_src1         | admin_src1     |
      | NO_CHG_2 | 66    | TODO_UPDATE     | 2020-05-11      | 2018-02-16       | 2018-02-15   | admin_del_src2    | admin_src2         | admin_src2     |
    And a commit ":construction: Destination commit initial" has been saved with all the new identified diff content in destination environment
    And a merge diff analysis has been started and completed with the available source package
    When the user access to merge commit page
    Then the merge commit content is rendered with these identified changes :
      | Table               | Key      | Action | Need Resolve | Payload                                                                                                                                                                                                                                                                      |
      | T_EFLUID_TEST_AUDIT | CHG_1    | ADD    | false        | VALUE:'11', ETAT_OBJET:'TODO_DELETED', DATE_SUPPRESSION:2018-02-17 00:00:00, DATE_MODIFICATION:2020-05-11 00:00:00, DATE_CREATION:2020-05-11 00:00:00, ACTEUR_SUPPRESSION:'evt SUPPR', ACTEUR_MODIFICATION:'evt MOD', ACTEUR_CREATION:'evt CRE'                              |
      | T_EFLUID_TEST_AUDIT | CHG_2    | UPDATE | true         | DATE_SUPPRESSION:2020-05-11 00:00:00=>2018-02-17 00:00:00, DATE_MODIFICATION:2020-05-11 00:00:00=>2018-02-16 00:00:00                                                                                                                                                        |
      | T_EFLUID_TEST_AUDIT | CHG_3    | UPDATE | true         | DATE_SUPPRESSION:2020-05-11 00:00:00=>2018-02-17 00:00:00                                                                                                                                                                                                                    |
      | T_EFLUID_TEST_AUDIT | CHG_4    | UPDATE | true         | DATE_SUPPRESSION:2020-05-11 00:00:00=>2018-02-17 00:00:00, DATE_MODIFICATION:2018-02-16 00:00:00=>2020-05-11 00:00:00, DATE_CREATION:2018-02-15 00:00:00=>2020-05-11 00:00:00, ACTEUR_SUPPRESSION:'admin_del_src2'=>'evt SUPPR', ACTEUR_MODIFICATION:'admin_src2'=>'evt MOD' |
      | T_EFLUID_TEST_AUDIT | NO_CHG_1 | UPDATE | true         | DATE_SUPPRESSION:2020-05-11 00:00:00=>2018-02-17 00:00:00, ACTEUR_SUPPRESSION:'admin_del_src1'=>'admin_del_src2', ACTEUR_MODIFICATION:'admin_src1'=>'admin_src2', ACTEUR_CREATION:'admin_src1'=>'admin_src2'                                                                 |
      | T_EFLUID_TEST_AUDIT | NO_CHG_2 | UPDATE | true         | DATE_SUPPRESSION:2020-05-11 00:00:00=>2018-02-17 00:00:00                                                                                                                                                                                                                    |
      | T_EFLUID_TEST_AUDIT | CHG_5    | ADD    | true         | VALUE:'77', ETAT_OBJET:'TODO_ADD', DATE_SUPPRESSION:2018-02-17 00:00:00, DATE_MODIFICATION:2020-05-11 00:00:00, DATE_CREATION:2020-05-11 00:00:00, ACTEUR_SUPPRESSION:'evt SUPPR', ACTEUR_MODIFICATION:'evt MOD', ACTEUR_CREATION:'evt CRE'                                  |
      | T_EFLUID_TEST_AUDIT | NO_CHG_3 | ADD    | true         | VALUE:'88', ETAT_OBJET:'TODO_ADD', DATE_SUPPRESSION:2018-02-17 00:00:00, DATE_MODIFICATION:2018-02-16 00:00:00, DATE_CREATION:2018-02-15 00:00:00, ACTEUR_SUPPRESSION:'admin_del_src2', ACTEUR_MODIFICATION:'admin_src2', ACTEUR_CREATION:'admin_src2'                       |

  @TestEfluidAuditTransformerRules
  Scenario: The audit transformer apply generated audit data on specified values even when null - matcher out of scope
    Given the test is an Efluid standard scenario
    And the configured transformers for project "Default" :
      | name  | type         | priority | configuration                                                                                        |
      | Audit | EFLUID_AUDIT | 1        | {"tablePattern":".*","appliedKeyPatterns":[".*"],"dateUpdates":{},"actorUpdates":{"ACTEUR.*":"bob"}} |
    And the existing data in managed table "T_EFLUID_TEST_AUDIT" :
      | id | value | etatObjet | dateSuppression | dateModification | dateCreation | acteurSuppression | acteurModification | acteurCreation |
      | 1  | 1     |           |                 |                  |              |                   |                    |                |
      | 2  | 2     |           |                 |                  |              |                   |                    |                |
    And a new commit ":construction: Update 1" has been saved with all the new identified diff content
    And the user has requested an export of the commit with name ":construction: Update 1" and this customization for transformer "Audit" :
      """json
      {
          "tablePattern" : ".*",
          "appliedKeyPatterns" : [ "TRA$.*" ],
          "appliedValueFilterPatterns" : {
              "ETAT_OBJET" : "0"
          },
          "dateUpdates" : {
              "DATE_SUPPRESSION" : "current_date",
              "DATE_MODIFICATION" : "current_date",
              "DATE_CREATION" : "current_date"
          },
          "actorUpdates" : {
              "ACTEUR_MODIFICATION" : "evt 12345",
              "ACTEUR_CREATION" : "evt 67890",
              "ACTEUR_SUPPRESSION" : "evt 295556"
          }
      }
      """
    And the user accesses to the destination environment with the same dictionary
    And no existing data in managed table "T_EFLUID_TEST_AUDIT" in destination environment
    And a merge diff analysis has been started and completed with the available source package
    When the user access to merge commit page
    Then the merge commit content is rendered with these identified changes :
      | Table               | Key | Action | Need Resolve | Payload                                                                                                                                            |
      | T_EFLUID_TEST_AUDIT | 1   | ADD    | true         | VALUE:'1', ETAT_OBJET:'', DATE_SUPPRESSION:, DATE_MODIFICATION:, DATE_CREATION:, ACTEUR_SUPPRESSION:'', ACTEUR_MODIFICATION:'', ACTEUR_CREATION:'' |
      | T_EFLUID_TEST_AUDIT | 2   | ADD    | true         | VALUE:'2', ETAT_OBJET:'', DATE_SUPPRESSION:, DATE_MODIFICATION:, DATE_CREATION:, ACTEUR_SUPPRESSION:'', ACTEUR_MODIFICATION:'', ACTEUR_CREATION:'' |

  @TestEfluidAuditTransformerRules
  Scenario: The audit transformer apply generated audit data on specified values even when null - correct matcher
    Given the test is an Efluid standard scenario
    And the configured transformers for project "Default" :
      | name  | type         | priority | configuration                                                                                        |
      | Audit | EFLUID_AUDIT | 1        | {"tablePattern":".*","appliedKeyPatterns":[".*"],"dateUpdates":{},"actorUpdates":{"ACTEUR.*":"bob"}} |
    And the existing data in managed table "T_EFLUID_TEST_AUDIT" :
      | id | value | etatObjet | dateSuppression | dateModification | dateCreation | acteurSuppression | acteurModification | acteurCreation |
      | 1  | 1     |           |                 |                  |              |                   |                    |                |
      | 2  | 2     |           |                 |                  |              |                   |                    |                |
    And a new commit ":construction: Update 1" has been saved with all the new identified diff content
    And the user has requested an export of the commit with name ":construction: Update 1" and this customization for transformer "Audit" :
      """json
      {
          "tablePattern" : ".*",
          "appliedKeyPatterns" : [ ".*" ],
          "dateUpdates" : {
              "DATE_SUPPRESSION" : "current_date",
              "DATE_MODIFICATION" : "current_date",
              "DATE_CREATION" : "current_date"
          },
          "actorUpdates" : {
              "ACTEUR_MODIFICATION" : "evt 12345",
              "ACTEUR_CREATION" : "evt 67890",
              "ACTEUR_SUPPRESSION" : "evt 295556"
          }
      }
      """
    And the user accesses to the destination environment with the same dictionary
    And no existing data in managed table "T_EFLUID_TEST_AUDIT" in destination environment
    And a merge diff analysis has been started and completed with the available source package
    When the user access to merge commit page
    Then the merge commit content is rendered with these identified changes :
      | Table               | Key | Action | Need Resolve | Payload                                                                                                                                                                                                                                 |
      | T_EFLUID_TEST_AUDIT | 1   | ADD    | true         | VALUE:'1', ETAT_OBJET:'', DATE_SUPPRESSION:2020-06-12 22:14:00, DATE_MODIFICATION:2020-06-12 22:14:00, DATE_CREATION:2020-06-12 22:14:00, ACTEUR_SUPPRESSION:'evt 295556', ACTEUR_MODIFICATION:'evt 12345', ACTEUR_CREATION:'evt 67890' |
      | T_EFLUID_TEST_AUDIT | 2   | ADD    | true         | VALUE:'2', ETAT_OBJET:'', DATE_SUPPRESSION:2020-06-12 22:14:00, DATE_MODIFICATION:2020-06-12 22:14:00, DATE_CREATION:2020-06-12 22:14:00, ACTEUR_SUPPRESSION:'evt 295556', ACTEUR_MODIFICATION:'evt 12345', ACTEUR_CREATION:'evt 67890' |
