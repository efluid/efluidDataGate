Feature: The backlog can be imported and merged with local changes

  The backlog content (commit with indexes, lob content and attachments) can be imported, and mixed (merged)
  with the changes from the locale instance using fixed merging rules

  Background:
    Given the existing data in managed table "TTAB_TWO" :
      | key | value | other     |
      | JJJ | One   | Other JJJ |
      | KKK | Two   | Other KKK |
      | VVV | 333   | Other VVV |
      | III | 444   | Other III |
    And the existing data in managed table "TTAB_FIVE" :
      | key | data                | simple |
      | 1   | ABCDEF1234567ABDDDD | 17.81  |
      | 2   | ABCDEF1234567ABEEEE | 17.82  |
      | 3   | ABCDEF1234567ABFFFF | 17.83  |

  Scenario: Commits are prepared in a destination environment before importing a package - merge init state
    Given the commit ":tada: Test commit init source" has been saved and exported with all the identified initial diff content
    And the user accesses to the destination environment with the same dictionary
    And the existing data in managed table "TTAB_TWO" in destination environment :
      | key | value | other     |
      | VVV | 333   | Other VVV |
      | III | 444   | Other III |
    And a commit ":construction: Destination commit 1" has been saved with all the new identified diff content in destination environment
    When the user do nothing more
    Then the commit ":construction: Destination commit 1" is added to commit list for current project in destination environment
    And the exported commit ":tada: Test commit init source" is not present in the destination environment

  Scenario: The dedicated merge diff with changes from destination environment is started when importing a package
    Given the commit ":tada: Test commit init source" has been saved and exported with all the identified initial diff content
    And the user accesses to the destination environment with the same dictionary
    And the existing data in managed table "TTAB_TWO" in destination environment :
      | key | value | other     |
      | VVV | 333   | Other VVV |
      | III | 444   | Other III |
    And a commit ":construction: Destination commit 1" has been saved with all the new identified diff content in destination environment
    When the user import the available source package
    Then a merge diff is running

  Scenario: The dedicated merge diff provides details on differences between imported package and local content - simple add content
    Given the commit ":tada: Test commit init source" has been saved and exported with all the identified initial diff content
    And the user accesses to the destination environment with the same dictionary
    And the existing data in managed table "TTAB_TWO" in destination environment :
      | key | value | other     |
      | VVV | 333   | Other VVV |
      | III | 444   | Other III |
    And a commit ":construction: Destination commit 1" has been saved with all the new identified diff content in destination environment
    And a merge diff has already been launched with the available source package
    And the merge diff is completed
    When the user access to diff commit page
    Then the merge commit content is rendered with these identified changes :
      | Table     | Key | Action | Payload                                                                                                        |
      | TTAB_TWO  | JJJ | ADD    | VALUE:'One', OTHER:'Other JJJ'                                                                                 |
      | TTAB_TWO  | KKK | ADD    | VALUE:'Two', OTHER:'Other KKK'                                                                                 |
      | TTAB_FIVE | 1   | ADD    | DATA:<a href="/lob/ZVs3L0PiRT7vzgYlGCrAmrqVm643dW1ZwshZNTNmEBc%3D" download="download">LOB</a>, SIMPLE:17.81   |
      | TTAB_FIVE | 2   | ADD    | DATA:<a href="/lob/MDOnerGg0ikFARKvihX0fFD8V2mUp4%2BKHfrji2ByPKE%3D" download="download">LOB</a>, SIMPLE:17.82 |
      | TTAB_FIVE | 3   | ADD    | DATA:<a href="/lob/mGb4npkQbRvRJrJWp%2FQIpwGPqZTFkKhI1FU9l9jNj1M%3D" download="download">LOB</a>, SIMPLE:17.83 |

  Scenario: The dedicated merge diff content can be selected to prepare a merge commit
    Given the commit ":tada: Test commit init source" has been saved and exported with all the identified initial diff content
    And the user accesses to the destination environment with the same dictionary
    And the existing data in managed table "TTAB_TWO" in destination environment :
      | key | value | other     |
      | VVV | 333   | Other VVV |
      | III | 444   | Other III |
    And a commit ":construction: Destination commit 1" has been saved with all the new identified diff content in destination environment
    And a merge diff analysis has been started and completed with the available source package
    When the user select all prepared diff content for merge commit
    And the user accesses to preparation commit page
    Then all the diff preparation content is selected for commit
    And the commit type is "MERGED"

  Scenario: The selected merge diff content can be saved as a new merge commit
    Given the commit ":tada: Test commit init source" has been saved and exported with all the identified initial diff content
    And the user accesses to the destination environment with the same dictionary
    And the existing data in managed table "TTAB_TWO" in destination environment :
      | key | value | other     |
      | VVV | 333   | Other VVV |
      | III | 444   | Other III |
    And a commit ":construction: Destination commit 1" has been saved with all the new identified diff content in destination environment
    And a merge diff analysis has been started and completed with the available source package
    And the user has selected all content for merge commit
    And the user has specified a commit comment ":construction: merge commit test"
    When the user save the commit
    Then the commit ":construction: merge commit test" is added to commit list for current project
    And the commit ":construction: merge commit test" from current project is of type "MERGED"

  Scenario: The selected merge diff content updates are applied to destination table - simple add content
    Given the commit ":tada: Test commit init source" has been saved and exported with all the identified initial diff content
    And the user accesses to the destination environment with the same dictionary
    And the existing data in managed table "TTAB_TWO" in destination environment :
      | key | value | other     |
      | VVV | 333   | Other VVV |
      | III | 444   | Other III |
    And no existing data in managed table "TTAB_FIVE" in destination environment
    And a commit ":construction: Destination commit initial" has been saved with all the new identified diff content in destination environment
    And a merge diff analysis has been started and completed with the available source package
    And the user has selected all content for merge commit
    And the user has specified a commit comment ":construction: merge commit test with changes"
    When the user save the merge commit
    Then the saved merge commit content has these identified changes :
      | Table     | Key | Action | Payload                                                                                                        |
      | TTAB_TWO  | JJJ | ADD    | VALUE:'One', OTHER:'Other JJJ'                                                                                 |
      | TTAB_TWO  | KKK | ADD    | VALUE:'Two', OTHER:'Other KKK'                                                                                 |
      | TTAB_FIVE | 1   | ADD    | DATA:<a href="/lob/ZVs3L0PiRT7vzgYlGCrAmrqVm643dW1ZwshZNTNmEBc%3D" download="download">LOB</a>, SIMPLE:17.81   |
      | TTAB_FIVE | 2   | ADD    | DATA:<a href="/lob/MDOnerGg0ikFARKvihX0fFD8V2mUp4%2BKHfrji2ByPKE%3D" download="download">LOB</a>, SIMPLE:17.82 |
      | TTAB_FIVE | 3   | ADD    | DATA:<a href="/lob/mGb4npkQbRvRJrJWp%2FQIpwGPqZTFkKhI1FU9l9jNj1M%3D" download="download">LOB</a>, SIMPLE:17.83 |
    And the data in managed table "TTAB_TWO" in destination environment is now :
      | key | value | other     |
      | JJJ | One   | Other JJJ |
      | KKK | Two   | Other KKK |
      | VVV | 333   | Other VVV |
      | III | 444   | Other III |
    And the data in managed table "TTAB_FIVE" in destination environment is now :
      | key | data                | simple |
      | 1   | ABCDEF1234567ABDDDD | 17.81  |
      | 2   | ABCDEF1234567ABEEEE | 17.82  |
      | 3   | ABCDEF1234567ABFFFF | 17.83  |