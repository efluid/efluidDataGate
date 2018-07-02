package fr.uem.efluid.services;

import static fr.uem.efluid.utils.ErrorType.PROJECT_MANDATORY;
import static fr.uem.efluid.utils.ErrorType.PROJECT_NAME_EXIST;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import fr.uem.efluid.model.entities.Project;
import fr.uem.efluid.model.entities.User;
import fr.uem.efluid.model.repositories.ProjectRepository;
import fr.uem.efluid.model.repositories.UserRepository;
import fr.uem.efluid.services.types.ProjectData;
import fr.uem.efluid.utils.ApplicationException;

/**
 * <p>
 * Project is a top level information for organization of data. An application instance
 * can have many projects. Regarding GIT principles, a project is like a repository /
 * project : on same repository instance we can have many repositories, and all the
 * information is independent on each project
 * </p>
 * 
 * @author elecomte
 * @since v0.2.0
 * @version 2
 */
@Service
@Transactional
public class ProjectManagementService extends AbstractApplicationService {

	private static final Logger LOGGER = LoggerFactory.getLogger(ProjectManagementService.class);

	@Autowired
	private ProjectRepository projects;

	@Autowired
	private UserRepository users;

	/**
	 * 
	 */
	public ProjectManagementService() {
		// TODO Auto-generated constructor stub
	}

	public ProjectData getCurrentSelectedProject() {

		// For now we are using project data stored in database directly, even if asked at
		// every request, for basic stateless model. Could be improved as the stateless
		// model is not mandatory here regarding the needs

		return ProjectData.fromEntity(getCurrentSelectedProjectEntity());
	}

	/**
	 * <p>
	 * Apply selected project to current user
	 * </p>
	 * 
	 * @param projectId
	 */
	@CacheEvict(cacheNames = "details", allEntries = true)
	public void selectProject(UUID projectId) {

		User user = this.users.getOne(getCurrentUser().getLogin());

		LOGGER.debug("Select project {} for current user {}", projectId, user.getLogin());

		Project project = this.projects.getOne(projectId);
		user.setSelectedProject(project);
		this.users.save(user);

		LOGGER.info("User {} is now working with project {}", user.getLogin(), project.getName());
	}

	/**
	 * @return
	 */
	public List<ProjectData> getPreferedProjectsForCurrentUser() {
		User user = this.users.getOne(getCurrentUser().getLogin());
		return user.getPreferedProjects().stream().map(ProjectData::fromEntity).collect(Collectors.toList());
	}

	/**
	 * <p>
	 * Update on current connected user
	 * </p>
	 * 
	 * @param projectIds
	 */
	public void setPreferedProjectsForCurrentUser(List<UUID> projectIds) {

		User user = this.users.getOne(getCurrentUser().getLogin());

		setPreferedProjectsForUser(user, projectIds);
	}

	/**
	 * <p>
	 * Set the prefered users from given uuids
	 * </p>
	 * 
	 * @param user
	 * @param projectIds
	 */
	public void setPreferedProjectsForUser(User user, List<UUID> projectIds) {

		LOGGER.debug("Update selected projects for user {}. Set {} projects", user.getLogin(), Integer.valueOf(projectIds.size()));

		// New list
		user.setPreferedProjects(new HashSet<>(this.projects.findAllById(projectIds)));

		this.users.save(user);
	}

	/**
	 * <p>
	 * For edit
	 * </p>
	 * 
	 * @param name
	 * @return add entity as data
	 */
	public ProjectData createNewProject(String name, int color) {

		LOGGER.debug("Create new project {}", name);

		assertProjectNameIsAvailable(name);

		Project project = new Project();

		project.setUuid(UUID.randomUUID());
		project.setName(name);
		project.setCreatedTime(LocalDateTime.now());
		project.setColor(color);

		project = this.projects.save(project);

		// Also add it as prefered to current user
		User user = this.users.getOne(getCurrentUser().getLogin());
		user.getPreferedProjects().add(project);
		this.users.save(user);

		return ProjectData.fromEntity(project);
	}

	/**
	 * <p>
	 * For edit
	 * </p>
	 * 
	 * @return
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
	 * @param imported
	 * @return
	 */
	Project importProject(Project imported, AtomicInteger newCounts) {

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
			} else {
				LOGGER.debug("Import exsting project by name \"{}\" : will reuse existing project with uuid {}", imported.getName(),
						imported.getUuid());
				loc = byName;
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
	 * @return
	 */
	Project getCurrentSelectedProjectEntity() {
		return this.projects.findSelectedProjectForUserLogin(getCurrentUser().getLogin());
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
