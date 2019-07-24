package fr.uem.efluid.services.types;

import java.util.List;

/**
 * @author elecomte
 * @since v0.0.8
 * @version 1
 */
public class TestQueryData {

	private List<List<String>> table;

	private long totalCount;

	/**
	 * 
	 */
	public TestQueryData() {
		super();
	}

	/**
	 * @param table
	 * @param totalCount
	 */
	public TestQueryData(List<List<String>> table, long totalCount) {
		super();
		this.table = table;
		this.totalCount = totalCount;
	}

	public List<List<String>> getTable() {
		return this.table;
	}

	public long getTotalCount() {
		return this.totalCount;
	}

	public void setTable(List<List<String>> table) {
		this.table = table;
	}

	public void setTotalCount(long totalCount) {
		this.totalCount = totalCount;
	}
}
