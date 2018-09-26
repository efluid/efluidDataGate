package fr.uem.efluid.system.common;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import fr.uem.efluid.utils.ApplicationException;
import fr.uem.efluid.utils.Associate;
import fr.uem.efluid.utils.ErrorType;

/**
 * @author elecomte
 * @version 1
 * @since v0.0.8
 */
final class DataSetHelper {

	static final Map<String, String> REV_SITEMAP = loadPageMap("sitemap");

	static final Map<String, String> REV_TEMPLATES = loadPageMap("templates");
	
	static final Map<String, UserDef> USERDEF = loadUsers();

	/**
	 * <p>
	 * Load configuration of sitemap
	 * </p>
	 *
	 * @return
	 */
	private static Map<String, String> loadPageMap(String name) {
		try {
			// Mapping is "link => X names"
			Map<String, String[]> siteMap = new ObjectMapper().readValue(
					new File("src/test/resources/datasets/" + name + ".json"), new TypeReference<Map<String, String[]>>() {
						// Nope
					});

			// Revert for easy use (name => link)
			return siteMap.entrySet().stream()
					.flatMap(e -> Stream.of(e.getValue()).map(v -> Associate.of(e.getKey(), v)))
					.collect(Collectors.toMap(Associate::getTwo, Associate::getOne));
		} catch (IOException e) {
			throw new ApplicationException(ErrorType.OTHER, "Error on test " + name + ".json", e);
		}
	}

	/**
	 * <p>
	 * Load configuration of sitemap
	 * </p>
	 *
	 * @return
	 */
	private static Map<String, UserDef> loadUsers() {
		try {
			return new ObjectMapper().readValue(
					new File("src/test/resources/datasets/users.json"), new TypeReference<Map<String, UserDef>>() {
						// Nope
					});
		} catch (IOException e) {
			throw new ApplicationException(ErrorType.OTHER, "Error on test user.json", e);
		}
	}

	static class UserDef {

		private String login;
		private String email;
		private String password;

		public UserDef() {
			// Default init
		}

		public String getPassword() {
			return this.password;
		}

		public void setPassword(String password) {
			this.password = password;
		}

		public String getEmail() {
			return this.email;
		}

		public void setEmail(String email) {
			this.email = email;
		}

		public String getLogin() {
			return this.login;
		}

		public void setLogin(String login) {
			this.login = login;
		}
	}

}
