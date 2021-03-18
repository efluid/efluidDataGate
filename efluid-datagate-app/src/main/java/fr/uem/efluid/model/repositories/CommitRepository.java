package fr.uem.efluid.model.repositories;

import fr.uem.efluid.model.entities.Commit;
import fr.uem.efluid.model.entities.Project;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

/**
 * @author elecomte
 * @version 1
 * @since v0.0.1
 */
public interface CommitRepository extends JpaRepository<Commit, UUID> {

    List<Commit> findByProject(Project project);

    @Query("select count(c) from Commit c where c.version.uuid = :versionUuid")
    long countCommitsForVersion(@Param("versionUuid") UUID versionUuid);

    @Query(value = "select c.uuid from Commit c " +
            "where c.createdTime = " +
            "   (select max(cc.createdTime) from Commit cc where cc.project.uuid = :projectUuid) " +
            "and c.state <> fr.uem.efluid.model.entities.CommitState.REVERT and c.project.uuid = :projectUuid")
    UUID findRevertableCommitUuid(@Param("projectUuid") UUID projectUuid);
}
