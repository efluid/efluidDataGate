package fr.uem.efluid.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.jdbc.core.JdbcTemplate;

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
public class ManagedDatasourceConfig extends CustomDataSourceParameters {

	private static final Logger LOGGER = LoggerFactory.getLogger(ManagedDatasourceConfig.class);

	@Bean
	public JdbcTemplate managedDatabaseJdbcTemplate() {
		LOGGER.info("[MANAGED DB] Init access to managed DB {}", this.getUrl());
		return DatasourceUtils.createJdbcTemplate(this);
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
