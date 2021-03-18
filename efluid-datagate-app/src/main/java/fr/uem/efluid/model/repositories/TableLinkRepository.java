package fr.uem.efluid.model.repositories;

import fr.uem.efluid.model.entities.DictionaryEntry;
import fr.uem.efluid.model.entities.FunctionalDomain;
import fr.uem.efluid.model.entities.TableLink;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.*;
import java.util.stream.Collectors;

import static fr.uem.efluid.utils.RuntimeValuesUtils.dbRawToUuid;

/**
 * @author elecomte
 * @version 1
 * @since v0.0.1
 */
public interface TableLinkRepository extends JpaRepository<TableLink, UUID> {

    /**
     * <p>
     * Check if some links are specified for a parameter table
     * </p>
     */
    @Query("select count(t) from TableLink t where t.dictionaryEntry = :dictionaryEntry")
    long countLinksForDictionaryEntry(@Param("dictionaryEntry") DictionaryEntry dictionaryEntry);

    List<TableLink> findByDictionaryEntry(DictionaryEntry dictionaryEntry);

    List<TableLink> findByDictionaryEntryDomain(FunctionalDomain domain);

    List<TableLink> findByDictionaryEntryDomainIn(List<FunctionalDomain> domains);

    Optional<TableLink> findByDictionaryEntryAndColumnFromAndTableToAndColumnTo(DictionaryEntry dictionaryEntry, String columnFrom, String tableTo, String columnTo);

    /**
     * <b><font color="red">Query for internal use only</font></b>
     */
    @Query(value = "SELECT concat(l.dictionary_entry_uuid,'') as from_col, concat(d.uuid,'') as to_col FROM link l INNER JOIN dictionary d ON d.table_name = l.table_to",
            nativeQuery = true)
    List<Object[]> _internal_findAllRelationships();

    default Map<UUID, Set<UUID>> loadAllDictionaryEntryRelationashipFromLinks() {

        return _internal_findAllRelationships().stream()
                .collect(Collectors.groupingBy(t -> dbRawToUuid(t[0]), Collectors.mapping(t -> dbRawToUuid(t[1]), Collectors.toSet())));
    }

    default Map<UUID, List<TableLink>> findAllMappedByDictionaryEntryUUID() {

        return findAll().stream()
                .collect(Collectors.groupingBy(d -> d.getDictionaryEntry().getUuid()));

    }
}
