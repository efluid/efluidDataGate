package fr.uem.efluid.system.common;

import static fr.uem.efluid.Application.Packages.CONFIG;
import static fr.uem.efluid.Application.Packages.CONVERTERS;
import static fr.uem.efluid.Application.Packages.ENTITIES;
import static fr.uem.efluid.Application.Packages.REPOSITORIES;
import static fr.uem.efluid.Application.Packages.REPOSITORIES_IMPLS;
import static fr.uem.efluid.Application.Packages.ROOT;
import static fr.uem.efluid.Application.Packages.SERVICES;
import static fr.uem.efluid.Application.Packages.TOOLS;
import static fr.uem.efluid.system.common.SystemTestConfig.SYS_TEST_ROOT;

import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Primary;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;

import fr.uem.efluid.tools.ManagedQueriesGenerator.QueryGenerationRules;
import fr.uem.efluid.utils.DatasourceUtils;
import fr.uem.efluid.utils.DatasourceUtils.CustomDataSourceParameters;

/**
 * <p>
 * Default config for fully contextualised tests
 * </p>
 * 
 * @author elecomte
 * @since v0.0.1
 * @version 1
 */
@EnableJpaRepositories({ REPOSITORIES, SYS_TEST_ROOT + ".stubs.repositories" })
@EntityScan({ ENTITIES, CONVERTERS, SYS_TEST_ROOT + ".stubs.entities" })
@ComponentScan({ CONFIG, SERVICES, REPOSITORIES_IMPLS, TOOLS, SYS_TEST_ROOT + ".stubs" })
@TestConfiguration
@ConfigurationProperties(prefix = "param-efluid.managed-datasource")
public class SystemTestConfig extends CustomDataSourceParameters {

	public static final String SYS_TEST_ROOT = ROOT + ".system";

	private static final Logger LOGGER = LoggerFactory.getLogger(SystemTestConfig.class);

	@Autowired
	private DataSource defaultDataSource;

	@Autowired
	private EntityManagerFactory defaultEntityManagerFactory;

	@Bean
	public JdbcTemplate managedDatabaseJdbcTemplate() {
		return new JdbcTemplate(this.defaultDataSource);
	}

	/**
	 * @return
	 */
	@Bean(name = DatasourceUtils.MANAGED_TRANSACTION_MANAGER)
	public PlatformTransactionManager managedTransactionManager() {
		return new DataSourceTransactionManager(this.defaultDataSource);
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

	/**
	 * @return
	 */
	@Bean
	public FilterRegistrationBean<InspectSessionFilter> inspectSessionFilter() {

		LOGGER.info("[INSPECT] Add session filter");

		FilterRegistrationBean<InspectSessionFilter> registration = new FilterRegistrationBean<>();
		registration.setFilter(new InspectSessionFilter());
		registration.setName("inspectSessionFilter");
		registration.setOrder(2);

		return registration;
	}
}
