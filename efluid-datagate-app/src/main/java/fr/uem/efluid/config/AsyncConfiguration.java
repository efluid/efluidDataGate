package fr.uem.efluid.config;

import fr.uem.efluid.tools.AsyncDriver;
import fr.uem.efluid.tools.FutureAsyncDriver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

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

    @Bean
    public AsyncDriver futureAsyncDriver() {

        // Check timeout every 5 seconds
        return new FutureAsyncDriver(
                this.properties.getThreadPoolSize(),
                this.properties.getTimeoutSeconds() * 1000,
                5000);
    }

    @ConfigurationProperties(prefix = "datagate-efluid.async-preparation")
    public static class AsyncPreparationProperties {

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
    }
}
