package fr.uem.efluid.utils;

import org.springframework.boot.autoconfigure.jdbc.DataSourceBuilder;
import org.springframework.jdbc.core.JdbcTemplate;

import fr.uem.efluid.tools.ManagedQueriesGenerator.QueryGenerationRules;

/**
 * @author elecomte
 * @since v0.0.1
 * @version 1
 */
public final class DatasourceUtils {

	public static final String MANAGED_TRANSACTION_MANAGER = "managedTransactionManager";

	/**
	 * @return JdbcTemplate for managed database access
	 */
	public static JdbcTemplate createJdbcTemplate(CustomDataSourceParameters params) {
		return new JdbcTemplate(
				DataSourceBuilder.create()
						.url(params.getUrl())
						.driverClassName(params.getDriverClassName())
						.username(params.getUsername())
						.password(params.getPassword())
						.build());
	}

	/**
	 * <p>
	 * A basic definition for a datasource spec. App needs 2 : one "core" DB for own data
	 * (using standard spring boot config), and one "parameters" DB for managed
	 * application, built from this configuration model.
	 * 
	 * @author elecomte
	 * @since v0.0.1
	 * @version 1
	 */
	public static class CustomDataSourceParameters {

		private String driverClassName;
		private String url;
		private String username;
		private String password;
		private CustomQueryGenerationRules query;

		/**
		 * 
		 */
		public CustomDataSourceParameters() {
			super();
		}

		/**
		 * @return the driverClassName
		 */
		public String getDriverClassName() {
			return this.driverClassName;
		}

		/**
		 * @param driverClassName
		 *            the driverClassName to set
		 */
		public void setDriverClassName(String driverClassName) {
			this.driverClassName = driverClassName;
		}

		/**
		 * @return the url
		 */
		public String getUrl() {
			return this.url;
		}

		/**
		 * @param url
		 *            the url to set
		 */
		public void setUrl(String url) {
			this.url = url;
		}

		/**
		 * @return the username
		 */
		public String getUsername() {
			return this.username;
		}

		/**
		 * @param username
		 *            the username to set
		 */
		public void setUsername(String username) {
			this.username = username;
		}

		/**
		 * @return the password
		 */
		public String getPassword() {
			return this.password;
		}

		/**
		 * @param password
		 *            the password to set
		 */
		public void setPassword(String password) {
			this.password = password;
		}

		/**
		 * @return the query
		 */
		public CustomQueryGenerationRules getQuery() {
			return this.query;
		}

		/**
		 * @param query the query to set
		 */
		public void setQuery(CustomQueryGenerationRules query) {
			this.query = query;
		}

	}

	/**
	 * <p>
	 * Config sub-bean for query generation rules regarding the managed DB
	 * </p>
	 * 
	 * @author elecomte
	 * @since v0.0.1
	 * @version 1
	 */
	public static class CustomQueryGenerationRules implements QueryGenerationRules {

		private boolean columnNamesProtected;

		private boolean tableNamesProtected;

		/**
		 * 
		 */
		public CustomQueryGenerationRules() {
			super();
		}

		/**
		 * @return the columnNamesProtected
		 */
		@Override
		public boolean isColumnNamesProtected() {
			return this.columnNamesProtected;
		}

		/**
		 * @param columnNamesProtected
		 *            the columnNamesProtected to set
		 */
		public void setColumnNamesProtected(boolean columnNamesProtected) {
			this.columnNamesProtected = columnNamesProtected;
		}

		/**
		 * @return the tableNamesProtected
		 */
		@Override
		public boolean isTableNamesProtected() {
			return this.tableNamesProtected;
		}

		/**
		 * @param tableNamesProtected
		 *            the tableNamesProtected to set
		 */
		public void setTableNamesProtected(boolean tableNamesProtected) {
			this.tableNamesProtected = tableNamesProtected;
		}
	}
}
