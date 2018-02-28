package fr.uem.efluid.config;

import static fr.uem.efluid.utils.DataGenerationUtils.domain;
import static fr.uem.efluid.utils.DataGenerationUtils.user;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.transaction.annotation.Transactional;

import fr.uem.efluid.model.repositories.FunctionalDomainRepository;
import fr.uem.efluid.model.repositories.UserRepository;

/**
 * <p>
 * Create some minimal values for easy import testing
 * </p>
 * 
 * @author elecomte
 * @since v0.0.1
 * @version 1
 */
@Configuration
@Profile("minimal")
@Transactional
public class MinimalDataLoader {

	private static final Logger LOGGER = LoggerFactory.getLogger(MinimalDataLoader.class);

	@Autowired
	private FunctionalDomainRepository domains;

	@Autowired
	private UserRepository users;

	@PostConstruct
	public void initValues() {

		LOGGER.info("[MINIMAL] Init Minimal values for testing");

		this.users.save(user("minimal"));
		this.domains.save(domain("Defaut"));

		LOGGER.info("[MINIMAL] Minimal values init done");
	}

}
