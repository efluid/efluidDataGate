package fr.uem.efluid.model.metas;

/**
 * <p>
 * Metadata model for a column : details on name, pk, fk and type
 * </p>
 * 
 * @author elecomte
 * @since v0.0.1
 * @version 1
 */
public class ColumnDescription implements Comparable<ColumnDescription> {

	private String name;
	private ColumnType type;
	private String foreignKeyTable;
	private String foreignKeyColumn;

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
	 * @return the type
	 */
	public ColumnType getType() {
		return this.type;
	}

	/**
	 * @param type
	 *            the type to set
	 */
	public void setType(ColumnType type) {
		this.type = type;
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

	/**
	 * @return the foreignKeyColumn
	 */
	public String getForeignKeyColumn() {
		return this.foreignKeyColumn;
	}

	/**
	 * @param foreignKeyColumn
	 *            the foreignKeyColumn to set
	 */
	public void setForeignKeyColumn(String foreignKeyColumn) {
		this.foreignKeyColumn = foreignKeyColumn;
	}

	/**
	 * @param o
	 * @return
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo(ColumnDescription o) {
		return this.name.compareTo(o.getName());
	}
}
