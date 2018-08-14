package fr.uem.efluid.services.types;

import java.time.LocalDateTime;

import fr.uem.efluid.model.entities.Version;
import fr.uem.efluid.rest.v1.model.VersionView;

/**
 * <p>
 * DTO for version info
 * </p>
 * 
 * @author elecomte
 * @since v0.2.0
 * @version 1
 */
public class VersionData {

	private final String name;

	private final String modelId;

	private final LocalDateTime createdTime;

	private final LocalDateTime updatedTime;

	private boolean canUpdate;

	/**
	 * @param name
	 * @param createdTime
	 * @param updatedTime
	 */
	public VersionData(String name, String modelId, LocalDateTime createdTime, LocalDateTime updatedTime, boolean canUpdate) {
		super();
		this.name = name;
		this.modelId = modelId;
		this.createdTime = createdTime;
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
	 * @return the createdTime
	 */
	public LocalDateTime getCreatedTime() {
		return this.createdTime;
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

	/**
	 * @param version
	 * @return
	 */
	public static VersionData fromEntity(Version version, boolean canUpdate) {
		return new VersionData(
				version.getName(),
				version.getModelIdentity() != null ? version.getModelIdentity() : " n/a ",
				version.getCreatedTime(),
				version.getUpdatedTime(),
				canUpdate);
	}

	/**
	 * @param data
	 * @return
	 */
	public static VersionView toView(VersionData data) {
		return data != null ? new VersionView(data.getName(), data.getModelId(), data.getUpdatedTime(), data.isCanUpdate()) : null;
	}
}
