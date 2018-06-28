package fr.uem.efluid.config;

import static fr.uem.efluid.utils.DataGenerationUtils.domain;
import static fr.uem.efluid.utils.DataGenerationUtils.project;
import static fr.uem.efluid.utils.DataGenerationUtils.user;

import javax.annotation.PostConstruct;

import org.pac4j.core.credentials.password.PasswordEncoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.transaction.annotation.Transactional;

import fr.uem.efluid.model.entities.Project;
import fr.uem.efluid.model.repositories.FunctionalDomainRepository;
import fr.uem.efluid.model.repositories.ProjectRepository;
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
	private ProjectRepository projects;

	@Autowired
	private FunctionalDomainRepository domains;

	@Autowired
	private UserRepository users;

	@Autowired
	private PasswordEncoder encoder;

	@PostConstruct
	public void initValues() {

		LOGGER.info("[MINIMAL] Init Minimal values for testing. User \"minimal\" with password / token equals to login");

		Project proj1 = this.projects.save(project("Default"));

		this.users.save(user("minimal", this.encoder));
		this.domains.save(domain("Defaut", proj1));

		LOGGER.warn("[MINIMAL] Minimal values init done. Not OK in production environment !!!");
	}

}
