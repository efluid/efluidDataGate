Feature: A complete set of test case are specified for Efluid needs

  The tests cases here are copy of efluid internal tests

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
    When the user access to diff commit page
    Then the merge commit content is rendered with these identified changes :
      | Table  | Key       | Action | Payload                                   |
      | TTEST1 | $testa_d  | REMOVE |                                           |
      | TTEST1 | $testa_u  | UPDATE | COL1:'testa update 1'=>'testa update 1 2' |
      | TTEST1 | $testa_i2 | ADD    | COL1:'testa insert 2'                     |
