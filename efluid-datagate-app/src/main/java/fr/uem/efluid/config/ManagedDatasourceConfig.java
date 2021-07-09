package fr.uem.efluid.config;

import javax.persistence.EntityManagerFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;

import fr.uem.efluid.config.ManagedDatasourceConfig.DataSourceProperties;
import fr.uem.efluid.tools.diff.ManagedQueriesGenerator.QueryGenerationRules;
import fr.uem.efluid.utils.DatasourceUtils;
import fr.uem.efluid.utils.DatasourceUtils.CustomDataSourceParameters;

/**
 * @author elecomte
 * @since v0.0.1
 * @version 1
 */
@Configuration
@Order(10)
@EnableConfigurationProperties(DataSourceProperties.class)
@Profile("!test")
public class ManagedDatasourceConfig {

	private static final Logger LOGGER = LoggerFactory.getLogger(ManagedDatasourceConfig.class);

	@Autowired
	private DataSourceProperties props;

	@Autowired
	private EntityManagerFactory defaultEntityManagerFactory;

	@Bean
	public JdbcTemplate managedDatabaseJdbcTemplate() {
		LOGGER.info("[MANAGED DB] Init access to managed DB {}", this.props.getUrl());
		return DatasourceUtils.createJdbcTemplate(this.props);
	}

	@Bean(name = DatasourceUtils.MANAGED_TRANSACTION_MANAGER)
	public PlatformTransactionManager managedTransactionManager() {
		return new DataSourceTransactionManager(managedDatabaseJdbcTemplate().getDataSource());
	}

	@Bean(name = DatasourceUtils.DEFAULT_TRANSACTION_MANAGER)
	@Primary
	public PlatformTransactionManager defaultTransactionManager() {
		return new JpaTransactionManager(this.defaultEntityManagerFactory);
	}

	@Bean
	public QueryGenerationRules managedQueryGenerationRules() {
		// Use local query config directly
		QueryGenerationRules rules = this.props.getQuery();
		LOGGER.info("[MANAGED DB] Using these query generation rules : columnProtected:{}, tableProtected:{}",
				Boolean.valueOf(rules.isColumnNamesProtected()), Boolean.valueOf(rules.isTableNamesProtected()));
		return rules;
	}

	/**
	 * @author elecomte
	 * @since v0.0.8
	 * @version 1
	 */
	@ConfigurationProperties(prefix = "datagate-efluid.managed-datasource")
	public static class DataSourceProperties extends CustomDataSourceParameters {
		// No more entries
	}
}
