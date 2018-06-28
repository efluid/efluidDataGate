package fr.uem.efluid.model.repositories;

import static fr.uem.efluid.utils.RuntimeValuesUtils.dbRawToUuid;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import fr.uem.efluid.model.entities.FunctionalDomain;
import fr.uem.efluid.model.entities.Project;

/**
 * @author elecomte
 * @since v0.0.1
 * @version 1
 */
public interface FunctionalDomainRepository extends JpaRepository<FunctionalDomain, UUID> {

	/**
	 * @param project
	 * @return
	 */
	List<FunctionalDomain> findByProject(Project project);

	@Query("SELECT DISTINCT do.uuid FROM IndexEntry ind INNER JOIN ind.dictionaryEntry.domain do GROUP BY do.uuid")
	List<UUID> findUsedIds();

	/**
	 * <b><font color="red">Query for internal use only</font></b>
	 * 
	 * @return
	 */
	// TODO : Once with java9, specify as private
	@Query(value = "select distinct "
			+ "		concat(c.uuid,'') as c_uuid, "
			+ "		d.name "
			+ "from domain d "
			+ "inner join dictionary t on t.domain_uuid = d.uuid "
			+ "inner join indexes i on i.dictionary_entry_uuid = t.uuid "
			+ "inner join commits c on c.uuid = i.commit_uuid "
			+ "where d.project_uuid = :projectUuid"
			+ "group by d.name, c.uuid",
			nativeQuery = true)
	List<Object[]> _internal_findAllNamesUsedByCommits(UUID projectUuid);

	/**
	 * Provides the domain names for each existing commits
	 * 
	 * @return
	 */
	default Map<UUID, List<String>> loadAllDomainNamesByCommitUuids(Project project) {

		return _internal_findAllNamesUsedByCommits(project.getUuid()).stream()
				.collect(Collectors.groupingBy(t -> dbRawToUuid(t[0]), Collectors.mapping(t -> String.valueOf(t[1]), Collectors.toList())));
	}

	/**
	 * @param projectUuid
	 * @return
	 */
	@Query(value = "select count(*) from domain d where d.project_uuid = :projectUuid", nativeQuery = true)
	int countForProject(UUID projectUuid);
}
