package fr.uem.efluid.model.entities;

import java.time.LocalDateTime;
import java.util.UUID;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

import fr.uem.efluid.model.Shared;

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

}
