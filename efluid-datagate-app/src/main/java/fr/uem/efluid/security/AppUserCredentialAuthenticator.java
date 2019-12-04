package fr.uem.efluid.security;

import javax.annotation.PostConstruct;

import org.pac4j.core.context.WebContext;
import org.pac4j.core.credentials.UsernamePasswordCredentials;
import org.pac4j.core.credentials.authenticator.Authenticator;
import org.pac4j.core.credentials.password.PasswordEncoder;
import org.pac4j.core.exception.CredentialsException;
import org.pac4j.core.exception.HttpAction;
import org.pac4j.core.util.CommonHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import fr.uem.efluid.model.entities.User;
import fr.uem.efluid.model.repositories.UserRepository;
import fr.uem.efluid.utils.WebUtils;

/**
 * <p>
 * Login / password authen, checking user in repo
 * </p>
 * 
 * @author elecomte
 * @since v0.0.1
 * @version 1
 */
@Component
public class AppUserCredentialAuthenticator implements Authenticator<UsernamePasswordCredentials> {

	private static final Logger LOGGER = LoggerFactory.getLogger(AppUserCredentialAuthenticator.class);

	@Autowired
	private PasswordEncoder encoder;

	@Autowired
	private UserRepository users;

	/**
	 * @see org.pac4j.core.credentials.authenticator.Authenticator#validate(org.pac4j.core.credentials.Credentials,
	 *      org.pac4j.core.context.WebContext)
	 */
	@Override
	public void validate(final UsernamePasswordCredentials credentials, final WebContext context) throws HttpAction, CredentialsException {

		LOGGER.debug("Begin web (form) authentication");

		if (credentials == null) {
			throw new CredentialsException("No credential");
		}

		String username = credentials.getUsername();
		String password = credentials.getPassword();

		if (CommonHelper.isBlank(username)) {
			throw new CredentialsException("Username cannot be blank");
		}

		if (CommonHelper.isBlank(password)) {
			throw new CredentialsException("Password cannot be blank");
		}

		User user = this.users.findByLogin(username);

		if (user == null) {
			throw new CredentialsException("Username doesn't exist");
		}

		if (!this.encoder.matches(password, user.getPassword())) {
			throw new CredentialsException("Password doesn't match");
		}

		LOGGER.debug("Authentication successfull for user {}", user.getLogin());

		credentials.setUserProfile(WebUtils.webSecurityProfileFromUser(user));
	}

	@PostConstruct
	public void signalLoading() {
		LOGGER.debug("[SECURITY] Load web authenticator {}", this.getClass().getName());
	}
}