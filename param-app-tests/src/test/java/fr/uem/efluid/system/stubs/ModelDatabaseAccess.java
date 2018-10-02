package fr.uem.efluid.system.stubs;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

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
 * A common component for init and use of app model (the database used for behaviors of the app)
 * </p>
 * 
 * @author elecomte
 * @since v0.0.8
 * @version 1
 */
@Component
@SuppressWarnings("unused")
public class ModelDatabaseAccess {

	private static final Logger LOGGER = LoggerFactory.getLogger(ModelDatabaseAccess.class);

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
	public void initWizzardData(final User user, final Project initProject, final List<FunctionalDomain> addDomains) {

		LOGGER.info("[MODEL-INIT] Setup wizzard resulting data");

		Project project = this.projects.save(initProject);
		this.projects.flush();

		Set<Project> prefered = new HashSet<>();
		prefered.add(project);
		// Like wizzard, preset default project
		user.setPreferedProjects(prefered);
		user.setSelectedProject(project);

		// User pwd is encoded
		user.setPassword(this.encoder.encode(user.getPassword()));

		this.holder.setWizzardUser(this.users.save(user));
		this.users.flush();

		addDomains.forEach(d -> {
			d.setProject(project);
			this.domains.save(d);
		});
		this.domains.flush();
	}

	/**
	 * @param project
	 */
	public void initDictionary(Project project) {
		// TODO
	}

	/**
	 * @param name
	 * @return
	 */
	public FunctionalDomain findDomainByName(String name) {
		return this.domains.findByName(name);
	}

}
