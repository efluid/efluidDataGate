package fr.uem.efluid.config;

import com.zaxxer.hikari.HikariDataSource;
import fr.uem.efluid.tools.AsyncDriver;
import fr.uem.efluid.tools.FutureAsyncDriver;
import fr.uem.efluid.tools.ManagerDbPoolConstrainedDriver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 * <p>
 * Common configuration for asynchronous processes
 * </p>
 *
 * @author elecomte
 * @version 1
 * @since v2.0.0
 */
@Configuration
@Profile("!test")
@EnableConfigurationProperties(AsyncConfiguration.AsyncPreparationProperties.class)
public class AsyncConfiguration {

    @Autowired
    private AsyncPreparationProperties properties;

    @Autowired
    private JdbcTemplate manager;

    @Bean
    public AsyncDriver futureAsyncDriver() {

        if (this.properties.getStrategy() == AsyncStrategy.DB_POOL) {

            return new ManagerDbPoolConstrainedDriver(
                    (HikariDataSource) this.manager.getDataSource(),
                    this.properties.getTimeoutSeconds() * 1000L,
                    5000
            );
        }

        // Check timeout every 5 seconds
        return new FutureAsyncDriver(
                this.properties.getThreadPoolSize(),
                this.properties.getTimeoutSeconds() * 1000L,
                5000);
    }

    @ConfigurationProperties(prefix = "datagate-efluid.async-preparation")
    public static class AsyncPreparationProperties {

        private AsyncStrategy strategy;

        private int threadPoolSize;

        private int timeoutSeconds;

        public int getThreadPoolSize() {
            return threadPoolSize;
        }

        public void setThreadPoolSize(int threadPoolSize) {
            this.threadPoolSize = threadPoolSize;
        }

        public int getTimeoutSeconds() {
            return timeoutSeconds;
        }

        public void setTimeoutSeconds(int timeoutSeconds) {
            this.timeoutSeconds = timeoutSeconds;
        }

        public AsyncStrategy getStrategy() {
            return strategy;
        }

        public void setStrategy(AsyncStrategy strategy) {
            this.strategy = strategy;
        }
    }

    public enum AsyncStrategy {
        THREAD_POOL, DB_POOL
    }
}
