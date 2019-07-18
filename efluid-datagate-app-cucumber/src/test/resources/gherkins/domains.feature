Feature: The dictionary is specified in functional domains

  Scenario: A functional domain edit page is available to all users
    Given from the home page
    When the user access to functional domain edit page
    Then the provided template is functional domain

  Scenario: The existing domains are listed
    Given the existing functional domains My domain, Device management, Other test domain
    When the user access to functional domain edit page
    Then the 3 existing functional domains are displayed

  Scenario: A domain can be added to the list of functional domains
    Given the existing functional domains My domain, Device management, Other test domain
    When the user add functional domain Demo domain
    Then the 4 updated functional domains are displayed

  Scenario: A domain can be removed from the list of functional domains if not used in commits
    Given the existing functional domains My domain, Device management, Other test domain
    When the user remove functional domain My domain
    Then the 2 remaining functional domains are displayed

  @SINGLE
  Scenario: An imported domain is deduplicated if a domain with the same name exists
    Given the existing functional domains My domain, Device management, Other test domain
    When the user import a package "datasets/imports/one-domain.par" containing a new domain name My domain
    Then only 3 domains are still existing and the old My domain functional domain is deleted
