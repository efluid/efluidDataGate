package fr.uem.efluid;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import static fr.uem.efluid.Application.Packages.*;

/**
 * Init class. Root config holder
 *
 * @author elecomte
 * @version 1
 */
@EnableAutoConfiguration
@EnableTransactionManagement
@EnableJpaRepositories(JPA_REPOSITORIES)
@EntityScan(JPA_ENTITIES)
@ComponentScan({SERVICES, WEB})
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
        String JPA_REPOSITORIES = ROOT + ".repositories";
        String JPA_ENTITIES = ROOT + ".model";
        String SERVICES = ROOT + ".services";
        String WEB = ROOT + ".web";
    }
}
