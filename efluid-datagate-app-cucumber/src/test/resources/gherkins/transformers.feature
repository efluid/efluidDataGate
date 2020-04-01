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

  Scenario: A transformer can be added for a specified type
    Given the existing versions "v1, v2"
    When the user add new version "v3"
    Then the 3 updated versions are displayed
