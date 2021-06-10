package fr.uem.efluid.security;

import fr.uem.efluid.model.entities.User;
import fr.uem.efluid.security.providers.AccountProvider;
import fr.uem.efluid.utils.WebUtils;
import org.pac4j.core.context.WebContext;
import org.pac4j.core.context.session.SessionStore;
import org.pac4j.core.credentials.Credentials;
import org.pac4j.core.credentials.TokenCredentials;
import org.pac4j.core.credentials.authenticator.Authenticator;
import org.pac4j.core.exception.CredentialsException;
import org.pac4j.core.exception.http.HttpAction;
import org.pac4j.core.util.CommonHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

/**
 * @author elecomte
 * @version 1
 * @since v0.0.1
 */
@Component
public class RestTokenAuthenticator implements Authenticator {

    private static final Logger LOGGER = LoggerFactory.getLogger(RestTokenAuthenticator.class);

    @Autowired
    private AccountProvider accountProvider;

    @Override
    public void validate(final Credentials credentials, final WebContext context, SessionStore sessionStore) throws HttpAction, CredentialsException {

        LOGGER.debug("Begin rest (token) authentication");

        if (!(credentials instanceof TokenCredentials)) {
            throw new CredentialsException("credentials must not be null");
        }

        TokenCredentials tokenCredentials = (TokenCredentials) credentials;

        if (CommonHelper.isBlank(tokenCredentials.getToken())) {
            throw new CredentialsException("token must not be blank");
        }

        final String token = tokenCredentials.getToken();

        User user = this.accountProvider.findExistingUserByToken(token)
                .orElseThrow(() -> new CredentialsException("Username doesn't exist"));

        LOGGER.debug("Authentication successfull for user {}", user.getLogin());

        credentials.setUserProfile(WebUtils.webSecurityProfileFromUser(user));
    }

    @PostConstruct
    public void signalLoading() {
        LOGGER.debug("[SECURITY] Load rest authenticator {}", this.getClass().getName());
    }
}
