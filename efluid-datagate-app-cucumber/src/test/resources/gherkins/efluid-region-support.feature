Feature: For Efluid needs a transformation can select the required values to keep for some biz regions

  Scenario: The data processed on merge in destination environment is selected regarding the region specified in transformer - demo 1
    Given the test is an Efluid standard scenario
    And the existing data in managed table "TTEST1" :
      | id      | col1   |
      | $1-regA | line 1 |
      | $2-regB | line 2 |
      | $3-regB | line 3 |
      | $4-regC | line 4 |
      | $5-regA | line 5 |
    And the existing data in managed table "TTEST2" :
      | id      | col1   |
      | $1-regB | line 1 |
      | $2-regC | line 2 |
      | $3-regA | line 3 |
      | $4-regA | line 4 |
    And the existing data in secondary parameter table "TRECOPIEPARAMREFERENTIELDIR" :
      | DIR  | TABNAME | OP  | COLS_PK | SRC_ID1 | SRC_ID2 | SRC_ID3 | SRC_ID4 | SRC_ID5 |
      | regA | TTEST1  | INS | + ID    | $1-regA |         |         |         |         |
      | regA | TTEST1  | INS | + ID    | $5-regA |         |         |         |         |
      | regB | TTEST1  | INS | + ID    | $2-regB |         |         |         |         |
      | regB | TTEST1  | INS | + ID    | $3-regB |         |         |         |         |
      | regC | TTEST1  | INS | + ID    | $4-regC |         |         |         |         |
      | regA | TTEST2  | INS | + ID    | $3-regA |         |         |         |         |
      | regA | TTEST2  | INS | + ID    | $4-regA |         |         |         |         |
      | regB | TTEST2  | INS | + ID    | $1-regB |         |         |         |         |
      | regC | TTEST2  | INS | + ID    | $2-regC |         |         |         |         |
    And a new commit "init for region support" has been saved with all the new identified diff content
    Given the configured transformers for project "Default" :
      | name          | type                  | priority | configuration  |
      | Transformer 1 | EFLUID_REGION_SUPPORT | 1        | {"todo"="..."} |
    And the user has requested an export of the commit with name ":construction: Update 1" and this customization for transformer "Transformer 1" :
      """json
      {
        "todo" : "???",
        "for-project": "test"
      }
      """
    And the user accesses to the destination environment with the same dictionary
    And no existing data in managed table "TTEST1" in destination environment
    And no existing data in managed table "TTEST2" in destination environment
    And the existing data in secondary parameter table "TAPPLICATIONINFO" in destination environment :
      | PROJET | SITE |
      | test   | regA |
    And a merge diff analysis has been started and completed with the available source package
    When the user access to merge commit page
    Then the merge commit content is rendered with these identified changes :
      | Table  | Key     | Action | Need Resolve | Payload       |
      | TTEST1 | $1-regA | ADD    | true         | COL1:'line 1' |
      | TTEST1 | $5-regA | ADD    | true         | COL1:'line 5' |
      | TTEST2 | $3-regA | ADD    | true         | COL1:'line 3' |
      | TTEST2 | $4-regA | ADD    | true         | COL1:'line 4' |

  Scenario Outline: The data processed on merge in destination environment is selected regarding the region specified in transformer - various cases
    Given the test is an Efluid standard scenario
    And the existing data in managed table "TTEST1" :
      | id      | col1   |
      | $1-regA | line 1 |
      | $2-regB | line 2 |
      | $3-regB | line 3 |
      | $4-regC | line 4 |
      | $5-regA | line 5 |
    And the existing data in managed table "TTEST2" :
      | id      | col1   |
      | $1-regB | line 1 |
      | $2-regC | line 2 |
      | $3-regA | line 3 |
      | $4-regA | line 4 |
    And the existing data in secondary parameter table "TRECOPIEPARAMREFERENTIELDIR" :
      | dir  | tabname | op  | colsPk | srcId1  | srcId2 | srcId3 | srcId4 | srcId5 |
      | regA | TTEST1  | INS | + ID   | $1-regA |        |        |        |        |
      | regA | TTEST1  | INS | + ID   | $5-regA |        |        |        |        |
      | regB | TTEST1  | INS | + ID   | $2-regB |        |        |        |        |
      | regB | TTEST1  | INS | + ID   | $3-regB |        |        |        |        |
      | regC | TTEST1  | INS | + ID   | $4-regC |        |        |        |        |
      | regA | TTEST2  | INS | + ID   | $3-regA |        |        |        |        |
      | regA | TTEST2  | INS | + ID   | $4-regA |        |        |        |        |
      | regB | TTEST2  | INS | + ID   | $1-regB |        |        |        |        |
      | regC | TTEST2  | INS | + ID   | $2-regC |        |        |        |        |
    And a new commit "init for region support" has been saved with all the new identified diff content
    Given the configured transformers for project "Default" :
      | name          | type                  | priority | configuration  |
      | Transformer 1 | EFLUID_REGION_SUPPORT | 1        | {"todo"="..."} |
    And the user has requested an export of the commit with name "init for region support" and this customization for transformer "Transformer 1" :
      """json
      {
        "todo" : "???",
        "for-project": "test"
      }
      """
    And the user accesses to the destination environment with the same dictionary
    And no existing data in managed table "TTEST1" in destination environment
    And no existing data in managed table "TTEST2" in destination environment
    And the existing data in secondary parameter table "TAPPLICATIONINFO" in destination environment :
      | projet | site          |
      | test   | <region code> |
    And a merge diff analysis has been started and completed with the available source package
    When the user access to merge commit page
    Then the merge commit content is rendered with changes for these lines : "<identified changes>"
    Examples:
      | region code | identified changes                                                             |
      | regA        | ADD TTEST1:$1-regA, ADD TTEST1:$5-regA, ADD TTEST2:$3-regA, ADD TTEST2:$4-regA |
      | regB        | ADD TTEST1:$2-regB, ADD TTEST1:$3-regA, ADD TTEST2:$1-regB                     |
      | regC        | ADD TTEST1:$4-regC, ADD TTEST2:$2-regC                                         |
