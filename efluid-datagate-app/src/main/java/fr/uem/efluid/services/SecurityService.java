package fr.uem.efluid.services;

import fr.uem.efluid.model.entities.User;
import fr.uem.efluid.security.UserHolder;
import fr.uem.efluid.security.providers.AccountProvider;
import fr.uem.efluid.services.types.UserDetails;
import fr.uem.efluid.utils.ApplicationException;
import fr.uem.efluid.utils.ErrorType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * @author elecomte
 * @version 2
 * @since v0.0.1
 */
@Service
@Transactional
public class SecurityService extends AbstractApplicationService {

    private static final Logger LOGGER = LoggerFactory.getLogger(SecurityService.class);

    @Autowired
    private AccountProvider accounts;

    @Autowired
    private ProjectManagementService projectService;

    /**
     * @param login
     * @param email
     * @param password
     */
    @CacheEvict(cacheNames = "users", allEntries = true)
    public User addSimpleUser(String login, String email, String password, boolean fromWizzard) {

        if (this.accounts.getSupport() == AccountProvider.Support.FULL) {

            LOGGER.info("Creation new user : {}", login);

            User user = this.accounts.createUser(login, email, password);

            if (fromWizzard) {
                LOGGER.info("New user {} is created in wizard mode. Set it as current active user for holder, dropped after wizard complete",
                        login);
                this.holder.setWizzardUser(user);
            }

            return user;
        }

        // For wizzard init, must support init on an authentication processed
        else {

            LOGGER.info("Creation new user from an authentication process: {}", login);

            Optional<User> user = this.accounts.authenticate(login, password);

            if (user.isPresent()) {
                if (fromWizzard) {
                    LOGGER.info("New user {} is created in wizard mode from external auth account. Set it as current active user for holder, dropped after wizard complete",
                            login);
                    this.holder.setWizzardUser(user.get());
                }
                return user.get();
            }

            return null;
        }
    }

    /**
     * <p>
     * Create a complete user for user management features. Apply the selected prefered
     * project in the same time
     * </p>
     *
     * @param login
     * @param email
     * @param password
     * @param preferedProjects Selected prefered projects for the new user
     */
    public void createUser(String login, String email, String password, List<UUID> preferedProjects) {

        User user = addSimpleUser(login, email, password, false);

        this.projectService.setPreferedProjectsForUser(user, preferedProjects);
    }

    /**
     * <p>
     * Create a complete user for user management features. Apply the selected prefered
     * project in the same time
     * </p>
     *
     * @param login
     * @param email
     * @param preferedProjects Selected prefered projects for the new user
     */
    public void editUser(String login, String email, List<UUID> preferedProjects) {

        User user = this.accounts.findExistingUserByLogin(login)
                .map(u -> {
                    // Apply new values
                    u.setEmail(email);
                    this.accounts.updateUser(u);
                    return u;
                })
                .orElseThrow(() -> new ApplicationException(ErrorType.OTHER, "User not found"));

        // Edit and save
        this.projectService.setPreferedProjectsForUser(user, preferedProjects);
    }

    /**
     * <p>
     * For rendering of user info
     * </p>
     *
     * @return
     */
    public UserDetails getCurrentUserDetails() {

        return getUserDetails(getCurrentUser().getLogin());
    }

    /**
     * <p>
     * For rendering of user to edit
     * </p>
     *
     * @param login identifier for user to get
     * @return
     */
    public UserDetails getUserDetails(String login) {

        return this.accounts.findExistingUserByLogin(login)
                .map(UserDetails::fromEntity)
                .filter(user -> !user.getLogin().equals(UserHolder.TECHNICAL_USER)) //hide user technic from list
                .orElseThrow(() -> new ApplicationException(ErrorType.OTHER, "User not found"));
    }

    /**
     * @return
     */
    public List<UserDetails> getAllUserDetails() {
        return this.accounts.findAllExistingUsers().stream()
                .map(UserDetails::fromEntity)
                .filter(user -> !user.getLogin().equals(UserHolder.TECHNICAL_USER)) //hide user technic from list
                .collect(Collectors.toList());
    }

    public boolean canPreloadUserOnly() {
        return this.accounts.getSupport() != AccountProvider.Support.FULL;
    }

    /**
     * <p>
     * Called when wizard process is completed to break wizard user mode
     * </p>
     */
    public void completeWizzardUserMode() {
        LOGGER.info("Wizzard completed. Drop user from holder");
        this.holder.setWizzardUser(null);
    }
}
