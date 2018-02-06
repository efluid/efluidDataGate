package fr.uem.efluid;

import static fr.uem.efluid.Application.Packages.*;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * Init class. Root config holder
 *
 * @author elecomte
 * @since v0.0.1
 * @version 1
 */
@EnableAutoConfiguration
@EnableTransactionManagement
@EnableJpaRepositories(REPOSITORIES)
@EntityScan(ENTITIES)
@ComponentScan({ CONFIG, SERVICES, WEB, REPOSITORIES_IMPLS, TOOLS })
@EnableCaching
@Configuration
public class Application {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		SpringApplication.run(Application.class, args);
	}

	/**
	 * Packages to inspect for ctxt init
	 */
	interface Packages {
		String ROOT = "fr.uem.efluid";
		String REPOSITORIES = ROOT + ".model.repositories";
		String REPOSITORIES_IMPLS = REPOSITORIES + ".impls";
		String ENTITIES = ROOT + ".model.entities";
		String SERVICES = ROOT + ".services";
		String WEB = ROOT + ".web";
		String CONFIG = ROOT + ".config";
		String TOOLS = ROOT + ".tools";
	}
}
