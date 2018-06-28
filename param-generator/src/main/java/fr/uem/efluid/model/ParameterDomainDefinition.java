package fr.uem.efluid.model;

import java.time.LocalDateTime;
import java.util.UUID;

import fr.uem.efluid.ParameterDomain;
import fr.uem.efluid.model.shared.ExportAwareFunctionalDomain;

/**
 * @author elecomte
 * @since v0.0.1
 * @version 1
 */
@SpecifiedWith(ParameterDomain.class)
public class ParameterDomainDefinition extends ExportAwareFunctionalDomain<ParameterProjectDefinition> {

	private UUID uuid;

	private String name;

	private LocalDateTime createdTime;

	private ParameterProjectDefinition project;

	/**
	 * @param uuid
	 */
	public ParameterDomainDefinition(UUID uuid) {
		super();
		this.uuid = uuid;
	}

	/**
	 * 
	 */
	public ParameterDomainDefinition() {
		super();
	}

	/**
	 * @return the uuid
	 */
	@Override
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
	@Override
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
	@Override
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
	@Override
	public LocalDateTime getImportedTime() {
		return null;
	}

	/**
	 * @return the project
	 */
	@Override
	public ParameterProjectDefinition getProject() {
		return this.project;
	}

	/**
	 * @param project
	 *            the project to set
	 */
	public void setProject(ParameterProjectDefinition project) {
		this.project = project;
	}

	/**
	 * @param raw
	 * @see fr.uem.efluid.model.Shared#deserialize(java.lang.String)
	 */
	@Override
	public void deserialize(String raw) {
		// Not implemented
	}
}
