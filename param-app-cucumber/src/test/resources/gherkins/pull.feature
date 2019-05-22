Feature: The backlog can be imported and merged with local changes

  The backlog content (commit with indexes, lob content and attachments) can be imported, and mixed (merged)
  with the changes from the locale instance using fixed merging rules

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
      | KKK | Two   | Other KKK |
    And the existing data in managed table "TTAB_THREE" :
      | key   | value | other   |
      | 11111 | A     | Other A |
      | 22222 | B     | Other B |
      | 33333 | C     | Other C |
    And the existing data in managed table "TTAB_FIVE" :
      | key | data                | simple |
      | 1   | ABCDEF1234567ABDDDD | 17.81  |
      | 2   | ABCDEF1234567ABEEEE | 17.82  |
      | 3   | ABCDEF1234567ABFFFF | 17.83  |

  Scenario: All the existing commits from the backlog can be exported in a single archive
    Given the commit ":tada: Test commit init" has been saved and exported with all the identified initial diff content
    And the user accesses to the destination environment with the same dictionary
    Then the export package contains 1 commit contents
    And the export package content has these identified changes for commit with name ":tada: Test commit init" :
      | Table      | Key | Action | Payload                                                                                                        |
      | TTAB_ONE   | AAA | ADD    | PRESET:'Preset 1', SOMETHING:'AAA'                                                                             |
      | TTAB_ONE   | BBB | ADD    | PRESET:'Preset 2', SOMETHING:'BBB'                                                                             |
      | TTAB_ONE   | CCC | ADD    | PRESET:'Preset 3', SOMETHING:'CCC'                                                                             |
      | TTAB_ONE   | DDD | ADD    | PRESET:'Preset 4', SOMETHING:'DDD'                                                                             |
      | TTAB_ONE   | EEE | ADD    | PRESET:'Preset 5', SOMETHING:'EEE'                                                                             |
      | TTAB_TWO   | JJJ | ADD    | VALUE:'One', OTHER:'Other JJJ'                                                                                 |
      | TTAB_TWO   | KKK | ADD    | VALUE:'Two', OTHER:'Other KKK'                                                                                 |
      | TTAB_THREE | A   | ADD    | OTHER:'Other A'                                                                                                |
      | TTAB_THREE | B   | ADD    | OTHER:'Other B'                                                                                                |
      | TTAB_THREE | C   | ADD    | OTHER:'Other C'                                                                                                |
      | TTAB_FIVE  | 1   | ADD    | DATA:<a href="/lob/ZVs3L0PiRT7vzgYlGCrAmrqVm643dW1ZwshZNTNmEBc%3D" download="download">LOB</a>, SIMPLE:17.81   |
      | TTAB_FIVE  | 2   | ADD    | DATA:<a href="/lob/MDOnerGg0ikFARKvihX0fFD8V2mUp4%2BKHfrji2ByPKE%3D" download="download">LOB</a>, SIMPLE:17.82 |
      | TTAB_FIVE  | 3   | ADD    | DATA:<a href="/lob/mGb4npkQbRvRJrJWp%2FQIpwGPqZTFkKhI1FU9l9jNj1M%3D" download="download">LOB</a>, SIMPLE:17.83 |
    And the export package content has these associated lob data for commit with name ":tada: Test commit init" :
      | data                | hash                                         |
      | ABCDEF1234567ABDDDD | ZVs3L0PiRT7vzgYlGCrAmrqVm643dW1ZwshZNTNmEBc= |
      | ABCDEF1234567ABEEEE | MDOnerGg0ikFARKvihX0fFD8V2mUp4+KHfrji2ByPKE= |
      | ABCDEF1234567ABFFFF | mGb4npkQbRvRJrJWp/QIpwGPqZTFkKhI1FU9l9jNj1M= |

