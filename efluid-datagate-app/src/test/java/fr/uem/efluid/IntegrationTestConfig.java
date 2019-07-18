package fr.uem.efluid;

import static fr.uem.efluid.Application.Packages.CONFIG;
import static fr.uem.efluid.Application.Packages.CONVERTERS;
import static fr.uem.efluid.Application.Packages.ENTITIES;
import static fr.uem.efluid.Application.Packages.REPOSITORIES;
import static fr.uem.efluid.Application.Packages.REPOSITORIES_IMPLS;
import static fr.uem.efluid.Application.Packages.ROOT;
import static fr.uem.efluid.Application.Packages.SERVICES;
import static fr.uem.efluid.Application.Packages.TOOLS;

import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Primary;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;

import fr.uem.efluid.tools.AsyncDriver;
import fr.uem.efluid.tools.FutureAsyncDriver;
import fr.uem.efluid.tools.ManagedQueriesGenerator.QueryGenerationRules;
import fr.uem.efluid.utils.DatasourceUtils;
import fr.uem.efluid.utils.DatasourceUtils.CustomDataSourceParameters;

/**
 * <p>
 * Default config for fully contextualised tests
 * </p>
 *
 * @author elecomte
 * @version 1
 * @since v0.0.1
 */
@EnableJpaRepositories({REPOSITORIES, ROOT + ".stubs"})
@EntityScan({ENTITIES, CONVERTERS, ROOT + ".stubs"})
@ComponentScan({CONFIG, SERVICES, REPOSITORIES_IMPLS, TOOLS, ROOT + ".stubs"})
@TestConfiguration
@ConfigurationProperties(prefix = "datagate-efluid.managed-datasource")
public class IntegrationTestConfig extends CustomDataSourceParameters {

    private static final Logger LOGGER = LoggerFactory.getLogger(IntegrationTestConfig.class);

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
                rules.isColumnNamesProtected(), rules.isTableNamesProtected());
        return rules;
    }

    /**
     * <p>
     * For integration tests, use standard async model
     * </p>
     *
     * @return
     */
    @Bean
    public AsyncDriver futureAsyncDriver() {
        return new FutureAsyncDriver(4, 5000, 200);
    }
}
