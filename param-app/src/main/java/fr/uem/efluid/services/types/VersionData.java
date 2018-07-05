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

	private final LocalDateTime createdTime;

	private final LocalDateTime updatedTime;

	/**
	 * @param name
	 * @param createdTime
	 * @param updatedTime
	 */
	public VersionData(String name, LocalDateTime createdTime, LocalDateTime updatedTime) {
		super();
		this.name = name;
		this.createdTime = createdTime;
		this.updatedTime = updatedTime;
	}

	/**
	 * @return the name
	 */
	public String getName() {
		return this.name;
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
	 * @param version
	 * @return
	 */
	public static VersionData fromEntity(Version version) {
		return new VersionData(version.getName(), version.getCreatedTime(), version.getUpdatedTime());
	}

	/**
	 * @param data
	 * @return
	 */
	public static VersionView toView(VersionData data) {
		return data != null ? new VersionView(data.getName(), data.getUpdatedTime()) : null;
	}
}
