package fr.uem.efluid.services.types;

import java.util.List;
import java.util.stream.Collectors;

import fr.uem.efluid.model.entities.Project;
import fr.uem.efluid.model.entities.User;

/**
 * <p>
 * Shared model for user content. Provides basic features for user content
 * </p>
 * 
 * @author elecomte
 * @since v0.0.1
 * @version 2
 */
public class UserDetails {

	private final String login;

	private final String email;

	private final String token;

	private final List<String> preferedProjects;

	/**
	 * @param login
	 * @param email
	 * @param token
	 */
	public UserDetails(String login, String email, String token, List<String> preferedProjects) {
		super();
		this.login = login;
		this.email = email;
		this.token = token;
		this.preferedProjects = preferedProjects;
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
	 * @return the preferedProjectUuids
	 */
	public List<String> getPreferedProjects() {
		return this.preferedProjects;
	}

	/**
	 * @param user
	 * @return
	 */
	public static UserDetails fromEntity(User user) {
		return new UserDetails(user.getLogin(), user.getEmail(), user.getToken(),
				user.getPreferedProjects().stream().map(Project::getName).collect(Collectors.toList()));
	}

}
