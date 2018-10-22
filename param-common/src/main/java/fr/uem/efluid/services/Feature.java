package fr.uem.efluid.services;

/**
 * <p>
 * Behavior activation, specified as code, initialized as standard application properties,
 * stored in database and updatable dynamically using specific service
 * </p>
 * 
 * @author elecomte
 * @since v0.0.8
 * @version 1
 */
public enum Feature {

	/**
	 * <p>
	 * For managed database updates : control the missing ids in update queries
	 * </p>
	 */
	CHECK_MISSING_IDS_AT_MANAGED_UPDATE("param-efluid.managed-updates.check-update-missing-ids"),

	/**
	 * <p>
	 * For managed database updates : control the missing ids in delete queries
	 * </p>
	 */
	CHECK_MISSING_IDS_AT_MANAGED_DELETE("param-efluid.managed-updates.check-delete-missing-ids"),

	/**
	 * <p>
	 * Check the dictionary version during import of a commit
	 * </p>
	 */
	VALIDATE_VERSION_FOR_IMPORT("param-efluid.imports.check-model-version");

	private final String propertyKey;

	/**
	 * @param propertyKey
	 */
	private Feature(String propertyKey) {
		this.propertyKey = propertyKey;
	}

	/**
	 * @return the propertyKey
	 */
	public String getPropertyKey() {
		return this.propertyKey;
	}
}
