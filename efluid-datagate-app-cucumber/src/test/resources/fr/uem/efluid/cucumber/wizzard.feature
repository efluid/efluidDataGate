Feature: A wizard is used to init basic behaviors

  Scenario: The wizard is default enabled after a fresh install
    Given the application is after a fresh install
    When any unauthenticated user access to home page
    Then the user is forwarded to wizard welcome page

  Scenario: The wizard is not enabled if already processed
    Given the application is fully initialized with the wizard
    When any unauthenticated user access to home page
    Then the user is redirected to default page

  Scenario: The wizard start with user creation if no LDAP
    Given the user is on wizard welcome page
    When the user access to wizard step one
    Then the provided template is wizard user creation
    And the user creation page is not an ldap authentication request

  Scenario: The wizard start with user authentication if LDAP auth enabled
    Given ldap auth is enabled with search at "ou=persons", with login attr "uid" and email attr "mail" and this content :
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
      userPassword:: e1NIQX1pZVNWNTVRYytlUU9hWURSU2hhL0Fqek5USkU9
      """
    And the user is on wizard welcome page
    When the user access to wizard step one
    Then the provided template is wizard user creation
    And the user creation page is an ldap authentication request

  Scenario: The user specified on wizard is stored and managed as current user - database only
    Given the user is on wizard user creation
    When the login "demo", the email "test@email.fr" and the password "test" are specified
    Then the demo user is stored
    And the current user is demo
    And the provided template is wizard projects creation

  Scenario: The user specified on wizard is preloaded from successful ldap auth when ldap is enabled
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
    Given the user is on wizard user creation
    When the login "demo" and the password "demo" are specified
    Then the demo user is stored
    And the current user is demo
    And the current user email is demo@company.com
    And the provided template is wizard projects creation

  Scenario: The user specified on wizard is not preloaded from failed ldap auth when ldap is enabled
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
      """
    Given the user is on wizard user creation
    When the login "demo" and the password "error" are specified
    Then the demo user is not stored
    And the provided template is wizard user creation
    And the user creation page is an ldap authentication request
    And an auth error is displayed