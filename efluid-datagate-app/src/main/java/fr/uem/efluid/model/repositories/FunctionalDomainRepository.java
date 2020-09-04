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

	List<FunctionalDomain> findByProject(Project project);

	@Query("SELECT DISTINCT domain.uuid FROM DictionaryEntry")
	List<UUID> findUsedIds();

	/**
	 * <b><font color="red">Query for internal use only</font></b>
	 */
	@Query(value = "select distinct "
			+ "		concat(c.uuid,'') as c_uuid, "
			+ "		d.name "
			+ "from domain d "
			+ "inner join dictionary t on t.domain_uuid = d.uuid "
			+ "inner join indexes i on i.dictionary_entry_uuid = t.uuid "
			+ "inner join commits c on c.uuid = i.commit_uuid "
			+ "where d.project_uuid = :projectUuid "
			+ "group by d.name, c.uuid",
			nativeQuery = true)
	List<Object[]> _internal_findAllNamesUsedByCommits(String projectUuid);

	/**
	 * Provides the domain names for each existing commits
	 */
	default Map<UUID, List<String>> loadAllDomainNamesByCommitUuids(Project project) {

		return _internal_findAllNamesUsedByCommits(project.getUuid().toString()).stream()
				.collect(Collectors.groupingBy(t -> dbRawToUuid(t[0]), Collectors.mapping(t -> String.valueOf(t[1]), Collectors.toList())));
	}

	@Query("select count(d) from FunctionalDomain d where d.project.uuid = :projectUuid")
	int countForProject(UUID projectUuid);

	FunctionalDomain findByProjectAndName(Project project, String name);
}
