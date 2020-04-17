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

  Scenario Outline: The transformer configuration validation rules depends on transformer type
    Given the user has initialized a new transformer of type "<type>", with name "<name>" and this configuration :
      """json
      <configuration>
      """
    When the user save the transformer
    Then the result "<result>" is provided
    Examples:
      | name            | type                  | configuration                                                | result                                      |
      | test1_uppercase | UPPERCASE_TRANSFORMER | {"tablePattern":".*","columnNames":[".*","COL_.*","COL_C"]}  | SUCCESS                                     |
      | test2_uppercase | UPPERCASE_TRANSFORMER | {"tablePattern":".*"}                                        | At least one column name must be specified. |
      | test3_uppercase | UPPERCASE_TRANSFORMER | {"columnNames":[".*","COL_.*","COL_C"]}                      | tablePattern cannot be empty or missing.    |
      | test4_efluid    | EFLUID_AUDIT          | {"tablePattern":".*","columnNames":[".*","COL_.*","COL_C"]}  | SUCCESS                                     |
      | test5_efluid    | EFLUID_AUDIT          | {"tablePattern":".*"}                                        | At least one column name must be specified. |
      | test6_efluid    | EFLUID_AUDIT          | {"columnNames":[".*"]}                                       | tablePattern cannot be empty or missing.    |
      | test7_efluid    | EFLUID_AUDIT          | {"tablePattern":".*","columnNames":[".*","COL_.*","COL_C"],} | JSON Parsing error                          |

  Scenario: The transformer configuration for a project is exported with commits
    Given the configured transformers for project "Default" :
      | name                  | type                  | priority | configuration                                                   |
      | My Test Transformer 1 | UPPERCASE_TRANSFORMER | 1        | {"tablePattern" : "T_UP.*","columnNames" : [ ".*" ]}            |
      | My Test Transformer 2 | UPPERCASE_TRANSFORMER | 10       | {"tablePattern" : "T_ONE","columnNames" : [ "COL_A", "COL_B" ]} |
      | My Test Transformer 3 | EFLUID_AUDIT          | 5        | {"tablePattern" : "T_TWO","columnNames" : [ "COL_.*" ]}         |
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
      | My Test Transformer 3 | EFLUID_AUDIT          | 5        | {"tablePattern" : "T_TWO","columnNames" : [ "COL_.*" ]}         |

  Scenario: The transformer configuration for a project can be customized at export
    Given the configured transformers for project "Default" :
      | name                  | type                  | priority | configuration                                                   |
      | My Test Transformer 1 | UPPERCASE_TRANSFORMER | 1        | {"tablePattern" : "T_UP.*","columnNames" : [ ".*" ]}            |
      | My Test Transformer 2 | UPPERCASE_TRANSFORMER | 10       | {"tablePattern" : "T_ONE","columnNames" : [ "COL_A", "COL_B" ]} |
      | My Test Transformer 3 | EFLUID_AUDIT          | 5        | {"tablePattern" : "T_TWO","columnNames" : [ "COL_.*" ]}         |
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
      | My Test Transformer 3 | EFLUID_AUDIT          | 5        | {"tablePattern" : "T_TWO","columnNames" : [ "COL_.*" ]}        |
