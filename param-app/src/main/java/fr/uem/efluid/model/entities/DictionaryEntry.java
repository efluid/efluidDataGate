package fr.uem.efluid.model.entities;

import java.time.LocalDateTime;
import java.util.UUID;

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import fr.uem.efluid.ColumnType;
import fr.uem.efluid.model.shared.ExportAwareDictionaryEntry;
import fr.uem.efluid.utils.SharedOutputInputUtils;

/**
 * <p>
 * Definition of a parameter table, in a "big central dictionary of all parameters".
 * Mostly a link between some business related datas which are used for a more user
 * friendly rendering, and some technical datas used to select the right parameters value
 * from managed database.
 * </p>
 * 
 * @author elecomte
 * @since v0.0.1
 * @version 1
 */
@Entity
@Table(name = "dictionary")
public class DictionaryEntry extends ExportAwareDictionaryEntry<FunctionalDomain> {

	@Id
	private UUID uuid;

	@NotNull
	private String parameterName;

	// TODO : add protection against SQL Injection

	@NotNull
	private String tableName;

	private String whereClause;

	@Size(max = 4096)
	private String selectClause;

	@NotNull
	private String keyName;

	@Enumerated(EnumType.STRING)
	@NotNull
	private ColumnType keyType;

	@NotNull
	private LocalDateTime createdTime;

	@NotNull
	private LocalDateTime updatedTime;

	private LocalDateTime importedTime;

	@ManyToOne(optional = false)
	private FunctionalDomain domain;

	/**
	 * @param uuid
	 */
	public DictionaryEntry(UUID uuid) {
		super();
		this.uuid = uuid;
	}

	/**
	 * 
	 */
	public DictionaryEntry() {
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
	 * @return the updatedTime
	 */
	@Override
	public LocalDateTime getUpdatedTime() {
		return this.updatedTime;
	}

	/**
	 * @param updatedTime
	 *            the updatedTime to set
	 */
	public void setUpdatedTime(LocalDateTime updatedTime) {
		this.updatedTime = updatedTime;
	}

	/**
	 * @return the domain
	 */
	@Override
	public FunctionalDomain getDomain() {
		return this.domain;
	}

	/**
	 * @param domain
	 *            the domain to set
	 */
	public void setDomain(FunctionalDomain domain) {
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

		SharedOutputInputUtils.fromJson(raw)
				.applyUUID("uid", v -> setUuid(v))
				.applyLdt("cre", v -> setCreatedTime(v))
				.applyLdt("upd", v -> setUpdatedTime(v))
				.applyUUID("dom", v -> setDomain(new FunctionalDomain(v)))
				.applyString("kna", v -> setKeyName(v))
				.applyString("kty", v -> setKeyType(ColumnType.valueOf(v)))
				.applyString("nam", v -> setParameterName(v))
				.applyString("sel", v -> setSelectClause(v))
				.applyString("tab", v -> setTableName(v))
				.applyString("whe", v -> setWhereClause(v));
	}

}
