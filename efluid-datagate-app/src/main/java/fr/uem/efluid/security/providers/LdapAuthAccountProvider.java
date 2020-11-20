package fr.uem.efluid.security.providers;

import fr.uem.efluid.model.entities.User;
import fr.uem.efluid.model.repositories.ProjectRepository;
import fr.uem.efluid.model.repositories.UserRepository;
import fr.uem.efluid.utils.ApplicationException;
import fr.uem.efluid.utils.ErrorType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ldap.core.AttributesMapper;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.query.LdapQuery;

import javax.naming.directory.Attribute;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;

import static org.springframework.ldap.query.LdapQueryBuilder.query;

/**
 * <p>Here the accounts are stored in local database, but are initialized and authenticated from a specified LDAP source. The password is never stored locally</p>
 * <p>Add all the available projects as prefered project</p>
 *
 * @author elecomte
 * @version 1
 * @since v1.2.0
 */
public class LdapAuthAccountProvider implements AccountProvider {

    private static final Logger LOGGER = LoggerFactory.getLogger(LdapAuthAccountProvider.class);

    @Autowired
    private LdapTemplate ldapTemplate;

    @Autowired
    private UserRepository users;

    @Autowired
    private ProjectRepository projects;

    private String searchBase;
    private String loginAttribute;
    private String mailAttribute;

    public LdapAuthAccountProvider(
            String searchBase,
            String loginAttribute,
            String mailAttribute) {
        updateAuthAccountProvider(searchBase, loginAttribute, mailAttribute);
    }

    @Override
    public Optional<User> authenticate(String username, String password) {

        try {
            this.ldapTemplate.authenticate(userQuery(username), password);
        } catch (Exception e) {
            LOGGER.warn("Authentication error : cannot process LDAP bind auth for user " + username, e);
            return Optional.empty();
        }
        // Search for existing of init automatically from ldap
        return this.users.findByLogin(username).or(() -> preloadUser(username));
    }

    @Override
    public Optional<User> findExistingUserByLogin(String username) {
        return this.users.findByLogin(username).or(() -> preloadUser(username));
    }

    @Override
    public Optional<User> findExistingUserByToken(String token) {
        return this.users.findByToken(token);
    }

    @Override
    public List<User> findAllExistingUsers() {
        // Use only users registered in local database
        return this.users.findAll();
    }

    @Override
    public User createUser(String login, String email, String password) {

        throw new UnsupportedOperationException("Cannot create directly users : users must exist on LDAP database");
    }

    @Override
    public User updateUser(User user) {
        return this.users.save(user);
    }

    @Override
    public Support getSupport() {
        return Support.PRELOAD;
    }

    private LdapQuery userQuery(String username) {
        return query()
                .base(this.searchBase)
                .where(this.loginAttribute).is(username);
    }

    private Optional<User> preloadUser(String username) {
        Collection<User> users = ldapTemplate.search(userQuery(username),
                (AttributesMapper<User>) attrs -> {
                    User u = new User();
                    u.setExternalRef(attrs.get("cn").get().toString());
                    Attribute email = attrs.get(this.mailAttribute);
                    u.setEmail(email != null ? email.get().toString(): username + "@unknown");
                    return u;
                });

        if (users.size() > 1) {
            throw new ApplicationException(ErrorType.LDAP_ERROR, "Invalid LDAP config : for username " + username + " on current binding configuration, more than 1 entry has been found !");
        }

        User user = users.iterator().next();

        user.setLogin(username);
        user.setCreatedTime(LocalDateTime.now());
        user.setToken(generateToken());
        user.setPreferedProjects(new HashSet<>(this.projects.findAll()));

        return Optional.of(this.users.save(user));
    }

    /**
     * Configuration entry point for testing
     *
     * @param searchBase
     * @param loginAttribute
     * @param mailAttribute
     */
    protected void updateAuthAccountProvider(
            String searchBase,
            String loginAttribute,
            String mailAttribute) {

        this.searchBase = searchBase;
        this.loginAttribute = loginAttribute;
        this.mailAttribute = mailAttribute;

        LOGGER.info("[SECURITY] Using LDAP authentication with database preload target. LDAP User " +
                "search base is {}, for ({} = ?}", this.searchBase, this.loginAttribute);
    }

}
