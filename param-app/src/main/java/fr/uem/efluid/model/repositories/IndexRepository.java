package fr.uem.efluid.model.repositories;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import fr.uem.efluid.model.entities.DictionaryEntry;
import fr.uem.efluid.model.entities.IndexEntry;

/**
 * <p>
 * Core index data provider, using JPA
 * </p>
 * 
 * @author elecomte
 * @since v0.0.1
 * @version 1
 */
public interface IndexRepository extends JpaRepository<IndexEntry, Long> {

	/**
	 * <p>
	 * Search for existing commit indexes
	 * </p>
	 * 
	 * @param commitUuid
	 * @return
	 */
	List<IndexEntry> findByCommitUuid(UUID commitUuid);

	/**
	 * <p>
	 * Load full index detail for one DictionaryEntry (= on managed table)
	 * </p>
	 * 
	 * @param dictionaryEntry
	 * @return
	 */
	List<IndexEntry> findByDictionaryEntryOrderByTimestamp(DictionaryEntry dictionaryEntry);

	/**
	 * @param dictionaryEntry
	 * @param timestamp
	 * @return
	 */
	List<IndexEntry> findByDictionaryEntryAndTimestampGreaterThanEqualOrderByTimestamp(DictionaryEntry dictionaryEntry, long timestamp);

	/**
	 * <b><font color="red">Query for internal use only</font></b>
	 * 
	 * @param dictionaryEntryUuid
	 * @param keyValues
	 * @return
	 */
	// TODO : Once with java9, specify as private
	@Query(value = "select i.* "
			+ "from index i "
			+ "inner join ("
			+ "	select max(ii.id) as max_id, ii.key_value from index ii where ii.dictionary_entry_uuid = :uuid group by ii.key_value"
			+ ") mi on i.id = mi.max_id "
			+ "where i.key_value in (:keys)", nativeQuery = true)
	List<IndexEntry> _internal_findAllPreviousIndexEntries(
			@Param("uuid") UUID dictionaryEntryUuid,
			@Param("keys") List<String> keyValues);

	/**
	 * <b><font color="red">Query for internal use only</font></b>
	 * 
	 * @param dictionaryEntryUuid
	 * @param keyValues
	 * @param excludeIds
	 * @return
	 */
	// TODO : Once with java9, specify as private
	@Query(value = "select i.* "
			+ "from index i "
			+ "inner join ("
			+ "	select max(ii.id) as max_id, ii.key_value from index ii where ii.dictionary_entry_uuid = :uuid and ii.id not in (:excludeIds) group by ii.key_value"
			+ ") mi on i.id = mi.max_id "
			+ "where i.key_value in (:keys)", nativeQuery = true)
	List<IndexEntry> _internal_findAllPreviousIndexEntries(
			@Param("uuid") UUID dictionaryEntryUuid,
			@Param("keys") List<String> keyValues,
			@Param("excludeIds") List<Long> excludeIds);

	/**
	 * <p>
	 * Get the "last" IndexEntry for each given KeyValue, using the specified
	 * DictionaryEntry as scope select.
	 * </p>
	 * 
	 * @param dictionaryEntry
	 * @param keyValues
	 * @param excludeIds
	 *            ids of IndexEntries to exclude. Can be null or empty
	 * @return
	 */
	default Map<String, IndexEntry> findAllPreviousIndexEntries(
			DictionaryEntry dictionaryEntry,
			List<String> keyValues,
			List<Long> excludeIds) {

		// Do not attempt to select with an empty "in"
		if (keyValues == null || keyValues.isEmpty()) {
			return new HashMap<>();
		}

		if (excludeIds == null || excludeIds.isEmpty()) {
			return _internal_findAllPreviousIndexEntries(dictionaryEntry.getUuid(), keyValues).stream()
					.collect(Collectors.toMap(IndexEntry::getKeyValue, v -> v));
		}

		return _internal_findAllPreviousIndexEntries(dictionaryEntry.getUuid(), keyValues, excludeIds).stream()
				.collect(Collectors.toMap(IndexEntry::getKeyValue, v -> v));
	}
}
