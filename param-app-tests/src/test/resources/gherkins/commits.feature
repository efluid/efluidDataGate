Feature: The update on parameter tables can be followed, checked and stored as commits. 

  The features are similar to the way GIT can be used to manage updates on source files, but adapted
  for database contents. The content is first identified, by looking on changes and comparing them
  to a local index, and then "stored" in commits. The commits can be shared with export / import files
  (similar to GIT push / pull processes)
  
  background:
    Given the dictionary is fully initialized with tables 1, 2 and 3
    And the user is authenticated
    And a valid version is defined
    And no diff is running
  
  Scenario: A diff analysis can be launched
    Given from the home page
    When the user access to diff launch page
    Then the provided template is diff running
    And a diff is running
    
  Scenario: Only one diff can be launched for current project
    Given from the home
    And a diff has already been launched
    When the user access to diff launch page
    Then an alert says that the diff is still running
      
