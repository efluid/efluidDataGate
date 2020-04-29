Feature: Access to the application is possible only for registered users after login
  
  Scenario: Without authentication a anonymous user is redirected to login page
    Given the application is fully initialized with the wizard
    And the user is not authenticated
    When the user access to default page
    Then the user is redirected to login page
  
  Scenario: The user authenticate with valid credentials
    Given the application is fully initialized with the wizard
    And the user is not authenticated
    And the user is currently on login page
    When the user specify valid credentials from "any"
    Then the authentication is successful
    And the user is redirected to home page
  
  Scenario: The user cannot authenticate with wrong credentials
    Given the application is fully initialized with the wizard
    And the user is not authenticated
    And the user is currently on login page
    When the user specify invalid credentials from "other"
    Then the authentication is failed
    Then the user is redirected to login error page

  Scenario: With Ldap auth enabled a new user can authenticate with valid Ldap credentials
    Given ldap auth is enabled with search at "ou=persons,dc=company,dc=com", with login attr "uid" and email attr "mail" and this content :
      """ldif
      dn: dc=company,dc=com
      objectClass: domain
      objectClass: top
      dc: company

      dn: ou=persons,dc=company,dc=com
      objectClass: organizationalUnit
      objectClass: top
      ou: persons

      dn: cn=LDAP_USER,ou=persons,dc=company,dc=com
      objectClass: inetOrgPerson
      objectClass: organizationalPerson
      objectClass: person
      objectClass: top
      sn: LDAP_USER
      cn: LDAP_USER
      uid: ldap-user
      userPassword: ldap-user
      """
    And the application is fully initialized with the wizard
    And the user is not authenticated
    And the user is currently on login page
    When the user specify valid credentials from "ldap"
    Then the authentication is successful
    And the user is redirected to home page
    And the ldap-user user is stored

  Scenario: With Ldap auth enabled a user cannot authenticate without valid Ldap credentials
    Given ldap auth is enabled with search at "ou=persons,dc=company,dc=com", with login attr "uid" and email attr "mail" and this content :
      """ldif
      dn: dc=company,dc=com
      objectClass: domain
      objectClass: top
      dc: company

      dn: ou=persons,dc=company,dc=com
      objectClass: organizationalUnit
      objectClass: top
      ou: persons

      dn: cn=LDAP_USER,ou=persons,dc=company,dc=com
      objectClass: inetOrgPerson
      objectClass: organizationalPerson
      objectClass: person
      objectClass: top
      sn: LDAP_USER
      cn: LDAP_USER
      uid: ldap-user
      userPassword: ldap-user
      """
    And the application is fully initialized with the wizard
    And the user is not authenticated
    And the user is currently on login page
    When the user specify invalid credentials from "other"
    Then the authentication is failed
    Then the user is redirected to login error page

