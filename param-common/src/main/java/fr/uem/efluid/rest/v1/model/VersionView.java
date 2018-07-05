package fr.uem.efluid.rest.v1.model;

import java.time.LocalDateTime;

/**
 * @author elecomte
 * @since v0.2.0
 * @version 1
 */
public class VersionView {

	private final String name;

	private final LocalDateTime updatedTime;

	/**
	 * @param name
	 * @param createdTime
	 */
	public VersionView(String name, LocalDateTime updatedTime) {
		super();
		this.name = name;
		this.updatedTime = updatedTime;
	}

	/**
	 * @return the name
	 */
	public String getName() {
		return this.name;
	}

	/**
	 * @return the updatedTime
	 */
	public LocalDateTime getUpdatedTime() {
		return this.updatedTime;
	}

}
