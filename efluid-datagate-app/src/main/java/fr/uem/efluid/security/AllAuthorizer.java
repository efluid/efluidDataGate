package fr.uem.efluid.security;

import fr.uem.efluid.model.entities.User;
import org.pac4j.core.authorization.authorizer.ProfileAuthorizer;
import org.pac4j.core.context.WebContext;
import org.pac4j.core.context.session.SessionStore;
import org.pac4j.core.profile.CommonProfile;
import org.pac4j.core.profile.UserProfile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.List;

/**
 * @author elecomte
 * @version 1
 * @since v0.0.1
 */
@Component
public class AllAuthorizer extends ProfileAuthorizer {

    private static final Logger LOGGER = LoggerFactory.getLogger(AllAuthorizer.class);

    @Autowired
    private UserHolder users;

    @Override
    public boolean isAuthorized(WebContext webContext, SessionStore sessionStore, List<UserProfile> list) {
        return list.stream().allMatch(p -> isProfileAuthorized(webContext, sessionStore, p));
    }

    @Override
    public boolean isProfileAuthorized(final WebContext context, SessionStore sessionStore, final UserProfile profile) {

        if (!(profile instanceof CommonProfile)) {
            return false;
        }

        CommonProfile cprofile = (CommonProfile) profile;

        LOGGER.debug("Apply user {} / {}", cprofile.getId(), cprofile.getEmail());

        this.users.setCurrentUser(new User() {

            /**
             * @see fr.uem.efluid.model.entities.User#getLogin()
             */
            @Override
            public String getLogin() {
                return cprofile.getId();
            }

            /**
             * @see fr.uem.efluid.model.entities.User#getEmail()
             */
            @Override
            public String getEmail() {
                return cprofile.getEmail();
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