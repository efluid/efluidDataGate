package fr.uem.efluid.model.shared;

import java.time.LocalDateTime;

import fr.uem.efluid.ColumnType;
import fr.uem.efluid.model.Shared;
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
public abstract class ExportAwareDictionaryEntry<D extends ExportAwareFunctionalDomain> implements Shared {

	/**
	 * @param parameterName
	 *            the parameterName to set
	 */
	public abstract String getParameterName();

	/**
	 * @param tableName
	 *            the tableName to set
	 */
	public abstract String getTableName();

	/**
	 * @param whereClause
	 *            the whereClause to set
	 */
	public abstract String getWhereClause();

	/**
	 * @param selectClause
	 *            the selectClause to set
	 */
	public abstract String getSelectClause();

	/**
	 * @param keyName
	 *            the keyName to set
	 */
	public abstract String getKeyName();

	/**
	 * @param createdTime
	 *            the createdTime to set
	 */
	public abstract LocalDateTime getCreatedTime();

	/**
	 * @param domain
	 *            the domain to set
	 */
	public abstract D getDomain();

	/**
	 * @param keyType
	 *            the keyType to set
	 */
	public abstract ColumnType getKeyType();

	/**
	 * @return
	 * @see fr.uem.efluid.model.Shared#serialize()
	 */
	@Override
	public final String serialize() {

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
	 * @return
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((this.getUuid() == null) ? 0 : this.getUuid().hashCode());
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
		if (!(obj instanceof ExportAwareDictionaryEntry))
			return false;
		ExportAwareDictionaryEntry<?> other = (ExportAwareDictionaryEntry<?>) obj;
		if (this.getUuid() == null) {
			if (other.getUuid() != null)
				return false;
		} else if (!this.getUuid().equals(other.getUuid()))
			return false;
		return true;
	}

	/**
	 * @return
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "DicEntry [<" + this.getUuid() + "> table:" + this.getTableName()
				+ ", where:" + this.getWhereClause() + ", select:" + this.getSelectClause() + ", key:" + this.getKeyName() + "]";
	}
}
