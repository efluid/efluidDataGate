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

    /**
     * @param project
     * @return
     */
    List<Version> findByProject(Project project);

    /**
     * @param name
     * @return
     */
    Version findByNameAndProject(String name, Project project);

    /**
     * @param projectUuid
     * @return
     */
    @Query(value = "select concat(uuid,'') from versions where created_time = "
            + " (select max(created_time) from versions where project_uuid = :projectUuid) "
            + " and project_uuid = :projectUuid limit 1", nativeQuery = true)
    Object _internal_findLastVersionUuidForProject(@Param("projectUuid") String projectUuid);

    /**
     * @param projectUuid
     * @return
     */
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

    /**
     * @param project
     * @return
     */
    default Version getLastVersionForProject(Project project) {

        UUID lastVersion = RuntimeValuesUtils.dbRawToUuid(_internal_findLastVersionUuidForProject(project.getUuid().toString()));

        // Last version
        return lastVersion != null ? getOne(lastVersion) : null;
    }

    /**
     * @param projectUuid
     * @return
     */
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
     *
     * @param project
     * @param lastVersionUpdate
     * @return
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

    /**
     * @param project
     * @return
     */
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

    /**
     * @param projectUuid
     * @return
     */
    @Query("select count(v) from Version v where v.project.uuid = :projectUuid")
    int countVersionsForProject(@Param("projectUuid") UUID projectUuid);

    /**
     * @param versionUuid
     * @return
     */
    @Query(value = "select count(*) from commits where version_uuid = :versionUuid", nativeQuery = true)
    int countVersionUseIn(@Param("versionUuid") UUID versionUuid);


    /**
     * @param versionUuid
     * @return true if specified version can be updated
     */
    default boolean isVersionUpdatable(UUID versionUuid) {
        return countVersionUseIn(versionUuid) == 0;
    }

}
