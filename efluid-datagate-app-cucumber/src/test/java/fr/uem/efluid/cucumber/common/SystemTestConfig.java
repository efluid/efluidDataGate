package fr.uem.efluid.cucumber.common;

import fr.uem.efluid.config.BusinessServiceConfig;
import fr.uem.efluid.cucumber.stubs.TweakedAsyncDriver;
import fr.uem.efluid.cucumber.stubs.TweakedIndexDisplayConfig;
import fr.uem.efluid.cucumber.stubs.TweakedPreparationUpdater;
import fr.uem.efluid.security.providers.DatabaseOnlyAccountProvider;
import fr.uem.efluid.tools.AsyncDriver;
import fr.uem.efluid.tools.diff.ManagedQueriesGenerator;
import fr.uem.efluid.tools.diff.ManagedQueriesGenerator.QueryGenerationRules;
import fr.uem.efluid.transformers.TransformerValueProvider;
import fr.uem.efluid.utils.DatasourceUtils;
import fr.uem.efluid.utils.DatasourceUtils.CustomDataSourceParameters;
import fr.uem.efluid.utils.FormatUtils;
import org.h2.server.web.WebServer;
import org.h2.tools.Server;
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
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;
import java.sql.SQLException;
import java.time.LocalDateTime;

import static fr.uem.efluid.Application.Packages.*;
import static fr.uem.efluid.cucumber.common.SystemTestConfig.SYS_TEST_ROOT;

/**
 * <p>
 * Default config for fully contextualised tests
 * </p>
 *
 * @author elecomte
 * @version 2
 * @since v0.0.1
 */
@TestConfiguration
@ComponentScan({CONFIG, SERVICES, REPOSITORIES_IMPLS, TOOLS, TRANSFORMERS, WEB, REST, SYS_TEST_ROOT + ".stubs"})
@ConfigurationProperties(prefix = "datagate-efluid.managed-datasource")
@EnableTransactionManagement
@EnableJpaRepositories({REPOSITORIES, SYS_TEST_ROOT + ".stubs.repositories"})
@EntityScan({ENTITIES, CONVERTERS, SYS_TEST_ROOT + ".stubs.entities"})
public class SystemTestConfig {

    static final String SYS_TEST_ROOT = ROOT + ".cucumber";

    private static final Logger LOGGER = LoggerFactory.getLogger(SystemTestConfig.class);

    @Autowired
    private CustomDataSourceParameters dsParams;

    @Autowired
    private DataSource defaultDataSource;

    @Autowired
    private EntityManagerFactory defaultEntityManagerFactory;

    @Autowired
    private ManagedQueriesGenerator queryGenerator;

    @Bean
    public JdbcTemplate managedDatabaseJdbcTemplate() {
        return new JdbcTemplate(this.defaultDataSource);
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
     * @return injected TM for managed db, simulated with JPA
     */
    @Bean(name = DatasourceUtils.MANAGED_TRANSACTION_MANAGER)
    public PlatformTransactionManager managedTransactionManager() {
        return new DataSourceTransactionManager(this.defaultDataSource);
    }

    /**
     * @return default TM for core features
     */
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
     * @return configured filter to control session content
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

    /**
     * <p>
     * For system tests, use a specific "programable" async driver
     * </p>
     *
     * @return adapted asynchronous driver
     */
    @Bean
    public AsyncDriver futureAsyncDriver() {
        return new TweakedAsyncDriver();
    }

    /**
     * To allow some post-init changes in preparation data, BEFORE run of diff / merge
     *
     * @return
     */
    @Bean
    public TweakedPreparationUpdater tweakedPreparationUpdater() {
        return new TweakedPreparationUpdater();
    }

    /**
     * Non primary AccountProvider for database access use
     *
     * @return fixed DB account provider (default provider)
     */
    @Bean
    public DatabaseOnlyAccountProvider databaseOnlyAccountProvider() {
        return new DatabaseOnlyAccountProvider();
    }

    /**
     * Live switchable provider, used as primary for testing
     *
     * @return account provider which can be updated live on testing
     */
    @Bean
    @Primary
    public SwitchableLdapAccountProvider switchableAccountProvider() {
        return new SwitchableLdapAccountProvider(databaseOnlyAccountProvider());
    }

    /**
     * properties for diff index service, with editable variables
     * @param original
     * @return
     */
    @Bean
    @Primary
    public TweakedIndexDisplayConfig tweakedIndexDisplayConfig(BusinessServiceConfig.IndexDisplayConfigProperties original){
        TweakedIndexDisplayConfig config = new TweakedIndexDisplayConfig();
        config.setCombineSimilarDiffAfter(original.getCombineSimilarDiffAfter());
        config.setDetailsIndexMax(original.getDetailsIndexMax());
        return config;
    }


    /**
     * Customized provider of transformation values for testing
     * (override for example the "current_date" value to use a fixed one)
     *
     * @return override provider for test needs
     */
    @Bean
    @Primary
    public TransformerValueProvider fixedTransformerValueProvider() {
        return new TransformerValueProvider(this.queryGenerator) {
            @Override
            public String getFormatedCurrentTime() {
                return FormatUtils.format(LocalDateTime.of(2020, 06, 12, 22, 14));
            }
        };
    }
}
