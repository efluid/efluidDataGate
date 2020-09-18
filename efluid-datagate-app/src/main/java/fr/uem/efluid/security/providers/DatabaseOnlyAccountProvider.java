package fr.uem.efluid.security.providers;

import fr.uem.efluid.model.entities.Project;
import fr.uem.efluid.model.repositories.ProjectRepository;
import fr.uem.efluid.security.UserHolder;
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

    @Autowired
    protected UserHolder holder;

    @Autowired
    private ProjectRepository projects;


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

    /**
     * Need the current user to get current selected project and add it for
     * the new user
     */
    public User getCurrentUser() {
        return this.holder.getCurrentUser();
    }

    public Project getCurrentSelectedProjectEntity() {
        return this.projects.findSelectedProjectForUserLogin(getCurrentUser().getLogin());
    }

    @Override
    public User createUser(String login, String email, String password) {

        User user = new User();
        user.setLogin(login);
        user.setPassword(this.encoder.encode(password));
        user.setEmail(email);
        user.setToken(generateToken());
        user.setCreatedTime(LocalDateTime.now());
        user.setSelectedProject(getCurrentSelectedProjectEntity());

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
