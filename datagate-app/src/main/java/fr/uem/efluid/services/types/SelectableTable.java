package fr.uem.efluid.services.types;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author elecomte
 * @since v0.0.1
 * @version 1
 */
public class SelectableTable implements Comparable<SelectableTable> {

	private final String tableName;
	private final String entryName;
	private final String domainName;
	private final List<String> columnNames;

	/**
	 * @param tableName
	 * @param entryName
	 */
	public SelectableTable(String tableName, String entryName, String domainName, List<String> columnNames) {
		super();
		this.tableName = tableName;
		this.entryName = entryName;
		this.domainName = domainName;
		this.columnNames = columnNames;
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
	 * @return the columnNames
	 */
	public List<String> getColumnNames() {
		return this.columnNames;
	}

	/**
	 * @return
	 */
	public String getFusedColumnNames() {
		return this.columnNames != null ? this.columnNames.stream().collect(Collectors.joining(", ")) : null;
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
