package fr.uem.efluid.model.repositories;

import static fr.uem.efluid.utils.RuntimeValuesUtils.dbRawToUuid;

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
import fr.uem.efluid.model.entities.TableMapping;

/**
 * @author elecomte
 * @since v2.0.0
 * @version 1
 */
public interface TableMappingRepository extends JpaRepository<TableMapping, UUID> {

	/**
	 * <p>
	 * Check if some mappings are specified for a parameter table
	 * </p>
	 */
	@Query("select count(t) from TableMapping t where t.dictionaryEntry = :dictionaryEntry")
	long countMappingsForDictionaryEntry(@Param("dictionaryEntry") DictionaryEntry dictionaryEntry);

	List<TableMapping> findByDictionaryEntry(DictionaryEntry dictionaryEntry);

	List<TableMapping> findByDictionaryEntryDomain(FunctionalDomain domain);

	List<TableMapping> findByDictionaryEntryDomainIn(List<FunctionalDomain> domains);

	/**
	 * <b><font color="red">Query for internal use only</font></b>
	 */
	@Query(value = "SELECT concat(m.dictionary_entry_uuid,'') as from_col, concat(d.uuid,'') as to_col FROM mappings m INNER JOIN dictionary d ON d.table_name = m.table_to",
			nativeQuery = true)
	List<Object[]> _internal_findAllRelationships();

	default Map<UUID, Set<UUID>> loadAllDictionaryEntryRelationashipFromMappings() {

		return _internal_findAllRelationships().stream()
				.collect(Collectors.groupingBy(t -> dbRawToUuid(t[0]), Collectors.mapping(t -> dbRawToUuid(t[1]), Collectors.toSet())));
	}

	default Map<UUID, List<TableMapping>> findAllMappedByDictionaryEntryUUID() {

		return findAll().stream()
				.collect(Collectors.groupingBy(d -> d.getDictionaryEntry().getUuid()));

	}
}
