package fr.uem.efluid.model.repositories;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import fr.uem.efluid.model.entities.Project;

/**
 * @author elecomte
 * @since v0.0.8
 * @version 1
 */
public interface ProjectRepository extends JpaRepository<Project, UUID> {

	/**
	 * <p>
	 * For a specified user (by login) get the currently selected project
	 * </p>
	 */
	@Query("SELECT u.selectedProject FROM User u WHERE u.login = :login")
	Project findSelectedProjectForUserLogin(String login);

	Project findByName(String name);

	@Query("SELECT uuid FROM Project")
	Project getAllProjects();
}
