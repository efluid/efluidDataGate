package fr.uem.efluid.model.metas;

/**
 * @author elecomte
 * @since v0.0.1
 * @version 1
 */
public class ColumnDescription {

	private String name;
	private boolean binary;
	private boolean primaryKey;
	private String foreignKeyTable;

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
	 * @return the binary
	 */
	public boolean isBinary() {
		return this.binary;
	}

	/**
	 * @param binary
	 *            the binary to set
	 */
	public void setBinary(boolean binary) {
		this.binary = binary;
	}

	/**
	 * @return the primaryKey
	 */
	public boolean isPrimaryKey() {
		return this.primaryKey;
	}

	/**
	 * @param primaryKey
	 *            the primaryKey to set
	 */
	public void setPrimaryKey(boolean primaryKey) {
		this.primaryKey = primaryKey;
	}

	/**
	 * @return the foreignKeyTable
	 */
	public String getForeignKeyTable() {
		return this.foreignKeyTable;
	}

	/**
	 * @param foreignKeyTable
	 *            the foreignKeyTable to set
	 */
	public void setForeignKeyTable(String foreignKeyTable) {
		this.foreignKeyTable = foreignKeyTable;
	}

}
