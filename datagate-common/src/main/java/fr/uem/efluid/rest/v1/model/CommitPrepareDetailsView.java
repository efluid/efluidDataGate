package fr.uem.efluid.rest.v1.model;

import java.util.List;

/**
 * @author elecomte
 * @since v0.0.1
 * @version 1
 */
public class CommitPrepareDetailsView {

	private long indexRowCount;

	private List<CommitPrepareTableView> details;

	/**
	 * 
	 */
	public CommitPrepareDetailsView() {
		super();
	}

	/**
	 * @return the indexRowCount
	 */
	public long getIndexRowCount() {
		return this.indexRowCount;
	}

	/**
	 * @param indexRowCount
	 *            the indexRowCount to set
	 */
	public void setIndexRowCount(long indexRowCount) {
		this.indexRowCount = indexRowCount;
	}

	/**
	 * @return the details
	 */
	public List<CommitPrepareTableView> getDetails() {
		return this.details;
	}

	/**
	 * @param details
	 *            the details to set
	 */
	public void setDetails(List<CommitPrepareTableView> details) {
		this.details = details;
	}

	/**
	 * @author elecomte
	 * @since v0.0.1
	 * @version 1
	 */
	public static class CommitPrepareTableView {

		private String parameter;
		private String table;
		private long indexRowCount;
		private String domain;

		/**
		 * @return the parameter
		 */
		public String getParameter() {
			return this.parameter;
		}

		/**
		 * @param parameter
		 *            the parameter to set
		 */
		public void setParameter(String parameter) {
			this.parameter = parameter;
		}

		/**
		 * @return the table
		 */
		public String getTable() {
			return this.table;
		}

		/**
		 * @param table
		 *            the table to set
		 */
		public void setTable(String table) {
			this.table = table;
		}

		/**
		 * @return the indexRowCount
		 */
		public long getIndexRowCount() {
			return this.indexRowCount;
		}

		/**
		 * @param indexRowCount
		 *            the indexRowCount to set
		 */
		public void setIndexRowCount(long indexRowCount) {
			this.indexRowCount = indexRowCount;
		}

		/**
		 * @return the domain
		 */
		public String getDomain() {
			return this.domain;
		}

		/**
		 * @param domain
		 *            the domain to set
		 */
		public void setDomain(String domain) {
			this.domain = domain;
		}
	}
}
