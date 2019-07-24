package fr.uem.efluid.model.repositories;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import fr.uem.efluid.model.entities.Commit;
import fr.uem.efluid.model.entities.Project;

/**
 * @author elecomte
 * @since v0.0.1
 * @version 1
 */
public interface CommitRepository extends JpaRepository<Commit, UUID> {

	@Query("select c from Commit c, Commit a where a.uuid = :uuid and c.createdTime >= a.uuid and c.project.uuid = :projectUuid")
	List<Commit> findAllAfterSpecifiedCommitUUID(@Param("uuid") UUID uuid, UUID projectUuid);

	/**
	 * @param project
	 * @return
	 */
	List<Commit> findByProject(Project project);

	/**
	 * @param versionUuid
	 * @return
	 */
	@Query("select count(c) from Commit c where c.version.uuid = :versionUuid")
	long countCommitsForVersion(@Param("versionUuid") UUID versionUuid);
}
