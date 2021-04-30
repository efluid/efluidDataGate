package fr.uem.efluid.model.repositories;

import fr.uem.efluid.model.entities.Project;
import fr.uem.efluid.model.entities.Version;
import fr.uem.efluid.utils.RuntimeValuesUtils;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * <p>
 * Management of version information. Includes identification of last updates on all
 * dictionary values
 * </p>
 *
 * @author elecomte
 * @version 1
 * @since v0.2.0
 */
public interface VersionRepository extends JpaRepository<Version, UUID> {

    String MAPPED_TYPE_DICT = "dictionary";

    String MAPPED_TYPE_DOMAIN = "domain";

    String MAPPED_TYPE_LINK = "link";

    String MAPPED_TYPE_MAPPING = "mapping";

    List<Version> findByProject(Project project);

    Version findByNameAndProject(String name, Project project);

    @Query(value = "select concat(uuid,'') from versions where created_time = "
            + " (select max(created_time) from versions where project_uuid = :projectUuid) "
            + " and project_uuid = :projectUuid order by created_time desc", nativeQuery = true)
    List<Object[]> _internal_findLastVersionUuidForProject(@Param("projectUuid") String projectUuid);

    @Query(value = "select '" + MAPPED_TYPE_DOMAIN + "' as type, max(updated_time) as time from domain "
            + " where project_uuid = :projectUuid "
            + " union "
            + " select '" + MAPPED_TYPE_DICT + "' as type, max(d.updated_time) as time from dictionary d "
            + " inner join domain m on d.domain_uuid = m.uuid "
            + " where m.project_uuid = :projectUuid "
            + " union "
            + " select '" + MAPPED_TYPE_LINK + "' as type, max(l.updated_time) as time from link l "
            + " inner join dictionary d on l.dictionary_entry_uuid = d.uuid "
            + " inner join domain m on d.domain_uuid = m.uuid "
            + " union "
            + " select '" + MAPPED_TYPE_MAPPING + "' as type, max(t.updated_time) as time from mappings t "
            + " inner join dictionary d on t.dictionary_entry_uuid = d.uuid "
            + " inner join domain m on d.domain_uuid = m.uuid "
            + " where m.project_uuid = :projectUuid", nativeQuery = true)
    List<Object[]> _internal_findLastDictionaryUpdateForProject(@Param("projectUuid") String projectUuid);

    default Version getLastVersionForProject(Project project) {

        // Universal "1st value"
        List<Object[]> versions = _internal_findLastVersionUuidForProject(project.getUuid().toString());

        UUID lastVersion = RuntimeValuesUtils.dbRawToUuid(versions != null && !versions.isEmpty() ? versions.get(0)[0] : null);

        // Last version
        return lastVersion != null ? getOne(lastVersion) : null;
    }

    @Query(value = "select '" + MAPPED_TYPE_DOMAIN + "' as type, uuid as uuid from domain "
            + " where project_uuid = :projectUuid "
            + " and updated_time < :checkTime "
            + " union "
            + " select '" + MAPPED_TYPE_DICT + "' as type, d.uuid as uuid from dictionary d "
            + " inner join domain m on d.domain_uuid = m.uuid "
            + " where m.project_uuid = :projectUuid "
            + " and d.updated_time < :checkTime "
            + " union "
            + " select '" + MAPPED_TYPE_LINK + "' as type, l.uuid as uuid from link l "
            + " inner join dictionary d on l.dictionary_entry_uuid = d.uuid "
            + " inner join domain m on d.domain_uuid = m.uuid "
            + " where m.project_uuid = :projectUuid"
            + " and l.updated_time < :checkTime "
            + " union "
            + " select '" + MAPPED_TYPE_MAPPING + "' as type, t.uuid as uuid from mappings t "
            + " inner join dictionary d on t.dictionary_entry_uuid = d.uuid "
            + " inner join domain m on d.domain_uuid = m.uuid "
            + " where m.project_uuid = :projectUuid "
            + " and t.updated_time < :checkTime ", nativeQuery = true)
    List<Object[]> _internal_findLastDictionaryUpdateForProject(@Param("projectUuid") String projectUuid, @Param("checkTime") LocalDateTime lastVersionUpdate);

    /**
     * Extract the version content : everything with an update date before specified one, for given project
     */
    default Map<String, List<UUID>> findLastVersionContents(Project project, LocalDateTime lastVersionUpdate) {

        List<Object[]> res = _internal_findLastDictionaryUpdateForProject(project.getUuid().toString(), lastVersionUpdate);

        // Mapped UUIDs for types
        return res.stream()
                .filter(a -> a[1] != null)
                .collect(Collectors.groupingBy(b -> String.valueOf(b[0])))
                .entrySet().stream()
                .collect(
                        Collectors.toMap(
                                Map.Entry::getKey,
                                e -> e.getValue().stream().map(v -> UUID.fromString(String.valueOf(v[1]))).collect(Collectors.toList())
                        ));
    }

    default boolean hasDictionaryUpdatesAfterLastVersionForProject(Project project) {

        // Last version
        Version ver = getLastVersionForProject(project);

        if (ver == null) {
            return true;
        }

        List<Object[]> res = _internal_findLastDictionaryUpdateForProject(project.getUuid().toString());

        return res.stream()
                .filter(a -> a[1] != null)
                .map(a -> RuntimeValuesUtils.localDateTime((Date) a[1]))
                .anyMatch(d -> d.isAfter(ver.getUpdatedTime()));
    }

    @Query("select count(v) from Version v where v.project.uuid = :projectUuid")
    int countVersionsForProject(@Param("projectUuid") UUID projectUuid);

    @Query(value = "select count(*) from commits where version_uuid = :versionUuid", nativeQuery = true)
    int countVersionUseIn(@Param("versionUuid") UUID versionUuid);

    @Query("select c.version from Commit c where c.uuid in :commitUuids")
    Stream<Version> findVersionForCommitUuidsIn(@Param("commitUuids") List<UUID> commitUuids);

    /**
     * @return true if specified version can be updated
     */
    default boolean isVersionUpdatable(UUID versionUuid) {
        return countVersionUseIn(versionUuid) == 0;
    }

}
