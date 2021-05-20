package fr.uem.efluid;

import fr.uem.efluid.tools.AsyncDriver;
import fr.uem.efluid.tools.FutureAsyncDriver;
import fr.uem.efluid.tools.ManagedQueriesGenerator.QueryGenerationRules;
import fr.uem.efluid.utils.DatasourceUtils;
import fr.uem.efluid.utils.DatasourceUtils.CustomDataSourceParameters;
import org.h2.server.web.WebServer;
import org.h2.tools.Server;
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

import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;

import java.sql.SQLException;

import static fr.uem.efluid.Application.Packages.*;

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
public class IntegrationTestConfig {

    private static final Logger LOGGER = LoggerFactory.getLogger(IntegrationTestConfig.class);

    @Autowired
    private DataSource defaultDataSource;

    @Autowired
    private CustomDataSourceParameters dsParams;

    @Autowired
    private EntityManagerFactory defaultEntityManagerFactory;

    @Bean
    public JdbcTemplate managedDatabaseJdbcTemplate() {
        return new JdbcTemplate(this.defaultDataSource);
    }

    @Bean(name = DatasourceUtils.MANAGED_TRANSACTION_MANAGER)
    public PlatformTransactionManager managedTransactionManager() {
        return new DataSourceTransactionManager(this.defaultDataSource);
    }

    @Bean(name = DatasourceUtils.DEFAULT_TRANSACTION_MANAGER)
    @Primary
    public PlatformTransactionManager defaultTransactionManager() {
        return new JpaTransactionManager(this.defaultEntityManagerFactory);
    }

    @Bean
    public QueryGenerationRules managedQueryGenerationRules() {
        // Use local query config directly
        QueryGenerationRules rules = this.dsParams.getQuery();
        LOGGER.info("[MANAGED DB] Using these query generation rules : columnProtected:{}, tableProtected:{}",
                rules.isColumnNamesProtected(), rules.isTableNamesProtected());
        return rules;
    }

    /**
     * @return Active H2 server instance
     * @throws SQLException for server init error
     */
    @Bean(initMethod = "start", destroyMethod = "stop")
    public Server h2WebConsole() throws SQLException {
        LOGGER.info("H2 CONSOLE activated on http://localhost:8082");
        return new Server(new WebServer(), "-web", "-webAllowOthers", "-webPort", "8082");
    }

    /**
     * <p>
     * For integration tests, use standard async model
     * </p>
     */
    @Bean
    public AsyncDriver futureAsyncDriver() {
        return new FutureAsyncDriver(4, 5000, 200);
    }
}
