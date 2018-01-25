package fr.uem.efluid.services.types;

import java.util.UUID;

import fr.uem.efluid.model.entities.DictionaryEntry;
import fr.uem.efluid.model.entities.FunctionalDomain;

/**
 * @author elecomte
 * @since v0.0.1
 * @version 1
 */
public final class DictionaryEntrySummary {

	private UUID uuid;

	private UUID domainUuid;
	
	private String domainName;

	private String name;

	private String table;

	private String where;

	private String select;

	private boolean canDelete;

	/**
	 * 
	 */
	public DictionaryEntrySummary() {
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
	 * @return the domainUuid
	 */
	public UUID getDomainUuid() {
		return this.domainUuid;
	}

	/**
	 * @param domainId
	 *            the domainUuid to set
	 */
	public void setDomainUuid(UUID domainUuid) {
		this.domainUuid = domainUuid;
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
	 * @return the table
	 */
	public String getTable() {
		return this.table;
	}

	/**
	 * @param table
	 *            the table to set
	 */
	public void setTable(String table) {
		this.table = table;
	}

	/**
	 * @return the where
	 */
	public String getWhere() {
		return this.where;
	}

	/**
	 * @param where
	 *            the where to set
	 */
	public void setWhere(String where) {
		this.where = where;
	}

	/**
	 * @return the select
	 */
	public String getSelect() {
		return this.select;
	}

	/**
	 * @param select
	 *            the select to set
	 */
	public void setSelect(String select) {
		this.select = select;
	}

	/**
	 * @return the domainName
	 */
	public String getDomainName() {
		return this.domainName;
	}

	/**
	 * @param domainName the domainName to set
	 */
	public void setDomainName(String domainName) {
		this.domainName = domainName;
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
	public static DictionaryEntry toEntity(DictionaryEntrySummary data) {
		DictionaryEntry entity = new DictionaryEntry();
		entity.setUuid(data.getUuid());
		entity.setDomain(new FunctionalDomain(data.getDomainUuid()));
		entity.setParameterName(data.getName());
		entity.setSelectClause(data.getSelect());
		entity.setTableName(data.getTable());
		entity.setWhereClause(data.getWhere());

		return entity;
	}

	/**
	 * @param entity
	 * @return
	 */
	public static DictionaryEntrySummary fromEntity(DictionaryEntry entity) {
		DictionaryEntrySummary data = new DictionaryEntrySummary();
		data.setUuid(entity.getUuid());
		data.setDomainUuid(entity.getDomain().getUuid());
		data.setDomainName(entity.getDomain().getName());
		data.setName(entity.getParameterName());
		data.setSelect(entity.getSelectClause());
		data.setTable(entity.getTableName());
		data.setWhere(entity.getWhereClause());
		return data;
	}
}