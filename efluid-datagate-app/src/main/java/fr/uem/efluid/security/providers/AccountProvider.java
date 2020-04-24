package fr.uem.efluid.security.providers;

import fr.uem.efluid.model.entities.User;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Default entity for application accounting and various entry point on user accounts
 *
 * @author elecomte
 * @version 1
 * @since v1.2.0
 */
public interface AccountProvider {

    /**
     * Authenticate and, if required, preload the user. Return optional.Empty if authentication failure or no available data
     *
     * @param username login
     * @param password pwd
     * @return Optional empty if authenticate failed, else return user
     */
    Optional<User> authenticate(String username, String password);

    Optional<User> findExistingUserByLogin(String username);

    Optional<User> findExistingUserByToken(String token);

    List<User> findAllExistingUsers();

    User createUser(String login, String email, String password);

    User updateUser(User user);

    Support getSupport();

    default String generateToken() {
        return UUID.randomUUID().toString().replaceAll("-", "");
    }

    enum Support {
        FULL, PRELOAD, LIMITED;
    }
}
