package fr.uem.efluid.model;

import java.time.LocalDateTime;
import java.util.UUID;

import fr.uem.efluid.ColumnType;
import fr.uem.efluid.ParameterTable;
import fr.uem.efluid.model.shared.ExportAwareDictionaryEntry;

/**
 * <p>
 * Definition of a parameter table specified with {@link ParameterTable}
 * </p>
 * 
 * @author elecomte
 * @since v0.0.1
 * @version 1
 */
@SpecifiedWith(ParameterTable.class)
public class ParameterTableDefinition extends ExportAwareDictionaryEntry<ParameterDomainDefinition> {

	private UUID uuid;

	private String parameterName;

	private String tableName;

	private String whereClause;

	private String selectClause;

	private String keyName;

	private ColumnType keyType;

	private LocalDateTime createdTime;

	private LocalDateTime updatedTime;

	private ParameterDomainDefinition domain;

	/**
	 * 
	 */
	public ParameterTableDefinition() {
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
	 * @return the parameterName
	 */
	@Override
	public String getParameterName() {
		return this.parameterName;
	}

	/**
	 * @param parameterName
	 *            the parameterName to set
	 */
	public void setParameterName(String parameterName) {
		this.parameterName = parameterName;
	}

	/**
	 * @return the tableName
	 */
	@Override
	public String getTableName() {
		return this.tableName;
	}

	/**
	 * @param tableName
	 *            the tableName to set
	 */
	public void setTableName(String tableName) {
		this.tableName = tableName;
	}

	/**
	 * @return the whereClause
	 */
	@Override
	public String getWhereClause() {
		return this.whereClause;
	}

	/**
	 * @param whereClause
	 *            the whereClause to set
	 */
	public void setWhereClause(String whereClause) {
		this.whereClause = whereClause;
	}

	/**
	 * @return the selectClause
	 */
	@Override
	public String getSelectClause() {
		return this.selectClause;
	}

	/**
	 * @param selectClause
	 *            the selectClause to set
	 */
	public void setSelectClause(String selectClause) {
		this.selectClause = selectClause;
	}

	/**
	 * @return the keyName
	 */
	@Override
	public String getKeyName() {
		return this.keyName;
	}

	/**
	 * @param keyName
	 *            the keyName to set
	 */
	public void setKeyName(String keyName) {
		this.keyName = keyName;
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
	 * @return the updatedTime
	 */
	@Override
	public LocalDateTime getUpdatedTime() {
		return this.updatedTime;
	}

	/**
	 * @param updatedTime the updatedTime to set
	 */
	public void setUpdatedTime(LocalDateTime updatedTime) {
		this.updatedTime = updatedTime;
	}
	/**
	 * @return the domain
	 */
	@Override
	public ParameterDomainDefinition getDomain() {
		return this.domain;
	}

	/**
	 * @param domain
	 *            the domain to set
	 */
	public void setDomain(ParameterDomainDefinition domain) {
		this.domain = domain;
	}

	/**
	 * @return the keyType
	 */
	@Override
	public ColumnType getKeyType() {
		return this.keyType;
	}

	/**
	 * @param keyType
	 *            the keyType to set
	 */
	public void setKeyType(ColumnType keyType) {
		this.keyType = keyType;
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
