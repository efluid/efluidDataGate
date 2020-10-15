package fr.uem.efluid.services;

/**
 * <p>
 * Behavior activation, specified as code, initialized as standard application properties,
 * stored in database and updatable dynamically using specific service
 * </p>
 *
 * @author elecomte
 * @version 1
 * @since v0.0.8
 */
public enum Feature {

    /**
     * <p>
     * For managed database updates : control the missing ids in update queries
     * </p>
     */
    CHECK_MISSING_IDS_AT_MANAGED_UPDATE("datagate-efluid.managed-updates.check-update-missing-ids"),

    /**
     * <p>
     * For managed database updates : control the missing ids in delete queries
     * </p>
     */
    CHECK_MISSING_IDS_AT_MANAGED_DELETE("datagate-efluid.managed-updates.check-delete-missing-ids"),

    /**
     * <p>
     * Check the dictionary version during import of a commit
     * </p>
     */
    VALIDATE_VERSION_FOR_IMPORT("datagate-efluid.imports.check-model-version"),

    /**
     * <p>
     * Check the dictionary content compatibility (by using a real dictionary diff for tables concerned by the index content) during import of a commit,
     * to validate if the import can be processed even if the versions are not compatible
     * </p>
     */
    CHECK_DICTIONARY_COMPATIBILITY_FOR_IMPORT("datagate-efluid.imports.check-dictionary-compatibility"),

    /**
     * <p>
     * Check if a ref commit (a not exported "ref only" commit for partial export) is present in local instance. If this parameter is `true` then
     * the merge import will fail as some required commits are not imported
     * </p>
     */
    VALIDATE_MISSING_REF_COMMITS_FOR_IMPORT("datagate-efluid.imports.check-missing-ref-commits"),

    /**
     * <p>If enabled, the database PK are pre-specified as dictionary entry keys</p>
     */
    SELECT_PK_AS_DEFAULT_DICT_ENTRY_KEY("datagate-efluid.dictionary.select-pk-as-default-keys"),

    /**
     * <p>For version creates : If enabled, force use the modelIdentifier result as version name (no user select of version name)</p>
     */
    USE_MODEL_ID_AS_VERSION_NAME("datagate-efluid.versions.use-model-id-as-version"),

    /**
     * For import / merge : keep as "anomaly" identified resolution errors
     */
    RECORD_IMPORT_WARNINGS("datagate-efluid.imports.record-merge-warnings");


    private final String propertyKey;

    /**
     * @param propertyKey
     */
    Feature(String propertyKey) {
        this.propertyKey = propertyKey;
    }

    /**
     * @return the propertyKey
     */
    public String getPropertyKey() {
        return this.propertyKey;
    }
}
