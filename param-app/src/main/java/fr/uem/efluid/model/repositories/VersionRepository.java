package fr.uem.efluid.model.repositories;

import java.util.Date;
import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import fr.uem.efluid.model.entities.Project;
import fr.uem.efluid.model.entities.Version;
import fr.uem.efluid.utils.RuntimeValuesUtils;

/**
 * <p>
 * Management of version information. Includes identification of last updates on all
 * dictionary values
 * </p>
 * 
 * @author elecomte
 * @since v0.2.0
 * @version 1
 */
public interface VersionRepository extends JpaRepository<Version, UUID> {

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
			+ " and project_uuid = :projectUuid", nativeQuery = true)
	Object _internal_findLastVersionUuidForProject(@Param("projectUuid") UUID projectUuid);

	/**
	 * @param projectUuid
	 * @return
	 */
	@Query(value = "select 'domain' as type, max(updated_time) as time from domain "
			+ " where project_uuid = :projectUuid "
			+ " union "
			+ " select 'dictionary' as type, max(d.updated_time) as time from dictionary d "
			+ " inner join domain m on d.domain_uuid = m.uuid "
			+ " where m.project_uuid = :projectUuid "
			+ " union "
			+ " select 'link' as type, max(l.updated_time) as time from link l "
			+ " inner join dictionary d on l.dictionary_entry_uuid = d.uuid "
			+ " inner join domain m on d.domain_uuid = m.uuid "
			+ " where m.project_uuid = :projectUuid", nativeQuery = true)
	List<Object[]> _internal_findLastDictionaryUpdateForProject(@Param("projectUuid") UUID projectUuid);

	/**
	 * @param project
	 * @return
	 */
	default Version getLastVersionForProject(Project project) {

		UUID lastVersion = RuntimeValuesUtils.dbRawToUuid(_internal_findLastVersionUuidForProject(project.getUuid()));

		// Last version
		return lastVersion != null ? getOne(lastVersion) : null;
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

		List<Object[]> res = _internal_findLastDictionaryUpdateForProject(project.getUuid());

		return res.stream()
				.filter(a -> a[1] != null)
				.map(a -> RuntimeValuesUtils.localDateTime((Date) a[1]))
				.anyMatch(d -> d.isAfter(ver.getUpdatedTime()));
	}

	/**
	 * @param projectUuid
	 * @return
	 */
	@Query(value = "select count(*) from versions d where d.project_uuid = :projectUuid", nativeQuery = true)
	int countForProject(@Param("projectUuid") UUID projectUuid);

	/**
	 * @param versionUuid
	 * @return
	 */
	@Query(value = "select count(*) = 0 from commits where version_uuid = :versionUuid", nativeQuery = true)
	boolean isVersionUpdatable(@Param("versionUuid") UUID versionUuid);
}
