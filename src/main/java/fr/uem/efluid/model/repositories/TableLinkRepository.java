package fr.uem.efluid.model.repositories;

import static fr.uem.efluid.utils.RuntimeValuesUtils.*;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import fr.uem.efluid.model.entities.DictionaryEntry;
import fr.uem.efluid.model.entities.TableLink;

/**
 * @author elecomte
 * @since v0.0.1
 * @version 1
 */
public interface TableLinkRepository extends JpaRepository<TableLink, UUID> {

	/**
	 * @param dictionaryEntry
	 * @return
	 */
	List<TableLink> findByDictionaryEntry(DictionaryEntry dictionaryEntry);

	/**
	 * @return
	 */
	@Query(value = "SELECT l.dictionary_entry_uuid, d.uuid FROM link l INNER JOIN dictionary d ON d.table_name = l.table_to",
			nativeQuery = true)
	List<Object[]> _internal_findAllRelationships();

	/**
	 * @return
	 */
	default Map<UUID, Set<UUID>> loadAllDictionaryEntryRelationashipFromLinks() {

		return _internal_findAllRelationships().stream()
				.collect(Collectors.groupingBy(t -> dbRawToUuid(t[0]), Collectors.mapping(t -> dbRawToUuid(t[1]), Collectors.toSet())));
	}
}
