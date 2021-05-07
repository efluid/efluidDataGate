package fr.uem.efluid.services;

import fr.uem.efluid.model.entities.Project;
import fr.uem.efluid.model.entities.User;
import fr.uem.efluid.model.repositories.ProjectRepository;
import fr.uem.efluid.security.UserHolder;
import fr.uem.efluid.security.providers.AccountProvider;
import fr.uem.efluid.services.types.ProjectData;
import fr.uem.efluid.utils.ApplicationException;
import fr.uem.efluid.utils.ErrorType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import static fr.uem.efluid.utils.ErrorType.*;

/**
 * <p>
 * Project is a top level information for organization of data. An application instance
 * can have many projects. Regarding GIT principles, a project is like a repository /
 * project : on same repository instance we can have many repositories, and all the
 * information is independent on each project
 * </p>
 *
 * @author elecomte
 * @version 2
 * @since v0.2.0
 */
@Service
@Transactional
public class ProjectManagementService extends AbstractApplicationService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ProjectManagementService.class);

    @Value("${datagate-efluid.display.get-current-selected-project-short-name}")
    private int shortNameLength;

    @Autowired
    private ProjectRepository projects;

    @Autowired
    private AccountProvider accountProvider;

    /**
     *
     */
    public ProjectManagementService() {
        super();
    }

    /**
     * @return display compliant details on active project
     */
    public ProjectData getCurrentSelectedProject() {

        // For now we are using project data stored in database directly, even if asked at
        // every request, for basic stateless model. Could be improved as the stateless
        // model is not mandatory here regarding the needs

        return ProjectData.fromEntity(getCurrentSelectedProjectEntity());
    }

    /**
     * This function is used to reduce length of title project
     */
    public String getCurrentSelectedProjectShortName() {
        if (ProjectData.fromEntity(getCurrentSelectedProjectEntity()).getName().length() > this.shortNameLength) {
            return ProjectData.fromEntity(getCurrentSelectedProjectEntity()).getName().substring(0, this.shortNameLength) + "...";
        } else {
            return ProjectData.fromEntity(getCurrentSelectedProjectEntity()).getName();
        }
    }

    /**
     * <p>
     * Apply selected project to current user
     * </p>
     *
     * @param projectId selected project
     * @return true if it's a forced preselect of project (when user had not yet selected any active project)
     */
    @CacheEvict(cacheNames = "details", allEntries = true)
    public boolean selectProject(UUID projectId) {

        User user = reloadCurrentUser();

        LOGGER.debug("Select project {} for current user {}", projectId, user.getLogin());

        Project project = this.projects.getOne(projectId);
        boolean hadSelectedProject = user.getSelectedProject() != null;
        user.setSelectedProject(project);
        this.accountProvider.updateUser(user);

        LOGGER.info("User {} is now working with project {}", user.getLogin(), project.getName());
        return !hadSelectedProject;
    }

    /**
     * @return display compliant project details
     */
    public List<ProjectData> getPreferedProjectsForCurrentUser() {
        return reloadCurrentUser().getPreferedProjects().stream().map(ProjectData::fromEntity).collect(Collectors.toList());
    }

    /**
     * <p>
     * Update on current connected user
     * </p>
     *
     * @param projectIds selected project uuids
     */
    public void setPreferedProjectsForCurrentUser(List<UUID> projectIds) {

        this.accountProvider.findExistingUserByLogin(getCurrentUser().getLogin())
                .ifPresent(u -> setPreferedProjectsForUser(u, projectIds));
    }

    /**
     * <p>
     * Set the prefered users from given uuids
     * </p>
     *
     * @param user       specified user
     * @param projectIds selected project uuids
     */
    public void setPreferedProjectsForUser(User user, List<UUID> projectIds) {

        LOGGER.debug("Update selected projects for user {}. Set {} projects", user.getLogin(), projectIds.size());

        // New list
        user.setPreferedProjects(new HashSet<>(this.projects.findAllById(projectIds)));

        this.accountProvider.updateUser(user);
    }

    /**
     * <p>
     * For edit
     * </p>
     *
     * @param name new project name
     * @return add entity as data
     */
    public ProjectData createNewProject(String name, int color) {

        LOGGER.debug("Create new project {}", name);

        assertProjectNameIsAvailable(name);

        final Project project = new Project();

        project.setUuid(UUID.randomUUID());
        project.setName(name);
        project.setCreatedTime(LocalDateTime.now());
        project.setColor(color);

        this.projects.save(project);

        // Also add it as prefered to current user
        User user = reloadCurrentUser();
        user.getPreferedProjects().add(project);
        this.accountProvider.updateUser(user);

        // Also add it automatically in list projects of technic user
        this.accountProvider.findExistingUserByLogin(UserHolder.TECHNICAL_USER)
                .ifPresent(technic -> {
                    technic.getPreferedProjects().add(project);
                    this.accountProvider.updateUser(technic);
                });

        return ProjectData.fromEntity(project);
    }

    /**
     * <p>
     * For edit
     * </p>
     *
     * @param oldNameProject old name project needed to find Project in list
     * @param newNameProject new name project needed to update
     * @throws ApplicationException if id on project is not known in DB do not update project return error
     */
    public Project updateNameProject(String newNameProject, String oldNameProject, UUID uuidProject) {

        Project project = this.projects.findByName(oldNameProject);

        if (uuidProject.compareTo(project.getUuid()) == 0) { //verify that id are the same and we're not trying to change the project project's name
            assertProjectNameIsAvailable(newNameProject);

            project.setName(newNameProject);

            project = this.projects.save(project);

            LOGGER.debug("Update project name {}", newNameProject);
        } else {
            throw new ApplicationException(PROJECT_WRONG_ID, "Les ids ne correspondent pas, le nom du projet ne peut pas être modifié.");
        }

        return project;
    }

    /**
     * <p>
     * For edit
     * </p>
     *
     * @return display compliant project details
     */
    public List<ProjectData> getAllProjects() {
        return this.projects.findAll().stream().map(ProjectData::fromEntity).collect(Collectors.toList());
    }

    /**
     * <p>
     * Process one Project
     * </p>
     * <p>
     * Project can be identified by uuid or by name during import
     * <p>
     *
     * @param imported           project data from imported package
     * @param newCounts          atomic for import count stats
     * @param substituteProjects substitutes on project name rules
     * @return created / updated project
     */
    Project importProject(Project imported, AtomicInteger newCounts, Map<UUID, Project> substituteProjects) {

        Optional<Project> localOpt = this.projects.findById(imported.getUuid());

        // Exists already
        localOpt.ifPresent(d -> LOGGER.debug("Import existing project by uuid {} : will update currently owned", imported.getUuid()));

        // Will try also by name
        Project byName = this.projects.findByName(imported.getName());

        // Search on existing Or is a new one
        Project local = localOpt.orElseGet(() -> {
            Project loc;
            if (byName == null) {
                LOGGER.debug("Import new project {} : will create currently owned", imported.getUuid());
                loc = new Project(imported.getUuid());
                newCounts.incrementAndGet();
                substituteProjects.put(loc.getUuid(), loc);
            } else {
                LOGGER.debug("Import exsting project by name \"{}\" : will reuse existing project with uuid {} and substitute "
                        + "for other data associated to project uuid {}", imported.getName(), byName.getUuid(), imported.getUuid());
                loc = byName;

                // Keep substitute for domain import
                substituteProjects.put(imported.getUuid(), byName);
            }
            return loc;
        });

        // Common attrs
        local.setCreatedTime(imported.getCreatedTime());
        local.setName(imported.getName());
        local.setColor(imported.getColor());

        local.setImportedTime(LocalDateTime.now());

        return local;
    }

    /**
     * @return entity for project for current user
     */
    Project getCurrentSelectedProjectEntity() {
        return this.projects.findSelectedProjectForUserLogin(getCurrentUser().getLogin());
    }

    private User reloadCurrentUser() {
        return this.accountProvider.findExistingUserByLogin(getCurrentUser().getLogin())
                .orElseThrow(() -> new ApplicationException(ErrorType.OTHER, "User not found"));
    }

    /**
     * <p>
     * Internal validation of availability of a selected project with clean failure
     * checking
     * </p>
     */
    private void assertProjectNameIsAvailable(String name) {

        if (this.projects.findByName(name) != null) {
            throw new ApplicationException(PROJECT_NAME_EXIST, "A project with name " + name + " already exist", name);
        }
    }

    /**
     * <p>
     * Internal validation of availability of a selected project with clean failure
     * checking
     * </p>
     */
    void assertCurrentUserHasSelectedProject() {

        String login = getCurrentUser().getLogin();
        if (this.projects.findSelectedProjectForUserLogin(login) == null) {
            throw new ApplicationException(PROJECT_MANDATORY, "No selected active project for current user " + login);
        }

    }
}
