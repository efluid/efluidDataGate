Feature: A dictionary is associated to versions
  
  Scenario: The list of specified versions is available to all users
    Given from the home page
    When the user access to list of versions
    Then the provided template is list of versions
    
  Scenario: The existing versions are listed
    Given the existing versions v1, v2
    When the user access to list of versions
    Then the 2 existing versions are displayed
    
  Scenario: A version can be added
    Given the existing versions v1, v2
    When the user add new version v3
    Then the 3 updated versions are displayed

  Scenario: A version can be updated to mark last changes in dictionary
    Given the existing versions v1, v2, v3
    When the user update version v3
    Then the 3 updated versions are displayed
    And the update date of version v3 is updated
    
  