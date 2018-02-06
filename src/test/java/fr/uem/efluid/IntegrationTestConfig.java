package fr.uem.efluid;

import static fr.uem.efluid.Application.Packages.*;

import java.sql.SQLException;

import org.h2.server.web.WebServer;
import org.h2.tools.Server;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.AdviceMode;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * <p>
 * Default config for fully contextualised tests
 * </p>
 * 
 * @author elecomte
 * @since v0.0.1
 * @version 1
 */
@EnableAutoConfiguration
@EnableTransactionManagement(mode = AdviceMode.ASPECTJ)
@EnableJpaRepositories({ REPOSITORIES, ROOT + ".stubs" })
@EntityScan({ ENTITIES, ROOT + ".stubs" })
@ComponentScan({ CONFIG, SERVICES, WEB, REPOSITORIES_IMPLS, TOOLS, ROOT + ".stubs" })
@Configuration
public class IntegrationTestConfig {

	private static final Logger LOGGER = LoggerFactory.getLogger(IntegrationTestConfig.class);

	/**
	 * @return
	 * @throws SQLException
	 */
	@Bean(initMethod = "start", destroyMethod = "stop")
	public Server h2WebConsole() throws SQLException {
		LOGGER.info("H2 CONSOLE activated on http://localhost:8082");
		return new Server(new WebServer(), "-web", "-webAllowOthers", "-webPort", "8082");
	}

}
