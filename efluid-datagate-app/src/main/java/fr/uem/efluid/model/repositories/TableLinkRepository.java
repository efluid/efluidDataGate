package fr.uem.efluid.model.repositories;

import static fr.uem.efluid.utils.RuntimeValuesUtils.*;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import fr.uem.efluid.model.entities.DictionaryEntry;
import fr.uem.efluid.model.entities.FunctionalDomain;
import fr.uem.efluid.model.entities.TableLink;

/**
 * @author elecomte
 * @since v0.0.1
 * @version 1
 */
public interface TableLinkRepository extends JpaRepository<TableLink, UUID> {

	/**
	 * <p>
	 * Check if some links are specified for a parameter table
	 * </p>
	 * 
	 * @param dictionaryEntry
	 * @return
	 */
	@Query("select count(t) from TableLink t where t.dictionaryEntry = :dictionaryEntry")
	long countLinksForDictionaryEntry(@Param("dictionaryEntry") DictionaryEntry dictionaryEntry);

	/**
	 * @param dictionaryEntry
	 * @return
	 */
	List<TableLink> findByDictionaryEntry(DictionaryEntry dictionaryEntry);

	/**
	 * @param domain
	 * @return
	 */
	List<TableLink> findByDictionaryEntryDomain(FunctionalDomain domain);

	/**
	 * @param domains
	 * @return
	 */
	List<TableLink> findByDictionaryEntryDomainIn(List<FunctionalDomain> domains);

	/**
	 * <b><font color="red">Query for internal use only</font></b>
	 * 
	 * @return
	 */
	@Query(value = "SELECT concat(l.dictionary_entry_uuid,'') as from_col, concat(d.uuid,'') as to_col FROM link l INNER JOIN dictionary d ON d.table_name = l.table_to",
			nativeQuery = true)
	List<Object[]> _internal_findAllRelationships();

	/**
	 * @return
	 */
	default Map<UUID, Set<UUID>> loadAllDictionaryEntryRelationashipFromLinks() {

		return _internal_findAllRelationships().stream()
				.collect(Collectors.groupingBy(t -> dbRawToUuid(t[0]), Collectors.mapping(t -> dbRawToUuid(t[1]), Collectors.toSet())));
	}

	/**
	 * @return
	 */
	default Map<UUID, List<TableLink>> findAllMappedByDictionaryEntryUUID() {

		return findAll().stream()
				.collect(Collectors.groupingBy(d -> d.getDictionaryEntry().getUuid()));

	}
}
