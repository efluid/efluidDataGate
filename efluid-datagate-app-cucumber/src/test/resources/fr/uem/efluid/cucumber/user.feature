Feature: User management is provided in the application
  
  Scenario: A user edit page is available to all users when auth is not from ldap
    Given from the home page
    When the user access to all users page
    Then the provided template is all users
    And it is possible to create a new user

  Scenario: The user edit page is not available when auth is from ldap
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

      dn: cn=DEMO,ou=persons,dc=company,dc=com
      objectClass: inetOrgPerson
      objectClass: organizationalPerson
      objectClass: person
      objectClass: top
      sn: DEMO
      cn: DEMO
      uid: demo
      userPassword: demo
      mail: demo@company.com
      """
    And from the home page
    When the user access to all users page
    Then the provided template is all users
    And it is not possible to create a new user

  Scenario: A technical user is created at startup on database auth and can connect only to rest service
    When the technical user with token "technical-token" connect to an authenticated rest service
    Then the request is a success

  Scenario: A technical user is created at startup on ldap auth and can connect only to rest service
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

      dn: cn=DEMO,ou=persons,dc=company,dc=com
      objectClass: inetOrgPerson
      objectClass: organizationalPerson
      objectClass: person
      objectClass: top
      sn: DEMO
      cn: DEMO
      uid: demo
      userPassword: demo
      mail: demo@company.com
      """
    When the technical user with token "technical-token" connect to an authenticated rest service
    Then the request is a success

  Scenario: The technical user is not listed in all users
    Given from the home page
    When the user access to all users page
    Then the list of users doesn't include "technical-user"
