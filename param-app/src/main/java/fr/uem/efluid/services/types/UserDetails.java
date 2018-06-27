package fr.uem.efluid.services.types;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

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

	private final List<ProjectData> preferedProjects;

	/**
	 * @param login
	 * @param email
	 * @param token
	 */
	public UserDetails(String login, String email, String token, List<ProjectData> preferedProjects) {
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
	public List<ProjectData> getPreferedProjects() {
		return this.preferedProjects;
	}

	/**
	 * @return easy to display rendering of project list
	 */
	public String getPreferedProjectsRender() {
		return this.preferedProjects.stream().map(ProjectData::getName).collect(Collectors.joining(", "));
	}

	/**
	 * <p>
	 * For easy checked selection in user edit
	 * </p>
	 * 
	 * @param uuid
	 * @return
	 */
	public boolean isPrefered(UUID uuid) {
		return this.preferedProjects.stream().anyMatch(p -> p.getUuid().equals(uuid));
	}

	/**
	 * @param user
	 * @return
	 */
	public static UserDetails fromEntity(User user) {
		return new UserDetails(user.getLogin(), user.getEmail(), user.getToken(),
				user.getPreferedProjects().stream().map(ProjectData::fromEntity).collect(Collectors.toList()));
	}

}
