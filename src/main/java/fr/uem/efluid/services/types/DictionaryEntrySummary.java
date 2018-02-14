package fr.uem.efluid.services.types;

import java.util.UUID;

import fr.uem.efluid.model.entities.DictionaryEntry;
import fr.uem.efluid.tools.ManagedQueriesGenerator;

/**
 * @author elecomte
 * @since v0.0.1
 * @version 1
 */
public final class DictionaryEntrySummary implements Comparable<DictionaryEntrySummary> {

	private final UUID uuid;

	private final UUID domainUuid;

	private final String domainName;

	private final String name;

	private final String query;

	private boolean canDelete;

	/**
	 * @param uuid
	 * @param domainUuid
	 * @param domainName
	 * @param name
	 * @param query
	 * @param canDelete
	 */
	public DictionaryEntrySummary(UUID uuid, UUID domainUuid, String domainName, String name, String query) {
		super();
		this.uuid = uuid;
		this.domainUuid = domainUuid;
		this.domainName = domainName;
		this.name = name;
		this.query = query;
	}

	/**
	 * @return the uuid
	 */
	public UUID getUuid() {
		return this.uuid;
	}

	/**
	 * @return the domainUuid
	 */
	public UUID getDomainUuid() {
		return this.domainUuid;
	}

	/**
	 * @return the name
	 */
	public String getName() {
		return this.name;
	}

	/**
	 * @return the query
	 */
	public String getQuery() {
		return this.query;
	}

	/**
	 * @return the domainName
	 */
	public String getDomainName() {
		return this.domainName;
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
	 * @param o
	 * @return
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo(DictionaryEntrySummary o) {

		int dom = this.getDomainName().compareTo(o.getDomainName());

		if (dom != 0) {
			return dom;
		}

		return this.getName().compareTo(o.getName());
	}

	/**
	 * @param entity
	 * @return
	 */
	public static DictionaryEntrySummary fromEntity(DictionaryEntry entity, ManagedQueriesGenerator generator) {
		return new DictionaryEntrySummary(
				entity.getUuid(),
				entity.getDomain().getUuid(),
				entity.getDomain().getName(),
				entity.getParameterName(),
				generator.producesSelectParameterQuery(entity));
	}
}