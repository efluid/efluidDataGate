Feature: For Efluid needs a transformation can select the required values to keep for some biz regions

  Scenario Outline: The transformer configuration validation rules depends on transformer type - EFLUID_REGION_SUPPORT
    Given the user has initialized a new transformer of type "EFLUID_REGION_SUPPORT", with name "<name>" and this configuration :
      """json
      <configuration>
      """
    When the user save the transformer
    Then the result "<result>" is provided
    Examples:
      | name    | configuration                              | result                                   |
      | region1 | {"tablePattern":".*","project":"test"}     | SUCCESS                                  |
      | region1 | {"tablePattern":"DATE_C","project":"test"} | SUCCESS                                  |
      | region1 | {"project":"test"}                         | tablePattern cannot be empty or missing. |
      | region1 | {"tablePattern":"DATE_C"}                  | project cannot be empty or missing       |

  Scenario: A comment is provided on parameter table content when the transformer is edited or created
    Given the test is an Efluid standard scenario
    And the existing data in secondary parameter table "TRECOPIEPARAMREFERENTIELDIR" :
      | dir  | tabname | op  | colsPk | srcId1  | srcId2 | srcId3 | srcId4 | srcId5 |
      | regA | TTEST1  | INS | + ID   | $1-regA |        |        |        |        |
      | regA | TTEST1  | INS | + ID   | $5-regA |        |        |        |        |
      | regB | TTEST1  | INS | + ID   | $2-regB |        |        |        |        |
      | regB | TTEST1  | INS | + ID   | $3-regB |        |        |        |        |
      | regC | TTEST1  | INS | + ID   | $4-regC |        |        |        |        |
      | regA | TTEST2  | INS | + ID   | $3-regA |        |        |        |        |
      | regC | TTEST2  | INS | + ID   | $2-regC |        |        |        |        |
    Given from the home page
    When the user access to list of transformers
    And the user select a transformer type "EFLUID_REGION_SUPPORT" to add
    Then the provided template is transformer definition edit
    And the transformer attachment comment is "7 lignes de TRECOPIEPARAMREFERENTIELDIR seront prises en compte dans le lot pour la regionalisation"

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
    Given the configured transformers for project "Default" :
      | name          | type                  | priority | configuration                          |
      | Transformer 1 | EFLUID_REGION_SUPPORT | 1        | {"tablePattern":".*","project":"test"} |
    And a new commit "init for region support" has been saved with all the new identified diff content
    And the user has requested an export of the commit with name "init for region support" and this customization for transformer "Transformer 1" :
      """json
      {
        "tablePattern" : ".*",
        "project": "test"
      }
      """
    And the user accesses to the destination environment with the same dictionary
    And no existing data in managed table "TTEST1" in destination environment
    And no existing data in managed table "TTEST2" in destination environment
    And the existing data in secondary parameter table "TAPPLICATIONINFO" in destination environment :
      | projet | site |
      | test   | regA |
    And a merge diff analysis has been started and completed with the available source package
    When the user access to merge commit page
    Then the merge commit content is rendered with these identified changes :
      | Table  | Key     | Action | Need Resolve | Payload       |
      | TTEST1 | $1-regA | ADD    | true         | COL1:'line 1' |
      | TTEST1 | $5-regA | ADD    | true         | COL1:'line 5' |
      | TTEST2 | $3-regA | ADD    | true         | COL1:'line 3' |
      | TTEST2 | $4-regA | ADD    | true         | COL1:'line 4' |

  Scenario Outline: The data processed on merge in destination environment is selected regarding the region specified in transformer - various init cases
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
    Given the configured transformers for project "Default" :
      | name          | type                  | priority | configuration                          |
      | Transformer 1 | EFLUID_REGION_SUPPORT | 1        | {"tablePattern":".*","project":"test"} |
    And a new commit "init for region support" has been saved with all the new identified diff content
    And the user has requested an export of the commit with name "init for region support" and this customization for transformer "Transformer 1" :
      """json
      {
        "tablePattern" : "<table pattern>",
        "project": "<projet>"
      }
      """
    And the user accesses to the destination environment with the same dictionary
    And no existing data in managed table "TTEST1" in destination environment
    And no existing data in managed table "TTEST2" in destination environment
    And the existing data in secondary parameter table "TAPPLICATIONINFO" in destination environment :
      | projet | site                |
      | test   | <region code test>  |
      | other  | <region code other> |
    And a merge diff analysis has been started and completed with the available source package
    When the user access to merge commit page
    Then the merge commit content is rendered with changes for these lines : "<identified changes>"
    Examples:
      | table pattern | projet | region code test | region code other | identified changes                                                                                                     |
      | .*            | test   | regA             | regB              | ADD TTEST1:$1-regA, ADD TTEST1:$5-regA, ADD TTEST2:$3-regA, ADD TTEST2:$4-regA                                         |
      | .*            | test   | regB             | regA              | ADD TTEST1:$2-regB, ADD TTEST1:$3-regB, ADD TTEST2:$1-regB                                                             |
      | .*            | other  | regB             | regC              | ADD TTEST1:$4-regC, ADD TTEST2:$2-regC                                                                                 |
      | TTEST1        | test   | regA             | regB              | ADD TTEST1:$1-regA, ADD TTEST1:$5-regA, ADD TTEST2:$1-regB, ADD TTEST2:$2-regC, ADD TTEST2:$3-regA, ADD TTEST2:$4-regA |
      | TTEST1        | other  | regC             | regA              | ADD TTEST1:$1-regA, ADD TTEST1:$5-regA, ADD TTEST2:$1-regB, ADD TTEST2:$2-regC, ADD TTEST2:$3-regA, ADD TTEST2:$4-regA |
      | TTEST2        | other  | regB             | regC              | ADD TTEST1:$1-regA, ADD TTEST1:$2-regB, ADD TTEST1:$3-regB, ADD TTEST1:$4-regC, ADD TTEST1:$5-regA, ADD TTEST2:$2-regC |

  Scenario Outline: The data processed on merge in destination environment is selected regarding the region specified in transformer - various edit cases
    Given the test is an Efluid standard scenario
    And the existing data in managed table "TTEST1" :
      | id | col1             |
      | $1 | line 1           |
      | $2 | line 2           |
      | $3 | line 3           |
      | $4 | line 4           |
      | $6 | line 6 different |
    And the existing data in secondary parameter table "TRECOPIEPARAMREFERENTIELDIR" :
      | dir  | tabname | op  | colsPk | srcId1 | srcId2 | srcId3 | srcId4 | srcId5 |
      | regA | TTEST1  | INS | + ID   | $1     |        |        |        |        |
      | regA | TTEST1  | INS | + ID   | $4     |        |        |        |        |
      | regB | TTEST1  | INS | + ID   | $2     |        |        |        |        |
      | regB | TTEST1  | INS | + ID   | $3     |        |        |        |        |
      | regB | TTEST1  | INS | + ID   | $5     |        |        |        |        |
      | regC | TTEST1  | INS | + ID   | $6     |        |        |        |        |
    Given the configured transformers for project "Default" :
      | name          | type                  | priority | configuration                          |
      | Transformer 1 | EFLUID_REGION_SUPPORT | 1        | {"tablePattern":".*","project":"test"} |
    And a new commit "init for region support" has been saved with all the new identified diff content
    And these changes are applied to table "TTEST1" :
      | change | id | col1   |
      | delete | $2 |        |
      | add    | $5 | line 5 |
    And a new commit "update for region support" has been saved with all the new identified diff content
    And the user has requested an export starting by the commit with name "init for region support"
    And the user accesses to the destination environment with the same dictionary
    And the existing data in managed table "TTEST1" in destination environment :
      | id | col1   |
      | $4 | line 4 |
      | $6 | line 6 |
    And a commit ":construction: Destination commit initial" has been saved with all the new identified diff content in destination environment
    And the existing data in secondary parameter table "TAPPLICATIONINFO" in destination environment :
      | projet | site          |
      | test   | <region code> |
    And a merge diff analysis has been started and completed with the available source package
    When the user access to merge commit page
    Then the merge commit content is rendered with changes for these lines : "<identified changes>"
    Examples:
      | region code | identified changes           |
      | regA        | ADD TTEST1:$1                |
      | regB        | ADD TTEST1:$3, ADD TTEST1:$5 |
      | regC        | UPDATE TTEST1:$6             |

  Scenario: Even for missing regions on transformer source, if the table is specified in region source then the lines are excluded
    Given the test is an Efluid standard scenario
    And the existing data in managed table "TTEST1" :
      | id | col1   |
      | $1 | line 1 |
      | $2 | line 2 |
      | $3 | line 3 |
    And the existing data in secondary parameter table "TRECOPIEPARAMREFERENTIELDIR" :
      | dir  | tabname | op  | colsPk | srcId1 | srcId2 | srcId3 | srcId4 | srcId5 |
      | regA | TTEST1  | INS | + ID   | $1     |        |        |        |        |
      | regB | TTEST1  | INS | + ID   | $2     |        |        |        |        |
      | regB | TTEST1  | INS | + ID   | $3     |        |        |        |        |
    Given the configured transformers for project "Default" :
      | name          | type                  | priority | configuration                          |
      | Transformer 1 | EFLUID_REGION_SUPPORT | 1        | {"tablePattern":".*","project":"test"} |
    And a new commit "init for region support" has been saved with all the new identified diff content
    And the user has requested an export starting by the commit with name "init for region support"
    And the user accesses to the destination environment with the same dictionary
    And no existing data in managed table "TTEST1" in destination environment
    And the existing data in secondary parameter table "TAPPLICATIONINFO" in destination environment :
      | projet | site |
      | test   | regC |
    And a merge diff analysis has been started and completed with the available source package
    When the user access to merge commit page
    Then no merge content has been identified

  Scenario: For matching regions on transformer source, if the table is not specified in region source then the lines are not processed by transformer
    Given the test is an Efluid standard scenario
    And the existing data in managed table "TTEST1" :
      | id | col1   |
      | $1 | line 1 |
      | $2 | line 2 |
      | $3 | line 3 |
    And the existing data in secondary parameter table "TRECOPIEPARAMREFERENTIELDIR" :
      | dir  | tabname | op  | colsPk | srcId1 | srcId2 | srcId3 | srcId4 | srcId5 |
      | regA | TTEST2  | INS | + ID   | $1     |        |        |        |        |
      | regB | TTEST2  | INS | + ID   | $2     |        |        |        |        |
      | regB | TTEST2  | INS | + ID   | $3     |        |        |        |        |
    Given the configured transformers for project "Default" :
      | name          | type                  | priority | configuration                          |
      | Transformer 1 | EFLUID_REGION_SUPPORT | 1        | {"tablePattern":".*","project":"test"} |
    And a new commit "init for region support" has been saved with all the new identified diff content
    And the user has requested an export starting by the commit with name "init for region support"
    And the user accesses to the destination environment with the same dictionary
    And no existing data in managed table "TTEST1" in destination environment
    And the existing data in secondary parameter table "TAPPLICATIONINFO" in destination environment :
      | projet | site |
      | test   | regA |
    And a merge diff analysis has been started and completed with the available source package
    When the user access to merge commit page
    Then the merge commit content is rendered with these identified changes :
      | Table  | Key | Action | Need Resolve | Payload       |
      | TTEST1 | $1  | ADD    | true         | COL1:'line 1' |
      | TTEST1 | $2  | ADD    | true         | COL1:'line 2' |
      | TTEST1 | $3  | ADD    | true         | COL1:'line 3' |