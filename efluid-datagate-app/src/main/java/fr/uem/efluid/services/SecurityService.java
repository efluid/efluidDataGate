package fr.uem.efluid.services;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.pac4j.core.credentials.password.PasswordEncoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import fr.uem.efluid.model.entities.User;
import fr.uem.efluid.model.repositories.UserRepository;
import fr.uem.efluid.services.types.UserDetails;

/**
 * @author elecomte
 * @since v0.0.1
 * @version 2
 */
@Service
@Transactional
public class SecurityService extends AbstractApplicationService {

	private static final Logger LOGGER = LoggerFactory.getLogger(SecurityService.class);

	@Autowired
	private UserRepository users;

	@Autowired
	private PasswordEncoder encoder;

	@Autowired
	private ProjectManagementService projectService;

	/**
	 * @param login
	 * @param email
	 * @param password
	 */
	@CacheEvict(cacheNames = "users", allEntries = true)
	public User addSimpleUser(String login, String email, String password, boolean fromWizzard) {

		LOGGER.info("Creation new user : {}", login);

		User user = new User();
		user.setLogin(login);
		user.setPassword(this.encoder.encode(password));
		user.setEmail(email);
		user.setToken(generateToken());

		if (fromWizzard) {
			LOGGER.info("New user {} is created in wizzard mode. Set it as current active user for holder, dropped after wizzard complete",
					login);
			this.holder.setWizzardUser(user);
		}

		return this.users.save(user);
	}

	/**
	 * <p>
	 * Create a complete user for user management features. Apply the selected prefered
	 * project in the same time
	 * </p>
	 * 
	 * @param login
	 * @param email
	 * @param password
	 * @param preferedProjects
	 *            Selected prefered projects for the new user
	 */
	public void createUser(String login, String email, String password, List<UUID> preferedProjects) {

		User user = addSimpleUser(login, email, password, false);

		this.projectService.setPreferedProjectsForUser(user, preferedProjects);
	}

	/**
	 * <p>
	 * Create a complete user for user management features. Apply the selected prefered
	 * project in the same time
	 * </p>
	 * 
	 * @param login
	 * @param email
	 * @param preferedProjects
	 *            Selected prefered projects for the new user
	 */
	public void editUser(String login, String email, List<UUID> preferedProjects) {

		User user = this.users.getOne(login);

		// Apply new values
		user.setEmail(email);

		// Edit and save
		this.projectService.setPreferedProjectsForUser(user, preferedProjects);
	}

	/**
	 * <p>
	 * For rendering of user info
	 * </p>
	 * 
	 * @return
	 */
	public UserDetails getCurrentUserDetails() {

		return getUserDetails(getCurrentUser().getLogin());
	}

	/**
	 * <p>
	 * For rendering of user to edit
	 * </p>
	 * 
	 * @param login
	 *            identifier for user to get
	 * @return
	 */
	public UserDetails getUserDetails(String login) {

		User freshUser = this.users.getOne(login);

		return UserDetails.fromEntity(freshUser);
	}

	/**
	 * @return
	 */
	public List<UserDetails> getAllUserDetails() {
		return this.users.findAll().stream()
				.map(UserDetails::fromEntity)
				.collect(Collectors.toList());
	}

	/**
	 * <p>
	 * Called when wizzard process is completed to break wizzard user mode
	 * </p>
	 */
	public void completeWizzardUserMode() {
		LOGGER.info("Wizzard completed. Drop user from holder");
		this.holder.setWizzardUser(null);
	}

	/**
	 * @return
	 */
	private static final String generateToken() {

		return UUID.randomUUID().toString().replaceAll("-", "");
	}
}
