package fr.uem.efluid.config;

import javax.persistence.EntityManagerFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;

import fr.uem.efluid.tools.ManagedQueriesGenerator.QueryGenerationRules;
import fr.uem.efluid.utils.DatasourceUtils;
import fr.uem.efluid.utils.DatasourceUtils.CustomDataSourceParameters;

/**
 * @author elecomte
 * @since v0.0.1
 * @version 1
 */
@Configuration
@Order(10)
@ConfigurationProperties(prefix = "param-efluid.managed-datasource")
@Profile("!test")
public class ManagedDatasourceConfig extends CustomDataSourceParameters {

	private static final Logger LOGGER = LoggerFactory.getLogger(ManagedDatasourceConfig.class);

	@Autowired
	private EntityManagerFactory defaultEntityManagerFactory;

	@Bean
	public JdbcTemplate managedDatabaseJdbcTemplate() {
		LOGGER.info("[MANAGED DB] Init access to managed DB {}", this.getUrl());
		return DatasourceUtils.createJdbcTemplate(this);
	}

	/**
	 * @return
	 */
	@Bean(name = DatasourceUtils.MANAGED_TRANSACTION_MANAGER)
	public PlatformTransactionManager managedTransactionManager() {
		return new DataSourceTransactionManager(managedDatabaseJdbcTemplate().getDataSource());
	}

	/**
	 * @return
	 */
	@Bean(name = DatasourceUtils.DEFAULT_TRANSACTION_MANAGER)
	@Primary
	public PlatformTransactionManager defaultTransactionManager() {
		return new JpaTransactionManager(this.defaultEntityManagerFactory);
	}

	@Bean
	public QueryGenerationRules managedQueryGenerationRules() {
		// Use local query config directly
		QueryGenerationRules rules = this.getQuery();
		LOGGER.info("[MANAGED DB] Using these query generation rules : columnProtected:{}, tableProtected:{}",
				Boolean.valueOf(rules.isColumnNamesProtected()), Boolean.valueOf(rules.isTableNamesProtected()));
		return rules;
	}
}
