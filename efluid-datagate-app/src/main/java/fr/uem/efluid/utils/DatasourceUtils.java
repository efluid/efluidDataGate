package fr.uem.efluid.utils;

import java.lang.reflect.InvocationTargetException;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.SQLException;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.jdbc.core.JdbcTemplate;

import com.zaxxer.hikari.HikariDataSource;

import fr.uem.efluid.tools.diff.ManagedQueriesGenerator.QueryGenerationRules;

/**
 * @author elecomte
 * @since v0.0.1
 * @version 1
 */
public final class DatasourceUtils {

	private static final Logger LOGGER = LoggerFactory.getLogger(DatasourceUtils.class);

	public static final String MANAGED_TRANSACTION_MANAGER = "managedTransactionManager";

	public static final String DEFAULT_TRANSACTION_MANAGER = "transactionManager";

	private static final String TRANSACTION_ISOLATION = "TRANSACTION_READ_COMMITTED";

	/**
	 * @return JdbcTemplate for managed database access
	 */
	public static JdbcTemplate createJdbcTemplate(CustomDataSourceParameters params) {
		return new JdbcTemplate(prepareDatasource(params));
	}

	@SuppressWarnings("unchecked")
	private static DataSource prepareDatasource(CustomDataSourceParameters params) {

		try {
			DriverManager.registerDriver(((Class<Driver>) Class.forName(params.getDriverClassName()))
					.getDeclaredConstructor().newInstance());

			// Prepare standard HikariDataSource
			HikariDataSource kc = (HikariDataSource) DataSourceBuilder.create().url(params.getUrl())
					.driverClassName(params.getDriverClassName()).username(params.getUsername())
					.password(params.getPassword()).build();

			// Prepare pool configuration and other params
			kc.setTransactionIsolation(TRANSACTION_ISOLATION);
			kc.setAutoCommit(false);
			
			// In case of active connection not recycled, enable this to check where they are used
			// kc.setLeakDetectionThreshold(5000);
			
			kc.setConnectionInitSql(params.getConnectionTestQuery());
			kc.setConnectionTestQuery(params.getConnectionTestQuery());
			kc.setConnectionTimeout(1000 * params.getTimeout());
			kc.setMinimumIdle(params.getMinimumIdle());
			kc.setMaximumPoolSize(params.getMaxPoolSize());

			LOGGER.debug("[DATASOURCE] Datasource initialized with driver {}, url {}, username {}",
					params.getDriverClassName(), params.getUrl(), params.getUsername());

			return kc;
		} catch (InstantiationException | IllegalAccessException | ClassNotFoundException | SQLException
				| IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException e) {
			LOGGER.error("[DATASOURCE] Error on datasource init. Check configuration properties from datagate-efluid.managed-datasource", e);
			throw new ApplicationException(ErrorType.WRONG_DS_TYPE,
					"Cannot init Driver " + params.getDriverClassName(), e);
		}
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
		private String connectionTestQuery;
		private int minimumIdle;
		private int maxPoolSize;
		private int timeout;
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
		 * @return the connectionTestQuery
		 */
		public String getConnectionTestQuery() {
			return this.connectionTestQuery;
		}

		/**
		 * @param connectionTestQuery
		 *            the connectionTestQuery to set
		 */
		public void setConnectionTestQuery(String connectionTestQuery) {
			this.connectionTestQuery = connectionTestQuery;
		}

		/**
		 * @return the minimumIdle
		 */
		public int getMinimumIdle() {
			return this.minimumIdle;
		}

		/**
		 * @param minimumIdle
		 *            the minimumIdle to set
		 */
		public void setMinimumIdle(int minimumIdle) {
			this.minimumIdle = minimumIdle;
		}

		/**
		 * @return the maxPoolSize
		 */
		public int getMaxPoolSize() {
			return this.maxPoolSize;
		}

		/**
		 * @param maxPoolSize
		 *            the maxPoolSize to set
		 */
		public void setMaxPoolSize(int maxPoolSize) {
			this.maxPoolSize = maxPoolSize;
		}

		/**
		 * @return the timeout
		 */
		public int getTimeout() {
			return this.timeout;
		}

		/**
		 * @param timeout
		 *            the timeout to set
		 */
		public void setTimeout(int timeout) {
			this.timeout = timeout;
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
		 * @param query
		 *            the query to set
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

		private String databaseDateFormat;

		private boolean joinOnNullableKeys;

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

		/**
		 * @return the databaseDateFormat
		 */
		@Override
		public String getDatabaseDateFormat() {
			return this.databaseDateFormat;
		}

		/**
		 * @param databaseDateFormat
		 *            the databaseDateFormat to set
		 */
		public void setDatabaseDateFormat(String databaseDateFormat) {
			this.databaseDateFormat = databaseDateFormat;
		}

		@Override
		public boolean isJoinOnNullableKeys() {
			return this.joinOnNullableKeys;
		}

		public void setJoinOnNullableKeys(boolean joinOnNullableKeys) {
			this.joinOnNullableKeys = joinOnNullableKeys;
		}
	}
}
