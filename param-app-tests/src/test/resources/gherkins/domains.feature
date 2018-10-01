Feature: The dictionary is specified in functional domains
  
  Scenario: A functional domain edit page is available to all users
    Given from the home page
    When the user access to functional domain edit page
    Then the provided template is functional domain
    
  Scenario: The existing domains are listed
    Given the existing functional domains My domain, Device management, Other test domain
    When the user access to functional domain edit page
    Then the 3 existing functional domains are displayed

  
  Scenario: The existing domains are listed
    Given the existing functional domains My domain, Device management, Other test domain
    When the user add functional domain Demo domain
    Then the 4 updated functional domains are displayed

  