Feature: Performance for diff generation and merge generation are controlled

  Scenario: A diff process with large data follow specification references - small size
    Given the test is a performance standard scenario for variation "reference"
    And the 5000 generated data in managed table "TTESTMULTIDATATYPE" :
      | id         | col1     | col2     | col3 | col4       | col5                | col6 | col7    |
      | $testj_d%% | testj %% | testj %% | 12%% | 2012-01-15 | 2012-01-15 00:00:00 | y    | clob %% |
    And a diff analysis can be started and completed
    And a diff has already been launched
    And the diff is completed
    Then the test process reference values are logged in "./perf-spec-results.csv"

  Scenario: A diff process with large data follow specification references - medium size
    Given the test is a performance standard scenario for variation "reference"
    And the 50000 generated data in managed table "TTESTMULTIDATATYPE" :
      | id         | col1     | col2     | col3 | col4       | col5                | col6 | col7    |
      | $testj_d%% | testj %% | testj %% | 12%% | 2012-01-15 | 2012-01-15 00:00:00 | y    | clob %% |
    And a diff analysis can be started and completed
    And a diff has already been launched
    And the diff is completed
    Then the test process reference values are logged in "./perf-spec-results.csv"

  Scenario: A diff process with large data follow specification references - large size
    Given the test is a performance standard scenario for variation "reference"
    And the 500000 generated data in managed table "TTESTMULTIDATATYPE" :
      | id         | col1     | col2     | col3 | col4       | col5                | col6 | col7    |
      | $testj_d%% | testj %% | testj %% | 12%% | 2012-01-15 | 2012-01-15 00:00:00 | y    | clob %% |
    And a diff analysis can be started and completed
    And a diff has already been launched
    And the diff is completed
    Then the test process reference values are logged in "./perf-spec-results.csv"