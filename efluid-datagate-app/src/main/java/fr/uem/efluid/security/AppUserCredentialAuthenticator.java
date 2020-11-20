package fr.uem.efluid.security;

import fr.uem.efluid.model.entities.User;
import fr.uem.efluid.security.providers.AccountProvider;
import fr.uem.efluid.utils.WebUtils;
import org.pac4j.core.context.WebContext;
import org.pac4j.core.credentials.UsernamePasswordCredentials;
import org.pac4j.core.credentials.authenticator.Authenticator;
import org.pac4j.core.exception.CredentialsException;
import org.pac4j.core.exception.HttpAction;
import org.pac4j.core.util.CommonHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

/**
 * <p>
 * Login / password authen, checking user in repo
 * </p>
 *
 * @author elecomte
 * @version 1
 * @since v0.0.1
 */
@Component
public class AppUserCredentialAuthenticator implements Authenticator<UsernamePasswordCredentials> {

    private static final Logger LOGGER = LoggerFactory.getLogger(AppUserCredentialAuthenticator.class);

    @Autowired
    private AccountProvider accounts;

    /**
     * @see org.pac4j.core.credentials.authenticator.Authenticator#validate(org.pac4j.core.credentials.Credentials,
     * org.pac4j.core.context.WebContext)
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

        //technic user should not be able to connect to app
        if(username.equals("technical-user")) {
            throw new CredentialsException("This user cannot connect to app");
        }

        User user = this.accounts.authenticate(username, password)
                .orElseThrow(() -> new CredentialsException("User not found or wrong password!"));

        LOGGER.debug("Authentication successfull for user {}", user.getLogin());

        credentials.setUserProfile(WebUtils.webSecurityProfileFromUser(user));
    }

    @PostConstruct
    public void signalLoading() {
        LOGGER.debug("[SECURITY] Load web authenticator {}", this.getClass().getName());
    }
}