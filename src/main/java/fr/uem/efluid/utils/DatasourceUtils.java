package fr.uem.efluid.utils;

import org.springframework.boot.autoconfigure.jdbc.DataSourceBuilder;
import org.springframework.jdbc.core.JdbcTemplate;

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

	}
}
