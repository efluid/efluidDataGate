package fr.uem.efluid.stubs;

import java.util.UUID;

/**
 * @author elecomte
 * @since v0.2.0
 * @version 1
 */
public class DataLoadResult {

	private final UUID dicUuid;
	private final UUID projectUuid;

	/**
	 * @param dicUuid
	 * @param projectUuid
	 */
	public DataLoadResult(UUID dicUuid, UUID projectUuid) {
		super();
		this.dicUuid = dicUuid;
		this.projectUuid = projectUuid;
	}

	/**
	 * @return the dicUuid
	 */
	public UUID getDicUuid() {
		return this.dicUuid;
	}

	/**
	 * @return the projectUuid
	 */
	public UUID getProjectUuid() {
		return this.projectUuid;
	}

}
