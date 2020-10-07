package fr.uem.efluid.model.repositories;

import com.google.common.collect.Lists;
import fr.uem.efluid.model.DiffLine;
import fr.uem.efluid.model.entities.DictionaryEntry;
import fr.uem.efluid.model.entities.IndexEntry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * <p>
 * Core index data provider, using JPA
 * </p>
 *
 * @author elecomte
 * @version 2
 * @since v0.0.1
 */
public interface IndexRepository extends JpaRepository<IndexEntry, Long>, JpaSpecificationExecutor<IndexEntry> {

    /**
     * <p>
     * Search for existing commit indexes
     * </p>
     */
    List<IndexEntry> findByCommitUuid(UUID commitUuid);

    /**
     * All index lines without "previous" value for a specified commit
     *
     * @param commitUuid selected commit
     * @return index to upgrade
     */
    @Query(value = "select i.* "
            + "from INDEXES i "
            + "where i.COMMIT_UUID = :commitUuid "
            + "AND i.ACTION != 'ADD' AND i.PREVIOUS is null", nativeQuery = true)
    List<IndexEntry> findWithUpgradablePreviosByCommitUuid(@Param("commitUuid") String commitUuid);

    /**
     * <p>
     * Search for existing commit indexes
     * </p>
     */
    long countByCommitUuid(UUID commitUuid);

    /**
     * <p>
     * Load full index detail for one DictionaryEntry (= on managed table)
     * </p>
     */
    List<IndexEntry> findByDictionaryEntry(DictionaryEntry dictionaryEntry);

    List<IndexEntry> findByDictionaryEntryAndTimestampLessThanEqual(DictionaryEntry dictionaryEntry, long timestamp);

    /**
     * <b><font color="red">Query for internal use only</font></b>
     */
    @Query(value = "select i.* "
            + "from indexes i "
            + "inner join ("
            + "	select max(ii.id) as max_id, ii.key_value from indexes ii where ii.dictionary_entry_uuid = :uuid and ii.id not in (:excludeIds) group by ii.key_value"
            + ") mi on i.id = mi.max_id "
            + "where i.key_value in (:keys)", nativeQuery = true)
    List<IndexEntry> _internal_findAllPreviousIndexEntries(
            @Param("uuid") String dictionaryEntryUuid,
            @Param("keys") List<String> keyValues,
            @Param("excludeIds") List<Long> excludeIds);

    /**
     * Search previous entries, with support for large data volumes, ignoring existing entries
     *
     * @param dictionaryEntry current DictionaryEntry
     * @param index           current index which previous entries are required
     * @return previous index content, for HR generate
     */
    default Map<String, IndexEntry> findAllPreviousIndexEntriesExcludingExisting(
            DictionaryEntry dictionaryEntry,
            List<IndexEntry> index) {

        // Do not attempt to select with an empty "in"
        if (index == null || index.isEmpty()) {
            return new HashMap<>();
        }

        // If less than 1000 items, direct call
        if (index.size() < 1000) {
            return _internal_findAllPreviousIndexEntries(
                    dictionaryEntry.getUuid().toString(),
                    index.stream().map(DiffLine::getKeyValue).collect(Collectors.toList()),
                    index.stream().map(IndexEntry::getId).collect(Collectors.toList())).stream()
                    .collect(Collectors.toMap(IndexEntry::getKeyValue, v -> v));
        }

        // Else need to split list partitions
        Map<String, IndexEntry> result = new HashMap<>();

        Lists.partition(index, 999).forEach(
                i -> result.putAll(_internal_findAllPreviousIndexEntries(
                        dictionaryEntry.getUuid().toString(),
                        i.stream().map(DiffLine::getKeyValue).collect(Collectors.toList()),
                        i.stream().map(IndexEntry::getId).collect(Collectors.toList())).stream()
                        .collect(Collectors.toMap(IndexEntry::getKeyValue, v -> v))));

        return result;
    }
}
