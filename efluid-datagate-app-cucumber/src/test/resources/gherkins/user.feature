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
