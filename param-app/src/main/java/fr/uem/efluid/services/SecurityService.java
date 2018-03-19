package fr.uem.efluid.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import fr.uem.efluid.model.entities.User;
import fr.uem.efluid.model.repositories.UserRepository;

/**
 * @author elecomte
 * @since v0.0.1
 * @version 1
 */
@Service
@Transactional
public class SecurityService {

	private static final Logger LOGGER = LoggerFactory.getLogger(SecurityService.class);

	@Autowired
	private UserRepository users;

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
		user.setPassword(password); // Temp : with spr-sec it will be encrypted !!!
		user.setEmail(email);

		this.users.save(user);
	}
}
