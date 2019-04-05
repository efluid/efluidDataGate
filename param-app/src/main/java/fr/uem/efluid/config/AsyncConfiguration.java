package fr.uem.efluid.config;

import fr.uem.efluid.tools.AsyncDriver;
import fr.uem.efluid.tools.FutureAsyncDriver;
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
public class AsyncConfiguration {

    @Bean
    public AsyncDriver futureAsyncDriver() {
        return new FutureAsyncDriver(4);
    }
}
