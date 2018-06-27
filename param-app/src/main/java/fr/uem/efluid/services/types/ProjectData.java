package fr.uem.efluid.services.types;

import java.util.UUID;

import fr.uem.efluid.model.entities.Project;

/**
 * @author elecomte
 * @since v0.2.0
 * @version 1
 */
public class ProjectData {

	private UUID uuid;

	private String name;

	/**
	 * 
	 */
	public ProjectData() {
		super();
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
	 * @return
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((this.uuid == null) ? 0 : this.uuid.hashCode());
		return result;
	}

	/**
	 * @param obj
	 * @return
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ProjectData other = (ProjectData) obj;
		if (this.uuid == null) {
			if (other.uuid != null)
				return false;
		} else if (!this.uuid.equals(other.uuid))
			return false;
		return true;
	}

	/**
	 * @param data
	 * @return
	 */
	public static Project toEntity(ProjectData data) {
		Project entity = new Project();
		entity.setName(data.getName());
		entity.setUuid(data.getUuid());
		return entity;
	}

	/**
	 * @param entity
	 * @return
	 */
	public static ProjectData fromEntity(Project entity) {

		if (entity == null) {
			return null;
		}

		ProjectData data = new ProjectData();
		data.setName(entity.getName());
		data.setUuid(entity.getUuid());
		return data;
	}
}
