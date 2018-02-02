package fr.uem.efluid.services.types;

/**
 * @author elecomte
 * @since v0.0.1
 * @version 1
 */
public class SelectableTable implements Comparable<SelectableTable> {

	private final String tableName;
	private final String entryName;
	private final String domainName;

	/**
	 * @param tableName
	 * @param entryName
	 */
	public SelectableTable(String tableName, String entryName, String domainName) {
		super();
		this.tableName = tableName;
		this.entryName = entryName;
		this.domainName = domainName;
	}

	/**
	 * @return the tableName
	 */
	public String getTableName() {
		return this.tableName;
	}

	/**
	 * @return the entryName
	 */
	public String getEntryName() {
		return this.entryName;
	}

	/**
	 * @return the domainName
	 */
	public String getDomainName() {
		return this.domainName;
	}

	/**
	 * @param o
	 * @return
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo(SelectableTable o) {
		return this.tableName.compareTo(o.getTableName());
	}
}
