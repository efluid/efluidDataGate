package fr.uem.efluid.security;

import javax.annotation.PostConstruct;

import org.pac4j.core.context.WebContext;
import org.pac4j.core.credentials.TokenCredentials;
import org.pac4j.core.credentials.authenticator.Authenticator;
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
 * @author elecomte
 * @since v0.0.1
 * @version 1
 */
@Component
public class RestTokenAuthenticator implements Authenticator<TokenCredentials> {

	private static final Logger LOGGER = LoggerFactory.getLogger(RestTokenAuthenticator.class);

	@Autowired
	private UserRepository users;

	@Override
	public void validate(final TokenCredentials credentials, final WebContext context) throws HttpAction, CredentialsException {

		LOGGER.debug("Begin rest (token) authentication");

		if (credentials == null) {
			throw new CredentialsException("credentials must not be null");
		}

		if (CommonHelper.isBlank(credentials.getToken())) {
			throw new CredentialsException("token must not be blank");
		}

		final String token = credentials.getToken();

		User user = this.users.findByToken(token);

		if (user == null) {
			throw new CredentialsException("Username doesn't exist");
		}

		LOGGER.debug("Authentication successfull for user {}", user.getLogin());

		credentials.setUserProfile(WebUtils.webSecurityProfileFromUser(user));
	}

	@PostConstruct
	public void signalLoading() {
		LOGGER.debug("[SECURITY] Load rest authenticator {}", this.getClass().getName());
	}
}
