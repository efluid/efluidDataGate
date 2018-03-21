package fr.uem.efluid.security;

import java.util.List;

import javax.annotation.PostConstruct;

import org.pac4j.core.authorization.authorizer.ProfileAuthorizer;
import org.pac4j.core.context.WebContext;
import org.pac4j.core.exception.HttpAction;
import org.pac4j.core.profile.CommonProfile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import fr.uem.efluid.model.entities.User;

/**
 * @author elecomte
 * @since v0.0.1
 * @version 1
 */
@Component
public class AllAuthorizer extends ProfileAuthorizer<CommonProfile> {

	private static final Logger LOGGER = LoggerFactory.getLogger(AllAuthorizer.class);

	@Autowired
	private UserHolder users;

	@Override
	public boolean isAuthorized(final WebContext context, final List<CommonProfile> profiles) throws HttpAction {
		return isAnyAuthorized(context, profiles);
	}

	@Override
	public boolean isProfileAuthorized(final WebContext context, final CommonProfile profile) {

		if (profile == null) {
			return false;
		}

		LOGGER.debug("Apply user {} / {}", profile.getId(), profile.getEmail());

		this.users.setCurrentUser(new User() {

			/**
			 * @return
			 * @see fr.uem.efluid.model.entities.User#getLogin()
			 */
			@Override
			public String getLogin() {
				return profile.getId();
			}

			/**
			 * @return
			 * @see fr.uem.efluid.model.entities.User#getEmail()
			 */
			@Override
			public String getEmail() {
				return profile.getEmail();
			}

		});

		// All authorized
		return profile.getUsername() != null;
	}

	@PostConstruct
	public void signalLoading() {
		LOGGER.debug("[SECURITY] Load authorizer {}", this.getClass().getName());
	}
}