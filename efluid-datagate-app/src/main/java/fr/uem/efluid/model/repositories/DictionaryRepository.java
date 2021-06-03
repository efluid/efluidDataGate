package fr.uem.efluid.model.repositories;

import fr.uem.efluid.model.entities.DictionaryEntry;
import fr.uem.efluid.model.entities.FunctionalDomain;
import fr.uem.efluid.model.entities.Project;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * @author elecomte
 * @version 1
 * @since v0.0.1
 */
public interface DictionaryRepository extends JpaRepository<DictionaryEntry, UUID> {

    @Query("SELECT DISTINCT dic.uuid FROM IndexEntry ind INNER JOIN ind.dictionaryEntry dic GROUP BY dic.uuid")
    List<UUID> findUsedIds();

    DictionaryEntry findByTableName(String tableName);

    Optional<DictionaryEntry> findByTableNameAndDomainProject(String tableName, Project project);

    List<DictionaryEntry> findByDomain(FunctionalDomain domain);

    List<DictionaryEntry> findByDomainIn(List<FunctionalDomain> domains);

    List<DictionaryEntry> findByDomainProject(Project project);

    // @Query(value = "select dic.* from DICTIONARY dic where dic.UUID in (select distinct id.DICTIONARY_ENTRY_UUID from INDEXES id where id.COMMIT_UUID = :uuid )", nativeQuery = true)
    // select distinct idx.dictionaryEntry from IndexEntry idx where idx.commit.uuid = :uuid
    @Query("SELECT DISTINCT idx.dictionaryEntry.uuid FROM IndexEntry idx WHERE idx.commit.uuid = :uuid")
    List<UUID> findUsedUuidsByCommitUuid(@Param("uuid") UUID commitUuid);

    default Map<UUID, DictionaryEntry> findAllByProjectMappedToUuid(Project project) {

        return findByDomainProject(project).stream()
                .collect(Collectors.toMap(DictionaryEntry::getUuid, d -> d));

    }

    default Map<String, DictionaryEntry> findAllByProjectMappedToTableName(Project project) {

        return findByDomainProject(project).stream()
                // TODO [RISK01] : for multiple dict entries for same table in one project, need to be updated
                .collect(Collectors.toMap(DictionaryEntry::getTableName, d -> d));

    }
}
