package fr.uem.efluid.security;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import fr.uem.efluid.model.entities.User;

/**
 * <p>
 * Basic holder of private User model. Available on all services
 * </p>
 * 
 * @author elecomte
 * @since v0.0.1
 * @version 1
 */
@Component
public class UserHolder {

	private static final Logger LOGGER = LoggerFactory.getLogger(UserHolder.class);

	private ThreadLocal<User> currentUser = new ThreadLocal<>();

	private User wizardUser;

	/**
	 * @param user
	 */
	public void setCurrentUser(User user) {

		if (user == null) {
			LOGGER.debug("Drop active user ");
			this.currentUser.remove();
		} else {
			LOGGER.debug("Apply active user {}", user.getLogin());
			this.currentUser.set(user);
		}
	}

	/**
	 * @return
	 */
	public User getCurrentUser() {
		return this.wizardUser != null ? this.wizardUser : this.currentUser.get();
	}

	/**
	 * @param user
	 */
	public void setWizzardUser(User user) {
		if (user == null) {
			LOGGER.debug("Drop active wizard user");
		} else {
			LOGGER.debug("Apply active wizard user {}", user.getLogin());
		}
		this.wizardUser = user;
	}

	@PostConstruct
	public void signalLoading() {
		LOGGER.debug("[SECURITY] User Holder {}", this.getClass().getName());
	}
}
