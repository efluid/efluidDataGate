package fr.uem.efluid.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import fr.uem.efluid.tools.AsyncDriver;
import fr.uem.efluid.tools.FutureAsyncDriver;

/**
 * <p>
 * Common configuration for asynchronous processes
 * </p>
 * 
 * @author elecomte
 * @since v2.0.0
 * @version 1
 */
@Configuration
@Profile("!test")
public class AsyncConfiguration {

	@Bean
	public AsyncDriver futureAsyncDriver() {
		return new FutureAsyncDriver(4);
	}
}
