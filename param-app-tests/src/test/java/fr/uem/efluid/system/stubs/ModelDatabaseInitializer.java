package fr.uem.efluid.system.stubs;

import java.util.List;

import org.pac4j.core.credentials.password.PasswordEncoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import fr.uem.efluid.model.entities.FunctionalDomain;
import fr.uem.efluid.model.entities.Project;
import fr.uem.efluid.model.entities.User;
import fr.uem.efluid.model.repositories.FunctionalDomainRepository;
import fr.uem.efluid.model.repositories.ProjectRepository;
import fr.uem.efluid.model.repositories.UserRepository;
import fr.uem.efluid.security.UserHolder;

/**
 * <p>
 * A common component for init of app model (the database used for behaviors of the app)
 * </p>
 * 
 * @author elecomte
 * @since v0.0.8
 * @version 1
 */
@Component
@SuppressWarnings("unused")
public class ModelDatabaseInitializer {

	private static final Logger LOGGER = LoggerFactory.getLogger(ModelDatabaseInitializer.class);

	@Autowired
	private UserRepository users;

	@Autowired
	private UserHolder holder;

	@Autowired
	private ProjectRepository projects;

	@Autowired
	private FunctionalDomainRepository domains;

	@Autowired
	private PasswordEncoder encoder;

	/**
	 * @param user
	 * @param initProject
	 * @param domain
	 */
	public void initWizzardData(User user, List<Project> initProject, FunctionalDomain domain) {

		LOGGER.info("[MODEL-INIT] Setup wizzard resulting data");

		// User pwd is encoded
		user.setPassword(this.encoder.encode(user.getPassword()));

		this.holder.setWizzardUser(this.users.save(user));
		this.projects.saveAll(initProject);
		this.domains.save(domain);
	}

	public void initDictionary(Project project) {
		// TODO
	}
}
