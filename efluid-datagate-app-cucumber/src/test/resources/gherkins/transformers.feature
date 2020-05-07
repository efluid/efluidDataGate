Feature: Some transformers can be set for a project to adapt commits at import regarding various rules

  Scenario: The list of configured transformers for a project is available to all users
    Given from the home page
    When the user access to list of transformers
    Then the provided template is list of transformers

  Scenario: The existing transformers from project are listed
    Given the configured transformers for project "Default" :
      | name                  | type                  | priority | configuration                                                   |
      | My Test Transformer 1 | UPPERCASE_TRANSFORMER | 1        | {"tablePattern" : "T_UP.*","columnNames" : [ ".*" ]}            |
      | My Test Transformer 2 | UPPERCASE_TRANSFORMER | 10       | {"tablePattern" : "T_ONE","columnNames" : [ "COL_A", "COL_B" ]} |
      | My Test Transformer 3 | EFLUID_AUDIT          | 5        | {"tablePattern" : "T_TWO","columnNames" : [ "COL_.*" ]}         |
    When the user access to list of transformers
    Then the 3 configured transformers from project "Default" are displayed

  Scenario: The available types of transformers are listed
    Given from the home page
    When the user access to list of transformers
    Then the available transformer types are :
      | name                  | type                       |
      | LOWERCASE_TRANSFORMER | LowerCaseTransformer       |
      | UPPERCASE_TRANSFORMER | UpperCaseTransformer       |
      | EFLUID_AUDIT          | EfluidAuditDataTransformer |

  Scenario: A transformer can be initialized for a specified type with a default configuration
    Given from the home page
    When the user access to list of transformers
    And the user select a transformer type "UPPERCASE_TRANSFORMER" to add
    Then the provided template is transformer definition edit
    And the transformer definition configuration is :
      """json
      {
        "tablePattern" : ".*",
        "columnNames" : [ ".*","COL_.*","COL_C" ]
      }
      """

  Scenario: The transformer configuration cannot be saved if not valid for transformer rules
    Given the user has initialized a new transformer of type "UPPERCASE_TRANSFORMER", with name "Test" and this configuration :
      """json
      {
        "tablePattern" : ".*"
      }
      """
    When the user try to save the transformer
    Then an error is provided with this message :
      """txt
      At least one column name must be specified. Use ".*" as default to match all
      """

  Scenario: The transformer configuration can be saved if valid for transformer rules
    Given the user has initialized a new transformer of type "UPPERCASE_TRANSFORMER", with name "Test" and this configuration :
      """json
      {
        "tablePattern" : ".*",
        "columnNames" : [ ".*","COL_.*","COL_C" ]
      }
      """
    When the user try to save the transformer
    Then the transformer with name "Test" of type "UPPERCASE_TRANSFORMER" exists

  Scenario Outline: The transformer configuration validation rules depends on transformer type - UPPERCASE_TRANSFORMER
    Given the user has initialized a new transformer of type "UPPERCASE_TRANSFORMER", with name "<name>" and this configuration :
      """json
      <configuration>
      """
    When the user save the transformer
    Then the result "<result>" is provided
    Examples:
      | name   | configuration                                               | result                                      |
      | upper1 | {"tablePattern":".*","columnNames":[".*","COL_.*","COL_C"]} | SUCCESS                                     |
      | upper2 | {"tablePattern":".*"}                                       | At least one column name must be specified. |
      | upper3 | {"columnNames":[".*","COL_.*","COL_C"]}                     | tablePattern cannot be empty or missing.    |

  Scenario Outline: The transformer configuration validation rules depends on transformer type - EFLUID_AUDIT
    Given the user has initialized a new transformer of type "EFLUID_AUDIT", with name "<name>" and this configuration :
      """json
      <configuration>
      """
    When the user save the transformer
    Then the result "<result>" is provided
    Examples:
      | name    | configuration                                                                                                                          | result                                                           |
      | audit1  | {"tablePattern":".*","appliedKeyPatterns":[".*"],"dateUpdates":{},"actorUpdates":{"ACT_C":"bob"}}                                      | SUCCESS                                                          |
      | audit2  | {"tablePattern":".*","appliedKeyPatterns":[".*"],"dateUpdates":{"DATE_C":"current_date"},"actorUpdates":{"ACT_C":"bob"}}               | SUCCESS                                                          |
      | audit3  | {"tablePattern":".*","appliedKeyPatterns":["12.*"],"dateUpdates":{"DATE_C":"2019-12-01"},"actorUpdates":{".*":"bob"}}                  | SUCCESS                                                          |
      | audit4  | {"tablePattern":".*","appliedValueFilterPatterns":{"ETAT":"0"},"appliedKeyPatterns":[".*"],"dateUpdates":{},"actorUpdates":{".*":"T"}} | SUCCESS                                                          |
      | audit5  | {"tablePattern":".*","dateUpdates":{},"actorUpdates":{"ACT_C":"bob"}}                                                                  | At least one key value pattern must be specified.                |
      | audit6  | {"tablePattern":".*","appliedKeyPatterns":[".*"],"dateUpdates":{},"actorUpdates":{}}                                                   | At least one update on date or actor must be specified.          |
      | audit7  | {"tablePattern":".*","appliedKeyPatterns":[".*"],"dateUpdates":{"":"current_date"},"actorUpdates":{}}                                  | A date update column name cannot be empty.                       |
      | audit8  | {"tablePattern":".*","appliedKeyPatterns":[".*"],"dateUpdates":{},"actorUpdates":{"":"bob"}}                                           | An actor update column name cannot be empty.                     |
      | audit9  | {"tablePattern":".*","appliedKeyPatterns":[".*"],"dateUpdates":{"DATE_C":""},"actorUpdates":{}}                                        | A date update value cannot be empty.                             |
      | audit10 | {"tablePattern":".*","appliedKeyPatterns":[".*"],"dateUpdates":{"DATE_C":"something"},"actorUpdates":{}}                               | A date update value must be "current_date" or a fixed date value |
      | audit11 | {"dateUpdates":{"DATE_C":"current_date"},"appliedKeyPatterns":[".*"],"actorUpdates":{}}                                                | tablePattern cannot be empty or missing.                         |
      | audit12 | {"tablePattern":".*","appliedKeyPatterns":[".*"],"dateUpdates":{},"actorUpdates":{},}                                                  | JSON Parsing error                                               |
      | audit13 | {"tablePattern":".*","appliedValueFilterPatterns":{"":"0"},"appliedKeyPatterns":[".*"],"dateUpdates":{},"actorUpdates":{".*":""}}      | Value filter column name cannot be empty.                        |

  Scenario: The transformer configuration for a project is exported with commits
    Given the configured transformers for project "Default" :
      | name                  | type                  | priority | configuration                                                   |
      | My Test Transformer 1 | UPPERCASE_TRANSFORMER | 1        | {"tablePattern" : "T_UP.*","columnNames" : [ ".*" ]}            |
      | My Test Transformer 2 | UPPERCASE_TRANSFORMER | 10       | {"tablePattern" : "T_ONE","columnNames" : [ "COL_A", "COL_B" ]} |
      | My Test Transformer 3 | EFLUID_AUDIT          | 5        | {"tablePattern":".*","appliedKeyPatterns":[".*"],"dateUpdates":{},"actorUpdates":{"ACT_C":"bob"}}         |
    And the existing data in managed table "TTAB_ONE" :
      | key | value | preset   | something |
      | 1   | AAA   | Preset 1 | AAA       |
      | 2   | BBB   | Preset 2 | BBB       |
      | 3   | CCC   | Preset 3 | CCC       |
    And a new commit ":construction: Update 1" has been saved with all the new identified diff content
    And these changes are applied to table "TTAB_TWO" :
      | change | key | value | other     |
      | add    | AAA | One   | Other JJJ |
      | add    | BBB | Two   | Other KKK |
    And a new commit ":construction: Update 2" has been saved with all the new identified diff content
    When the user request an export of all the commits
    Then the export package contains 2 commit contents
    And the export package content has these transformer definitions :
      | name                  | type                  | priority | configuration                                                   |
      | My Test Transformer 1 | UPPERCASE_TRANSFORMER | 1        | {"tablePattern" : "T_UP.*","columnNames" : [ ".*" ]}            |
      | My Test Transformer 2 | UPPERCASE_TRANSFORMER | 10       | {"tablePattern" : "T_ONE","columnNames" : [ "COL_A", "COL_B" ]} |
      | My Test Transformer 3 | EFLUID_AUDIT          | 5        | {"tablePattern":".*","appliedKeyPatterns":[".*"],"dateUpdates":{},"actorUpdates":{"ACT_C":"bob"}}         |

  Scenario: The transformer configuration for a project can be customized at export
    Given the configured transformers for project "Default" :
      | name                  | type                  | priority | configuration                                                   |
      | My Test Transformer 1 | UPPERCASE_TRANSFORMER | 1        | {"tablePattern" : "T_UP.*","columnNames" : [ ".*" ]}            |
      | My Test Transformer 2 | UPPERCASE_TRANSFORMER | 10       | {"tablePattern" : "T_ONE","columnNames" : [ "COL_A", "COL_B" ]} |
      | My Test Transformer 3 | EFLUID_AUDIT          | 5        | {"tablePattern":".*","appliedKeyPatterns":[".*"],"dateUpdates":{},"actorUpdates":{"ACT_C":"bob"}}         |
    And the existing data in managed table "TTAB_ONE" :
      | key | value | preset   | something |
      | 1   | AAA   | Preset 1 | AAA       |
      | 2   | BBB   | Preset 2 | BBB       |
      | 3   | CCC   | Preset 3 | CCC       |
    And a new commit ":construction: Update 1" has been saved with all the new identified diff content
    And these changes are applied to table "TTAB_TWO" :
      | change | key | value | other     |
      | add    | AAA | One   | Other JJJ |
      | add    | BBB | Two   | Other KKK |
    And a new commit ":construction: Update 2" has been saved with all the new identified diff content
    When the user request to prepare an export of the commit with name ":construction: Update 1"
    And the user customize the transformer "My Test Transformer 2" with this configuration :
      """json
      {
        "tablePattern" : "T_ONE",
        "columnNames" : [ "COL_D","COL_C" ]
      }
      """
    And the user validate the prepared export
    Then the export download start automatically
    And the export package content has these transformer definitions :
      | name                  | type                  | priority | configuration                                                  |
      | My Test Transformer 1 | UPPERCASE_TRANSFORMER | 1        | {"tablePattern" : "T_UP.*","columnNames" : [ ".*" ]}           |
      | My Test Transformer 2 | UPPERCASE_TRANSFORMER | 10       | {"tablePattern" : "T_ONE","columnNames" : [ "COL_D","COL_C" ]} |
      | My Test Transformer 3 | EFLUID_AUDIT          | 5        | {"tablePattern":".*","appliedKeyPatterns":[".*"],"dateUpdates":{},"actorUpdates":{"ACT_C":"bob"}}        |

  Scenario: The transformer configuration for a project can be disabled at export
    Given the configured transformers for project "Default" :
      | name                  | type                  | priority | configuration                                                   |
      | My Test Transformer 1 | UPPERCASE_TRANSFORMER | 1        | {"tablePattern" : "T_UP.*","columnNames" : [ ".*" ]}            |
      | My Test Transformer 2 | UPPERCASE_TRANSFORMER | 10       | {"tablePattern" : "T_ONE","columnNames" : [ "COL_A", "COL_B" ]} |
      | My Test Transformer 3 | EFLUID_AUDIT          | 5        | {"tablePattern":".*","appliedKeyPatterns":[".*"],"dateUpdates":{},"actorUpdates":{"ACT_C":"bob"}}         |
    And the existing data in managed table "TTAB_ONE" :
      | key | value | preset   | something |
      | 1   | AAA   | Preset 1 | AAA       |
      | 2   | BBB   | Preset 2 | BBB       |
      | 3   | CCC   | Preset 3 | CCC       |
    And a new commit ":construction: Update 1" has been saved with all the new identified diff content
    And these changes are applied to table "TTAB_TWO" :
      | change | key | value | other     |
      | add    | AAA | One   | Other JJJ |
      | add    | BBB | Two   | Other KKK |
    And a new commit ":construction: Update 2" has been saved with all the new identified diff content
    When the user request to prepare an export of the commit with name ":construction: Update 1"
    And the user disable the transformer "My Test Transformer 2"
    And the user validate the prepared export
    Then the export download start automatically
    And the export package content has these transformer definitions :
      | name                  | type                  | priority | configuration                                                  |
      | My Test Transformer 1 | UPPERCASE_TRANSFORMER | 1        | {"tablePattern" : "T_UP.*","columnNames" : [ ".*" ]}           |
      | My Test Transformer 3 | EFLUID_AUDIT          | 5        | {"tablePattern":".*","appliedKeyPatterns":[".*"],"dateUpdates":{},"actorUpdates":{"ACT_C":"bob"}}        |

  Scenario: The data processed on merge in destination environment is transformed regarding the transformer configuration
    Given the configured transformers for project "Default" :
      | name          | type                  | priority | configuration                                    |
      | Transformer 1 | UPPERCASE_TRANSFORMER | 1        | {"tablePattern" : ".*","columnNames" : [ ".*" ]} |
    And the existing data in managed table "TTAB_ONE" :
      | key | value | preset           | something |
      | 1   | AAA   | Preset 1         | aaa       |
      | 2   | BBB   | Preset lowercase | bbb       |
      | 3   | CCC   | Preset 3         | ccc       |
    And the existing data in managed table "TTAB_TWO" :
      | key | value | other     |
      | AAA | One   | Other JJJ |
      | BBB | Two   | Other KKK |
    And a new commit ":construction: Update 1" has been saved with all the new identified diff content
    And the user has requested an export of the commit with name ":construction: Update 1"
    And the user accesses to the destination environment with the same dictionary
    And no existing data in managed table "TTAB_ONE" in destination environment
    And no existing data in managed table "TTAB_TWO" in destination environment
    And a merge diff analysis has been started and completed with the available source package
    When the user access to merge commit page
    Then the merge commit content is rendered with these identified changes :
      | Table    | Key | Action | Need Resolve | Payload                                    |
      | TTAB_ONE | AAA | ADD    | true         | PRESET:'PRESET 1', SOMETHING:'AAA'         |
      | TTAB_ONE | BBB | ADD    | true         | PRESET:'PRESET LOWERCASE', SOMETHING:'BBB' |
      | TTAB_ONE | CCC | ADD    | true         | PRESET:'PRESET 3', SOMETHING:'CCC'         |
      | TTAB_TWO | AAA | ADD    | true         | VALUE:'ONE', OTHER:'OTHER JJJ'             |
      | TTAB_TWO | BBB | ADD    | true         | VALUE:'TWO', OTHER:'OTHER KKK'             |

  Scenario: The data processed on merge in destination environment is transformed regarding the customized transformer configuration
    Given the configured transformers for project "Default" :
      | name          | type                  | priority | configuration                                    |
      | Transformer 1 | UPPERCASE_TRANSFORMER | 1        | {"tablePattern" : ".*","columnNames" : [ ".*" ]} |
    And the existing data in managed table "TTAB_ONE" :
      | key | value | preset           | something |
      | 1   | AAA   | Preset 1         | aaa       |
      | 2   | BBB   | Preset lowercase | bbb       |
      | 3   | CCC   | Preset 3         | ccc       |
    And the existing data in managed table "TTAB_TWO" :
      | key | value | other     |
      | AAA | One   | Other JJJ |
      | BBB | Two   | Other KKK |
    And a new commit ":construction: Update 1" has been saved with all the new identified diff content
    And the user has requested an export of the commit with name ":construction: Update 1" and this customization for transformer "Transformer 1" :
      """json
      {
        "tablePattern" : "TTAB_ONE",
        "columnNames" : [ "PRESET" ]
      }
      """
    And the user accesses to the destination environment with the same dictionary
    And no existing data in managed table "TTAB_ONE" in destination environment
    And no existing data in managed table "TTAB_TWO" in destination environment
    And a merge diff analysis has been started and completed with the available source package
    When the user access to merge commit page
    Then the merge commit content is rendered with these identified changes :
      | Table    | Key | Action | Need Resolve | Payload                                    |
      | TTAB_ONE | AAA | ADD    | true         | PRESET:'PRESET 1', SOMETHING:'aaa'         |
      | TTAB_ONE | BBB | ADD    | true         | PRESET:'PRESET LOWERCASE', SOMETHING:'bbb' |
      | TTAB_ONE | CCC | ADD    | true         | PRESET:'PRESET 3', SOMETHING:'ccc'         |
      | TTAB_TWO | AAA | ADD    | true         | VALUE:'One', OTHER:'Other JJJ'             |
      | TTAB_TWO | BBB | ADD    | true         | VALUE:'Two', OTHER:'Other KKK'             |

  Scenario: The data processed on merge in destination environment is transformed regarding transformer priorities
    Given the configured transformers for project "Default" :
      | name          | type                  | priority | configuration                                              |
      | Transformer 1 | UPPERCASE_TRANSFORMER | 1        | {"tablePattern" : "TTAB_ONE","columnNames" : [ ".*" ]}     |
      | Transformer 2 | LOWERCASE_TRANSFORMER | 5        | {"tablePattern" : "TTAB_ONE","columnNames" : [ "PRESET" ]} |
      | Transformer 3 | UPPERCASE_TRANSFORMER | 10       | {"tablePattern" : "TTAB_TWO","columnNames" : [ ".*" ]}     |
      | Transformer 4 | LOWERCASE_TRANSFORMER | 5        | {"tablePattern" : "TTAB_TWO","columnNames" : [ "VALUE" ]}  |
    And the existing data in managed table "TTAB_ONE" :
      | key | value | preset           | something |
      | 1   | AAA   | Preset 1         | AbCdE     |
      | 2   | BBB   | Preset lowercase | BcDeF     |
    And the existing data in managed table "TTAB_TWO" :
      | key | value | other     |
      | AAA | One   | Other JJJ |
      | BBB | Two   | Other KKK |
    And a new commit ":construction: Update 1" has been saved with all the new identified diff content
    And the user has requested an export of the commit with name ":construction: Update 1"
    And the user accesses to the destination environment with the same dictionary
    And no existing data in managed table "TTAB_ONE" in destination environment
    And no existing data in managed table "TTAB_TWO" in destination environment
    And a merge diff analysis has been started and completed with the available source package
    When the user access to merge commit page
    Then the merge commit content is rendered with these identified changes :
      | Table    | Key | Action | Need Resolve | Payload                                      |
      | TTAB_ONE | AAA | ADD    | true         | PRESET:'PRESET 1', SOMETHING:'ABCDE'         |
      | TTAB_ONE | BBB | ADD    | true         | PRESET:'PRESET LOWERCASE', SOMETHING:'BCDEF' |
      | TTAB_TWO | AAA | ADD    | true         | VALUE:'one', OTHER:'OTHER JJJ'               |
      | TTAB_TWO | BBB | ADD    | true         | VALUE:'two', OTHER:'OTHER KKK'               |