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

import fr.uem.efluid.model.Shared;
import fr.uem.efluid.model.metas.ColumnType;
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
public class DictionaryEntry implements Shared {

	@Id
	private UUID uuid;

	@NotNull
	private String parameterName;

	// TODO : add protection against SQL Injection

	@NotNull
	private String tableName;

	private String whereClause;

	private String selectClause;

	private String keyName;

	@Enumerated(EnumType.STRING)
	private ColumnType keyType;

	@NotNull
	private LocalDateTime createdTime;

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
	 * @return the domain
	 */
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
	 * @return
	 * @see fr.uem.efluid.model.Shared#serialize()
	 */
	@Override
	public String serialize() {

		return SharedOutputInputUtils.newJson()
				.with("uid", getUuid())
				.with("cre", getCreatedTime())
				.with("dom", getDomain().getUuid())
				.with("kna", getKeyName())
				.with("kty", getKeyType())
				.with("nam", getParameterName())
				.with("sel", getSelectClause())
				.with("tab", getTableName())
				.with("whe", getWhereClause())
				.toString();
	}

	/**
	 * @param raw
	 * @see fr.uem.efluid.model.Shared#deserialize(java.lang.String)
	 */
	@Override
	public void deserialize(String raw) {

		SharedOutputInputUtils.fromJson(raw)
				.apply("uid", UUID.class, v -> setUuid(v))
				.apply("cre", LocalDateTime.class, v -> setCreatedTime(v))
				.apply("dom", UUID.class, v -> setDomain(new FunctionalDomain(v)))
				.apply("kna", String.class, v -> setKeyName(v))
				.apply("kty", ColumnType.class, v -> setKeyType(v))
				.apply("nam", String.class, v -> setParameterName(v))
				.apply("sel", String.class, v -> setSelectClause(v))
				.apply("tab", String.class, v -> setTableName(v))
				.apply("whe", String.class, v -> setWhereClause(v));
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
		DictionaryEntry other = (DictionaryEntry) obj;
		if (this.uuid == null) {
			if (other.uuid != null)
				return false;
		} else if (!this.uuid.equals(other.uuid))
			return false;
		return true;
	}

	/**
	 * @return
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "DicEntry [<" + this.uuid + "> table:" + this.tableName
				+ ", where:" + this.whereClause + ", select:" + this.selectClause + ", key:" + this.keyName + "]";
	}

}
