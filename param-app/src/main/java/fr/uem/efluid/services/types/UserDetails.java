package fr.uem.efluid.services.types;

import fr.uem.efluid.model.entities.User;

/**
 * <p>
 * Shared model for user content. Provides basic features for user content
 * </p>
 * 
 * @author elecomte
 * @since v0.0.1
 * @version 1
 */
public class UserDetails {

	private final String login;

	private final String email;

	private final String token;

	/**
	 * @param login
	 * @param email
	 * @param token
	 */
	public UserDetails(String login, String email, String token) {
		super();
		this.login = login;
		this.email = email;
		this.token = token;
	}

	/**
	 * @return the login
	 */
	public String getLogin() {
		return this.login;
	}

	/**
	 * @return the email
	 */
	public String getEmail() {
		return this.email;
	}

	/**
	 * @return the token
	 */
	public String getToken() {
		return this.token;
	}

	/**
	 * @param user
	 * @return
	 */
	public static UserDetails fromEntity(User user) {
		return new UserDetails(user.getLogin(), user.getEmail(), user.getToken());
	}

}
