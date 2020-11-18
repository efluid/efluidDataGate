package fr.uem.efluid.model.repositories;

import com.google.common.collect.Lists;
import fr.uem.efluid.model.DiffLine;
import fr.uem.efluid.model.DiffPayloads;
import fr.uem.efluid.model.entities.DictionaryEntry;
import fr.uem.efluid.model.entities.IndexAction;
import fr.uem.efluid.model.entities.IndexEntry;
import fr.uem.efluid.utils.ApplicationException;
import fr.uem.efluid.utils.ErrorType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.io.IOException;
import java.io.Reader;
import java.sql.Clob;
import java.sql.SQLException;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * <p>
 * Core index data provider, using JPA
 * </p>
 * <p>
 * Provides some advanced extraction processes for content regenerate
 * </p>
 *
 * @author elecomte
 * @version 3
 * @since v0.0.1
 */
public interface IndexRepository extends JpaRepository<IndexEntry, Long>, JpaSpecificationExecutor<IndexEntry> {

    /**
     * For tests env reset only !
     */
    @Query(value = "DELETE FROM INDEXES", nativeQuery = true)
    @Modifying
    void dropAll();

    /**
     * <p>
     * Search for existing commit indexes
     * </p>
     */
    List<IndexEntry> findByCommitUuid(UUID commitUuid);

    /**
     * Access on knew key values
     *
     * @param dictionaryEntryUuid
     * @return
     */
    @Query(value = "SELECT DISTINCT i.KEY_VALUE FROM INDEXES i WHERE DICTIONARY_ENTRY_UUID = :dictUuid", nativeQuery = true)
    Set<String> getKeyValuesForDictionaryEntry(@Param("dictUuid") String dictionaryEntryUuid);

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
    List<IndexEntry> findWithUpgradablePreviousByCommitUuid(@Param("commitUuid") String commitUuid);

    /**
     * <p>
     * Search for existing commit indexes
     * </p>
     */
    long countByCommitUuid(UUID commitUuid);

    @Query(value = "SELECT MAX(i.timestamp) FROM INDEXES i " +
            "INNER JOIN COMMITS c on c.UUID = i.COMMIT_UUID " +
            "WHERE C.IMPORTED_TIME = (SELECT MAX(IMPORTED_TIME) FROM COMMITS c)", nativeQuery = true)
    Long findMaxIndexTimestampOfLastImportedCommit();

    /**
     * <p>
     * Load full index detail for one DictionaryEntry (= on managed table)
     * </p>
     */
    Stream<IndexEntry> findByDictionaryEntryOrderByTimestampAsc(DictionaryEntry dictionaryEntry);

    /**
     * Access on knew key values
     *
     * @param dictionaryEntryUuid
     * @return
     */
    @Query(value = "SELECT DISTINCT i.KEY_VALUE FROM INDEXES i WHERE DICTIONARY_ENTRY_UUID = :dictUuid AND TIMESTAMP <= :pivot", nativeQuery = true)
    Set<String> getKeyValuesForDictionaryEntryBefore(@Param("dictUuid") String dictionaryEntryUuid, @Param("pivot") long timestamp);

    @Query(value = "SELECT i.KEY_VALUE, i.PAYLOAD FROM INDEXES i " +
            "INNER JOIN (SELECT MAX(TIMESTAMP) AS TS, KEY_VALUE FROM INDEXES WHERE DICTIONARY_ENTRY_UUID = :dictUuid AND KEY_VALUE IN :keys GROUP BY KEY_VALUE) ii ON i.TIMESTAMP = ii.TS AND i.KEY_VALUE = ii.KEY_VALUE " +
            "WHERE i.DICTIONARY_ENTRY_UUID = :dictUuid AND i.ACTION != 'REMOVE'", nativeQuery = true)
    Stream<Object[]> _internal_findRegeneratedContentForDictionaryEntryAndBuffer(@Param("dictUuid") String dictionaryEntryUuid, @Param("keys") Collection<String> keys);

    @Query(value = "SELECT i.KEY_VALUE, i.PAYLOAD, i.PREVIOUS FROM INDEXES i " +
            "INNER JOIN (SELECT MAX(TIMESTAMP) AS TS, KEY_VALUE FROM INDEXES WHERE DICTIONARY_ENTRY_UUID = :dictUuid AND KEY_VALUE IN :keys GROUP BY KEY_VALUE) ii ON i.TIMESTAMP = ii.TS AND i.KEY_VALUE = ii.KEY_VALUE " +
            "WHERE i.DICTIONARY_ENTRY_UUID = :dictUuid", nativeQuery = true)
    Stream<ProjectedDiffPayloads> findDiffPayloadsForDictionaryEntryAndBuffer(@Param("dictUuid") String dictionaryEntryUuid, @Param("keys") Collection<String> keys);

    /**
     * For new Knew content regenerate on buffered diff generation
     *
     * @param dictionaryEntryUuid
     * @param keys
     * @return knew content mapped to their keys
     */
    default Map<String, String> findRegeneratedContentForDictionaryEntryAndBuffer(UUID dictionaryEntryUuid, Collection<String> keys) {
        Map<String, String> result = new HashMap<>(keys.size());
        _internal_findRegeneratedContentForDictionaryEntryAndBuffer(dictionaryEntryUuid.toString(), keys)
                .forEach(projectionAsContentMap(result));
        return result;
    }

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

    private Consumer<Object[]> projectionAsContentMap(Map<String, String> result) {
        return (l) -> {
            if (l[0] != null) {
                // Internal convert to managed string - would fail if content > 2^32 bits
                Clob content = (Clob) l[1];
                result.put(l[0].toString(), clobToString(content, 1024).toString());
            }
        };
    }

    private static String clobToString(Clob clob, int bufferSize) {
        StringBuilder stringBuilder = new StringBuilder(bufferSize);
        try (Reader reader = clob.getCharacterStream()) {
            char[] buffer = new char[bufferSize];
            while (true) {
                int amountRead = reader.read(buffer, 0, bufferSize);
                if (amountRead == -1) {
                    return stringBuilder.toString();
                }
                stringBuilder.append(buffer, 0, amountRead);
            }
        } catch (IOException | SQLException e) {
            throw new ApplicationException(ErrorType.REGENERATE_ERROR, "Couldn't get current content clob as string", e);
        }
    }

    /**
     * Specific static projection compliant implementation of DiffPayloads for the
     * simplest access to the index entries payloads
     *
     * @author elecomte
     * @version 1
     * @since v2.1.7
     */
    class ProjectedDiffPayloads implements DiffPayloads {

        private final String keyValue;
        private final String payload;
        private final String previous;

        public ProjectedDiffPayloads(String keyValue, String payload, String previous) {
            this.keyValue = keyValue;
            this.payload = payload;
            this.previous = previous;
        }

        @Override
        public String getKeyValue() {
            return keyValue;
        }

        @Override
        public String getPayload() {
            return payload;
        }

        @Override
        public String getPrevious() {
            return previous;
        }
    }
}
