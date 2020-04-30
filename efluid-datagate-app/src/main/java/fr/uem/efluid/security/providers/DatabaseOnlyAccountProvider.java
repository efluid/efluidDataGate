package fr.uem.efluid.security.providers;

import fr.uem.efluid.model.entities.User;
import fr.uem.efluid.model.repositories.UserRepository;
import org.pac4j.core.credentials.password.PasswordEncoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Default authentication and user provider, using only local user accounts and password encoder
 *
 * @author elecomte
 * @version 1
 * @since v1.2.0
 */
public class DatabaseOnlyAccountProvider implements AccountProvider {

    private static final Logger LOGGER = LoggerFactory.getLogger(DatabaseOnlyAccountProvider.class);

    @Autowired
    private PasswordEncoder encoder;

    @Autowired
    private UserRepository users;

    public DatabaseOnlyAccountProvider() {
        LOGGER.info("[SECURITY] Using pure database accounting and authentication");
    }

    @Override
    public Optional<User> findExistingUserByLogin(String username) {
        return this.users.findByLogin(username);
    }

    @Override
    public Optional<User> findExistingUserByToken(String token) {
        return this.users.findByToken(token);
    }

    @Override
    public Optional<User> authenticate(String username, String password) {
        return this.users.findByLogin(username)
                .filter(u -> this.encoder.matches(password, u.getPassword()));
    }

    @Override
    public List<User> findAllExistingUsers() {
        return this.users.findAll();
    }

    @Override
    public User createUser(String login, String email, String password) {

        User user = new User();
        user.setLogin(login);
        user.setPassword(this.encoder.encode(password));
        user.setEmail(email);
        user.setToken(generateToken());
        user.setCreatedTime(LocalDateTime.now());

        return this.users.save(user);
    }

    @Override
    public User updateUser(User user) {
        return this.users.save(user);
    }

    @Override
    public Support getSupport() {
        return Support.FULL;
    }
}
