package fr.uem.efluid.rest.v1.model;

import java.time.LocalDateTime;

/**
 * @author elecomte
 * @since v0.2.0
 * @version 1
 */
public class VersionView {

	private final String name;

	private final String modelId;

	private final LocalDateTime updatedTime;

	private final boolean canUpdate;

	/**
	 * @param name
	 * @param createdTime
	 */
	public VersionView(String name, String modelId, LocalDateTime updatedTime, boolean canUpdate) {
		super();
		this.name = name;
		this.modelId = modelId;
		this.updatedTime = updatedTime;
		this.canUpdate = canUpdate;
	}

	/**
	 * @return the name
	 */
	public String getName() {
		return this.name;
	}

	/**
	 * @return the modelId
	 */
	public String getModelId() {
		return this.modelId;
	}

	/**
	 * @return the updatedTime
	 */
	public LocalDateTime getUpdatedTime() {
		return this.updatedTime;
	}

	/**
	 * @return
	 */
	public boolean isCanUpdate() {
		return this.canUpdate;
	}

}
