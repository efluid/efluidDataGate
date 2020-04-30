package fr.uem.efluid.cucumber.common;

import fr.uem.efluid.model.entities.User;
import fr.uem.efluid.security.providers.AccountProvider;
import fr.uem.efluid.security.providers.DatabaseOnlyAccountProvider;
import fr.uem.efluid.security.providers.LdapAuthAccountProvider;

import java.util.List;
import java.util.Optional;

/**
 * An <tt>AccountProvider</tt> which can switch between LDAP and DATABASE type at runtime (for testing)
 *
 * @author elecomte
 * @version 1
 * @since v1.2.0
 */
public class SwitchableLdapAccountProvider extends LdapAuthAccountProvider {

    private final DatabaseOnlyAccountProvider database;

    private boolean useLdap = false;

    SwitchableLdapAccountProvider(DatabaseOnlyAccountProvider database) {
        super(null, null, null);
        this.database = database;

    }

    @Override
    public Optional<User> authenticate(String username, String password) {
        return this.useLdap ? super.authenticate(username, password) : this.database.authenticate(username, password);
    }

    @Override
    public Optional<User> findExistingUserByLogin(String username) {
        return this.useLdap ? super.findExistingUserByLogin(username) : this.database.findExistingUserByLogin(username);
    }

    @Override
    public Optional<User> findExistingUserByToken(String token) {
        return this.useLdap ? super.findExistingUserByToken(token) : this.database.findExistingUserByToken(token);
    }

    @Override
    public List<User> findAllExistingUsers() {
        return this.useLdap ? super.findAllExistingUsers() : this.database.findAllExistingUsers();
    }

    @Override
    public User createUser(String login, String email, String password) {
        return this.useLdap ? super.createUser(login, email, password) : this.database.createUser(login, email, password);
    }

    @Override
    public User updateUser(User user) {
        return this.useLdap ? super.updateUser(user) : this.database.updateUser(user);
    }

    @Override
    public Support getSupport() {
        return this.useLdap ? super.getSupport() : this.database.getSupport();
    }

    private AccountProvider getProvider() {
        if (this.useLdap) {
            return this;
        }
        return this.database;
    }

    public void useDatabase() {
        this.useLdap = false;
    }

    /**
     * Enable LDAP mode, with specified config (for in test mode specification)
     *
     * @param searchBase
     * @param loginAttribute
     * @param mailAttribute
     */
    public void useLdap(String searchBase,
                        String loginAttribute,
                        String mailAttribute) {
        this.useLdap = true;
        this.updateAuthAccountProvider(searchBase,
                loginAttribute,
                mailAttribute);
    }
}
