package fr.uem.efluid.services;

import fr.uem.efluid.model.entities.User;

/**
 * @author elecomte
 * @since v0.0.1
 * @version 1
 */
public abstract class AbstractApplicationService {

	public final static User FAKE_USER = new User();

	static {
		FAKE_USER.setEmail("fake@email.fr");
		FAKE_USER.setLogin("fake");
		FAKE_USER.setPassword("******");
	}

	/**
	 * @return
	 */
	protected User getCurrentUser() {

		// TODO : use spring security for clean user management
		return FAKE_USER;
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
