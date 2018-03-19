package fr.uem.efluid.services;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;

import fr.uem.efluid.model.entities.User;
import fr.uem.efluid.model.repositories.UserRepository;

/**
 * @author elecomte
 * @since v0.0.1
 * @version 1
 */
public abstract class AbstractApplicationService {

	@Autowired
	private UserRepository users;

	/**
	 * @return
	 */
	@Cacheable("users")
	protected User getCurrentUser() {

		// TODO : temp system for basic access. Replaced by spring sec.

		List<User> found = this.users.findAll();

		return found != null && !found.isEmpty() ? found.get(0) : null;
	}

	/**
	 * @param value
	 * @return
	 */
	protected static boolean isNotEmpty(String value) {
		if (value == null) {
			return false;
		}
		return value.length() > 0;
	}
}
