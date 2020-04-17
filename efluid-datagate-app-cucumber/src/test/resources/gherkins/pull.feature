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
    And the existing data in managed table "TTAB_SIX" :
      | identifier | text                                     | date       |
      | 7          | Ceci est un text enregistré dans un CLOB | 2012-01-15 |
      | 8          | Un autre text CLOB                       | 2005-07-08 |
      | 9          | Encore un autre                          | 2021-12-25 |

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
      | Table     | Key | Action | Need Resolve | Payload                                                                                                                         |
      | TTAB_TWO  | VVV | ADD    | false        | VALUE:'333', OTHER:'Other VVV'                                                                                                  |
      | TTAB_TWO  | III | ADD    | false        | VALUE:'444', OTHER:'Other III'                                                                                                  |
      | TTAB_TWO  | JJJ | ADD    | true         | VALUE:'One', OTHER:'Other JJJ'                                                                                                  |
      | TTAB_TWO  | KKK | ADD    | true         | VALUE:'Two', OTHER:'Other KKK'                                                                                                  |
      | TTAB_FIVE | 1   | ADD    | true         | DATA:<a href="/ui/lob/WlZzM0wwUGlSVDd2emdZbEdDckFtcnFWbTY0M2RXMVp3c2haTlRObUVCYz0=" download="download">LOB</a>, SIMPLE:17.81                    |
      | TTAB_FIVE | 2   | ADD    | true         | DATA:<a href="/ui/lob/TURPbmVyR2cwaWtGQVJLdmloWDBmRkQ4VjJtVXA0K0tIZnJqaTJCeVBLRT0=" download="download">LOB</a>, SIMPLE:17.82                  |
      | TTAB_FIVE | 3   | ADD    | true         | DATA:<a href="/ui/lob/bUdiNG5wa1FiUnZSSnJKV3AvUUlwd0dQcVpURmtLaEkxRlU5bDlqTmoxTT0=" download="download">LOB</a>, SIMPLE:17.83                  |
      | TTAB_SIX  | 7   | ADD    | true         | TEXT:<a href="/ui/lob/QXlvSkNtWk5RWHdrenQyWmNIZ0c4Y3B2VXVjc2JHbmFuVHVRdStwYUdPcz0=" download="download">TEXT</a>, DATE:2012-01-15 00:00:00     |
      | TTAB_SIX  | 8   | ADD    | true         | TEXT:<a href="/ui/lob/S09wazdEUDlpTG5BbHM1L1JvRjErS1JEeFdNRGFBK2VTazJiVUdvOGczZz0=" download="download">TEXT</a>, DATE:2005-07-08 00:00:00 |
      | TTAB_SIX  | 9   | ADD    | true         | TEXT:<a href="/ui/lob/K2Z1REFwVm0ycUh1OEJhU09Pa0tBdElDclRoYzVWTTlFU3pGTS9DL1ZHST0=" download="download">TEXT</a>, DATE:2021-12-25 00:00:00 |

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

  Scenario: The selected merge diff content updates are applied to destination table - simple add content with lobs
    Given the commit ":tada: Test commit init source" has been saved and exported with all the identified initial diff content
    And the user accesses to the destination environment with the same dictionary
    And the existing data in managed table "TTAB_TWO" in destination environment :
      | key | value | other     |
      | VVV | 333   | Other VVV |
      | III | 444   | Other III |
    And no existing data in managed table "TTAB_FIVE" in destination environment
    And no existing data in managed table "TTAB_SIX" in destination environment
    And a commit ":construction: Destination commit initial" has been saved with all the new identified diff content in destination environment
    And a merge diff analysis has been started and completed with the available source package
    And the user has selected all content for merge commit
    And the user has specified a commit comment ":construction: merge commit test with changes"
    When the user save the merge commit
    Then the saved merge commit content has these identified changes :
      | Table     | Key | Action | Payload                                                                                                                         |
      | TTAB_TWO  | JJJ | ADD    | VALUE:'One', OTHER:'Other JJJ'                                                                                                  |
      | TTAB_TWO  | KKK | ADD    | VALUE:'Two', OTHER:'Other KKK'                                                                                                  |
      | TTAB_FIVE | 1   | ADD    | DATA:<a href="/ui/lob/WlZzM0wwUGlSVDd2emdZbEdDckFtcnFWbTY0M2RXMVp3c2haTlRObUVCYz0=" download="download">LOB</a>, SIMPLE:17.81                    |
      | TTAB_FIVE | 2   | ADD    | DATA:<a href="/ui/lob/TURPbmVyR2cwaWtGQVJLdmloWDBmRkQ4VjJtVXA0K0tIZnJqaTJCeVBLRT0=" download="download">LOB</a>, SIMPLE:17.82                  |
      | TTAB_FIVE | 3   | ADD    | DATA:<a href="/ui/lob/bUdiNG5wa1FiUnZSSnJKV3AvUUlwd0dQcVpURmtLaEkxRlU5bDlqTmoxTT0=" download="download">LOB</a>, SIMPLE:17.83                  |
      | TTAB_SIX  | 7   | ADD    | TEXT:<a href="/ui/lob/QXlvSkNtWk5RWHdrenQyWmNIZ0c4Y3B2VXVjc2JHbmFuVHVRdStwYUdPcz0=" download="download">TEXT</a>, DATE:2012-01-15 00:00:00     |
      | TTAB_SIX  | 8   | ADD    | TEXT:<a href="/ui/lob/S09wazdEUDlpTG5BbHM1L1JvRjErS1JEeFdNRGFBK2VTazJiVUdvOGczZz0=" download="download">TEXT</a>, DATE:2005-07-08 00:00:00 |
      | TTAB_SIX  | 9   | ADD    | TEXT:<a href="/ui/lob/K2Z1REFwVm0ycUh1OEJhU09Pa0tBdElDclRoYzVWTTlFU3pGTS9DL1ZHST0=" download="download">TEXT</a>, DATE:2021-12-25 00:00:00 |
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
    And the data in managed table "TTAB_SIX" in destination environment is now :
      | identifier | text                                     | date       |
      | 7          | Ceci est un text enregistré dans un CLOB | 2012-01-15 |
      | 8          | Un autre text CLOB                       | 2005-07-08 |
      | 9          | Encore un autre                          | 2021-12-25 |
