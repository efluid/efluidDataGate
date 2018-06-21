package fr.uem.efluid.services;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import fr.uem.efluid.model.entities.Project;
import fr.uem.efluid.model.entities.User;
import fr.uem.efluid.model.repositories.ProjectRepository;
import fr.uem.efluid.model.repositories.UserRepository;
import fr.uem.efluid.services.types.ProjectData;

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
 * @version 1
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

		return ProjectData.fromEntity(this.projects.findSelectedProjectForUserLogin(getCurrentUser().getLogin()));
	}

	/**
	 * <p>
	 * Apply selected project to current user
	 * </p>
	 * 
	 * @param projectId
	 */
	public void selectProject(UUID projectId) {
		User user = this.users.getOne(getCurrentUser().getLogin());
		LOGGER.debug("Select project {} for current user {}", projectId, user.getLogin());
		user.setSelectedProject(this.projects.getOne(projectId));
		this.users.save(user);
	}

	/**
	 * @return
	 */
	public List<ProjectData> getPreferedProjectsForCurrentUser() {
		User user = this.users.getOne(getCurrentUser().getLogin());
		return user.getPreferedProjects().stream().map(ProjectData::fromEntity).collect(Collectors.toList());
	}

	/**
	 * @param projects
	 */
	public void setPreferedProjectsForCurrentUser(List<UUID> projects) {

		User user = this.users.getOne(getCurrentUser().getLogin());

		LOGGER.debug("Update selected projects for current user {}. Set {} projects", user.getLogin(), Integer.valueOf(projects.size()));

		// New list
		user.setPreferedProjects(new HashSet<>(this.projects.findAllById(projects)));
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
	public ProjectData createNewProject(String name) {

		LOGGER.debug("Create new project {}", name);

		Project project = new Project();

		project.setUuid(UUID.randomUUID());
		project.setName(name);
		project.setCreatedTime(LocalDateTime.now());

		return ProjectData.fromEntity(this.projects.save(project));
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
}
