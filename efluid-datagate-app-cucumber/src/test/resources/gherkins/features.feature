Feature: Some features can be managed on application for dynamic updates of behaviors

  Scenario: The defaults features are managed and listed with a rest service
    Given the application is fully initialized with the wizard
    When the user access to the feature listing rest service
    Then there are the listed features:
      | feature                             | state    |
      | CHECK_MISSING_IDS_AT_MANAGED_UPDATE | enabled  |
      | CHECK_MISSING_IDS_AT_MANAGED_DELETE | enabled  |
      | VALIDATE_VERSION_FOR_IMPORT         | enabled  |
      | SELECT_PK_AS_DEFAULT_DICT_ENTRY_KEY | enabled  |
      | USE_MODEL_ID_AS_VERSION_NAME        | disabled |
