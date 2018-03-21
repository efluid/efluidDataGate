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
	private PasswordEncoder encoder;

	/**
	 * @param login
	 * @param email
	 * @param password
	 */
	@CacheEvict(cacheNames = "users", allEntries = true)
	public void addSimpleUser(String login, String email, String password) {

		LOGGER.info("Creation new user : {}", login);

		User user = new User();
		user.setLogin(login);
		user.setPassword(this.encoder.encode(password));
		user.setEmail(email);
		user.setToken(generateToken());

		this.users.save(user);
	}

	/**
	 * @return
	 */
	public UserDetails getCurrentUserDetails() {

		User freshUser = this.users.getOne(getCurrentUser().getLogin());

		return UserDetails.fromEntity(freshUser);
	}

	/**
	 * @return
	 */
	private static final String generateToken() {

		return UUID.randomUUID().toString().replaceAll("-", "");
	}
}
