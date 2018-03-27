package fr.uem.efluid.services;

import java.util.UUID;

import org.pac4j.core.credentials.password.PasswordEncoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import fr.uem.efluid.model.entities.User;
import fr.uem.efluid.model.repositories.UserRepository;
import fr.uem.efluid.security.UserHolder;
import fr.uem.efluid.services.types.UserDetails;

/**
 * @author elecomte
 * @since v0.0.1
 * @version 1
 */
@Service
@Transactional
public class SecurityService extends AbstractApplicationService {

	private static final Logger LOGGER = LoggerFactory.getLogger(SecurityService.class);

	@Autowired
	private UserRepository users;

	@Autowired
	private UserHolder holder;

	@Autowired
	private PasswordEncoder encoder;

	/**
	 * @param login
	 * @param email
	 * @param password
	 */
	@CacheEvict(cacheNames = "users", allEntries = true)
	public void addSimpleUser(String login, String email, String password, boolean fromWizzard) {

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

		this.users.save(user);
	}

	/**
	 * <p>
	 * For rendering of user info
	 * </p>
	 * 
	 * @return
	 */
	public UserDetails getCurrentUserDetails() {

		User freshUser = this.users.getOne(getCurrentUser().getLogin());

		return UserDetails.fromEntity(freshUser);
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
