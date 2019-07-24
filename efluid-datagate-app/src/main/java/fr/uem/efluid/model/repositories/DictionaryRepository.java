package fr.uem.efluid.model.repositories;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import fr.uem.efluid.model.entities.DictionaryEntry;
import fr.uem.efluid.model.entities.FunctionalDomain;
import fr.uem.efluid.model.entities.Project;

/**
 * @author elecomte
 * @since v0.0.1
 * @version 1
 */
public interface DictionaryRepository extends JpaRepository<DictionaryEntry, UUID> {

	/**
	 * @return
	 */
	@Query("SELECT DISTINCT dic.uuid FROM IndexEntry ind INNER JOIN ind.dictionaryEntry dic GROUP BY dic.uuid")
	List<UUID> findUsedIds();

	/**
	 * @param tableName
	 * @return
	 */
	DictionaryEntry findByTableName(String tableName);

	/**
	 * @param domain
	 * @return
	 */
	List<DictionaryEntry> findByDomain(FunctionalDomain domain);

	/**
	 * @param domains
	 * @return
	 */
	List<DictionaryEntry> findByDomainIn(List<FunctionalDomain> domains);

	/**
	 * @param project
	 * @return
	 */
	List<DictionaryEntry> findByDomainProject(Project project);

	/**
	 * @return
	 */
	default Map<UUID, DictionaryEntry> findAllMappedByUuid(Project project) {

		return findByDomainProject(project).stream()
				.collect(Collectors.toMap(DictionaryEntry::getUuid, d -> d));

	}

	/**
	 * @return
	 */
	default Map<String, DictionaryEntry> findAllMappedByTableName(Project project) {

		return findByDomainProject(project).stream()
				.collect(Collectors.toMap(DictionaryEntry::getTableName, d -> d));

	}
}
