package fr.uem.efluid.rest.v1.model;

import java.util.UUID;

/**
 * @author elecomte
 * @since v0.2.0
 * @version 1
 */
public class ProjectDetailView {

	private String name;

	private UUID uuid;

	private String color;

	/**
	 * 
	 */
	public ProjectDetailView() {
		super();
	}

	/**
	 * @return the name
	 */
	public String getName() {
		return this.name;
	}

	/**
	 * @param name
	 *            the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * @return the uuid
	 */
	public UUID getUuid() {
		return this.uuid;
	}

	/**
	 * @param uuid
	 *            the uuid to set
	 */
	public void setUuid(UUID uuid) {
		this.uuid = uuid;
	}

	/**
	 * @return the color
	 */
	public String getColor() {
		return this.color;
	}

	/**
	 * @param color
	 *            the color to set
	 */
	public void setColor(String color) {
		this.color = color;
	}
}
