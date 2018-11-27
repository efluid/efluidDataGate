package fr.uem.efluid.services.types;

import java.util.List;

/**
 * @author elecomte
 * @since v0.0.8
 * @version 1
 */
public class TestQueryData {

	private final List<List<String>> table;

	private final long totalCount;

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
}
