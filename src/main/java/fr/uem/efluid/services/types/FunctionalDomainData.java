package fr.uem.efluid.services.types;

import java.time.LocalDateTime;
import java.util.UUID;

import fr.uem.efluid.model.entities.FunctionalDomain;

/**
 * @author elecomte
 * @since v0.0.1
 * @version 1
 */
public final class FunctionalDomainData {

	private UUID uuid;
	private String name;
	private LocalDateTime createdTime;
	private LocalDateTime importedTime;
	private boolean canDelete;

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
	 * @return the createdTime
	 */
	public LocalDateTime getCreatedTime() {
		return this.createdTime;
	}

	/**
	 * @param createdTime
	 *            the createdTime to set
	 */
	public void setCreatedTime(LocalDateTime createdTime) {
		this.createdTime = createdTime;
	}

	/**
	 * @return the importedTime
	 */
	public LocalDateTime getImportedTime() {
		return this.importedTime;
	}

	/**
	 * @param importedTime
	 *            the importedTime to set
	 */
	public void setImportedTime(LocalDateTime importedTime) {
		this.importedTime = importedTime;
	}

	/**
	 * @return the canDelete
	 */
	public boolean isCanDelete() {
		return this.canDelete;
	}

	/**
	 * @param canDelete
	 *            the canDelete to set
	 */
	public void setCanDelete(boolean canDelete) {
		this.canDelete = canDelete;
	}

	/**
	 * @param data
	 * @return
	 */
	public static FunctionalDomain toEntity(FunctionalDomainData data) {
		FunctionalDomain entity = new FunctionalDomain();
		entity.setName(data.getName());
		entity.setCreatedTime(data.getCreatedTime());
		entity.setImportedTime(data.getImportedTime());
		entity.setUuid(data.getUuid());
		return entity;
	}

	/**
	 * @param entity
	 * @return
	 */
	public static FunctionalDomainData fromEntity(FunctionalDomain entity) {
		FunctionalDomainData data = new FunctionalDomainData();
		data.setName(entity.getName());
		data.setCreatedTime(entity.getCreatedTime());
		data.setImportedTime(entity.getImportedTime());
		data.setUuid(entity.getUuid());
		return data;
	}
}