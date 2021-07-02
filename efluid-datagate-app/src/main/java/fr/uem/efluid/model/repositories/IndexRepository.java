package fr.uem.efluid.model.repositories;

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
import org.springframework.data.util.Pair;

import java.io.IOException;
import java.io.Reader;
import java.sql.Clob;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * <p>
 * Core index data provider, using JPA. Index is managed in a flat format, with :
 * <ul>
 *      <li>Table identifier</li>
 *      <li>Corresponding key value (could be a composite key, combined in a single value)</li>
 *      <li>Payload in internal format (see payload builder tools)</li>
 *      <li>Timestamp</li>
 *      <li>Associated commit</li>
 *      <li>Previous payload</li>
 *      <li>Change type</li>
 * </ul>
 * </p>
 * <p>
 * Provides some advanced extraction processes for content regenerate and commit compare process
 * </p>
 *
 * @author elecomte
 * @version 4
 * @since v0.0.1
 */
public interface IndexRepository extends JpaRepository<IndexEntry, Long>, JpaSpecificationExecutor<IndexEntry> {

    @Query(value = "UPDATE INDEXES SET DICTIONARY_ENTRY_UUID = :newEntry WHERE DICTIONARY_ENTRY_UUID = :existing", nativeQuery = true)
    @Modifying
    void updateDictionaryEntryReference(UUID existing, UUID newEntry);

    /**
     * For tests env reset only ! Drop everything !!!
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
     * For export process, get specified index in stream
     *
     * @param commitUuids commit uuids
     * @return corresponding entries, full load
     */
    Stream<IndexEntry> findByCommitUuidInOrderByTimestamp(Collection<UUID> commitUuids);

    /**
     * Access on knew key values
     *
     * @param dictionaryEntryUuid corresponding dictEntry uuid
     * @return keys for specified dict entry
     */
    @Query(value = "SELECT DISTINCT i.KEY_VALUE FROM INDEXES i WHERE DICTIONARY_ENTRY_UUID = :dictUuid", nativeQuery = true)
    Set<String> getKeyValuesForDictionaryEntry(@Param("dictUuid") String dictionaryEntryUuid);

    /**
     * <p>
     * Search for existing commit indexes
     * </p>
     */
    Stream<IndexEntry> findByCommitUuidAndDictionaryEntry(UUID commitUuid, DictionaryEntry dictionaryEntry);

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

    @Query(value = "SELECT MAX(i.timestamp) FROM INDEXES i " +
            "WHERE i.COMMIT_UUID = :commitUuid", nativeQuery = true)
    Long findMaxIndexTimestampOfCommit(@Param("commitUuid") String commitUuid);

    /**
     * Access on knew key values
     *
     * @param dictionaryEntryUuid corresponding dictEntry uuid before pivot time
     * @param timestamp           limit for key search in time (pivot time)
     * @return all keys for selected dict
     */
    @Query(value = "SELECT DISTINCT i.KEY_VALUE FROM INDEXES i WHERE DICTIONARY_ENTRY_UUID = :dictUuid AND TIMESTAMP <= :pivot", nativeQuery = true)
    Set<String> getKeyValuesForDictionaryEntryBefore(@Param("dictUuid") String dictionaryEntryUuid, @Param("pivot") long timestamp);

    /**
     * Get the common keys for two commits, on specified table
     *
     * @param commitOneUuid
     * @param commitTwoUuid
     * @param dictionaryEntryUuid
     * @return
     */
    @Query(value = "SELECT DISTINCT iOne.KEY_VALUE " +
            "FROM INDEXES iOne " +
            "INNER JOIN INDEXES iTwo ON iTwo.KEY_VALUE = iOne.KEY_VALUE AND iTwo.DICTIONARY_ENTRY_UUID = iOne.DICTIONARY_ENTRY_UUID AND iTwo.COMMIT_UUID = :commitTwoUuid " +
            "WHERE iOne.DICTIONARY_ENTRY_UUID = :dictUuid AND iOne.COMMIT_UUID = :commitOneUuid ", nativeQuery = true)
    Set<String> getCommonKeyValuesForCommitAndDictionaryEntry(
            @Param("commitOneUuid") String commitOneUuid,
            @Param("commitTwoUuid") String commitTwoUuid,
            @Param("dictUuid") String dictionaryEntryUuid);

    /* ####################################### Queries for manual projection ####################################### */

    /**
     * <b><font color="red">Query for internal use only</font></b>
     */
    @Query(value = "SELECT i.KEY_VALUE, i.PAYLOAD FROM INDEXES i " +
            "INNER JOIN (SELECT MAX(TIMESTAMP) AS TS, KEY_VALUE FROM INDEXES WHERE DICTIONARY_ENTRY_UUID = :dictUuid AND TIMESTAMP <= :pivot AND KEY_VALUE IN :keys GROUP BY KEY_VALUE) ii ON i.TIMESTAMP = ii.TS AND i.KEY_VALUE = ii.KEY_VALUE " +
            "WHERE i.DICTIONARY_ENTRY_UUID = :dictUuid AND i.ACTION != 'REMOVE'", nativeQuery = true)
    List<Object[]> _internal_findRegeneratedContentForDictionaryEntryAndBufferBefore(
            @Param("dictUuid") String dictionaryEntryUuid,
            @Param("keys") Collection<String> keys,
            @Param("pivot") long pivot);

    /**
     * <b><font color="red">Query for internal use only</font></b>
     */
    @Query(value = "SELECT i.KEY_VALUE as keyValue, i.PAYLOAD as payload, i.PREVIOUS as previous FROM INDEXES i " +
            "INNER JOIN (SELECT MAX(TIMESTAMP) AS TS, KEY_VALUE FROM INDEXES WHERE DICTIONARY_ENTRY_UUID = :dictUuid AND TIMESTAMP <= :pivot AND KEY_VALUE IN :keys GROUP BY KEY_VALUE) ii ON i.TIMESTAMP = ii.TS AND i.KEY_VALUE = ii.KEY_VALUE " +
            "WHERE i.DICTIONARY_ENTRY_UUID = :dictUuid", nativeQuery = true)
    List<Object[]> _internal_findDiffPayloadsForDictionaryEntryAndBufferBefore(
            @Param("dictUuid") String dictionaryEntryUuid,
            @Param("keys") Collection<String> keys,
            @Param("pivot") long pivot);

    /**
     * <b><font color="red">Query for internal use only</font></b>
     */
    @Query(value = "select i.* "
            + "from indexes i "
            + "inner join ("
            + "	select max(ii.id) as max_id, ii.key_value from indexes ii where ii.dictionary_entry_uuid = :uuid and ii.id not in (:excludeIds) group by ii.key_value"
            + ") mi on i.id = mi.max_id "
            + "where i.key_value in (:keys)", nativeQuery = true)
    List<IndexEntry> _internal_findAllPreviousIndexEntries(@Param("uuid") String dictionaryEntryUuid, @Param("keys") List<String> keyValues, @Param("excludeIds") List<Long> excludeIds);

    /**
     * <b><font color="red">Query for internal use only</font></b>
     * <p>
     * Projection :
     * key = rawLine[0]
     * action = rawLine[1]
     * timestamp = rawLine[2]
     * payload = rawLine[3]
     * previous = rawLine[4]
     * </p>
     */
    @Query(value = "SELECT " +
            "     i.KEY_VALUE as keyValue, " +
            "     i.ACTION as action, " +
            "     i.TIMESTAMP as timestamp, " +
            "     i.PAYLOAD as payload, " +
            "     i.PREVIOUS as previous, " +
            "     i.COMMIT_UUID as commit " +
            "FROM INDEXES i " +
            "WHERE DICTIONARY_ENTRY_UUID = :dictUuid " +
            "AND TIMESTAMP > :start AND TIMESTAMP <= :end " +
            "AND KEY_VALUE IN :keys ORDER BY i.TIMESTAMP ASC", nativeQuery = true)
    List<Object[]> _internal_findDiffPayloadsForKeysOnDictionaryEntryBetween(
            @Param("dictUuid") String dictionaryEntryUuid,
            @Param("keys") Collection<String> keys,
            @Param("start") long start,
            @Param("end") long end);

    /* ############################ Internal projection processes (performance related) ########################### */

    /**
     * Get projected payload mapped to key for specified keys
     *
     * <b><font color="red">Query for internal use only</font></b>
     *
     * @param keys selected keys to check
     * @return updated payload mapped to their keys
     */
    default Map<String, DiffPayloads> findDiffPayloadsForDictionaryEntryAndBufferBefore(UUID dictionaryEntryUuid, Collection<String> keys, long pivot) {
        Map<String, DiffPayloads> result = new HashMap<>();

        chop(keys, 999).forEach(
                i -> _internal_findDiffPayloadsForDictionaryEntryAndBufferBefore(dictionaryEntryUuid.toString(), i, pivot)
                        .forEach(projectionAsProjectedDiffPayloadMap(result))
        );

        return result;
    }

    /**
     * For new Knew content regenerate on buffered diff generation
     *
     * @param dictionaryEntryUuid corresponding dictEntry uuid
     * @param keys                selected keys to check
     * @param pivotTime           time scope range before which payloads are loaded
     * @return knew content mapped to their keys
     */
    default Map<String, String> findRegeneratedContentForDictionaryEntryAndBufferBefore(UUID dictionaryEntryUuid, Collection<String> keys, long pivotTime) {
        Map<String, String> result = new HashMap<>(keys.size());

        chop(keys, 999).forEach(
                i -> _internal_findRegeneratedContentForDictionaryEntryAndBufferBefore(dictionaryEntryUuid.toString(), i, pivotTime)
                        .forEach(projectionAsContentMap(result)));

        return result;
    }

    /**
     * Get projected payload mapped to key for specified keys between two timestamps
     *
     * @param dictionaryEntryUuid corresponding dictEntry uuid
     * @param keys                selected keys to check
     * @param start               time range start
     * @param end                 time range end
     * @return payload list in range, mapped to their keys, organized in line by commits
     */
    default Map<String, List<Pair<UUID, DiffLine>>> findDiffLinesForKeysOnDictionaryEntryBetweenTimeRangeMappedToCommit(
            UUID dictionaryEntryUuid,
            Collection<String> keys,
            long start, long end) {
        Map<String, List<Pair<UUID, DiffLine>>> result = new HashMap<>();

        chop(keys, 999).forEach(
                i -> _internal_findDiffPayloadsForKeysOnDictionaryEntryBetween(dictionaryEntryUuid.toString(), i, start, end)
                        .forEach(projectionAsProjectedDiffLineCommitMap(result, dictionaryEntryUuid))
        );

        return result;
    }

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

        chop(index, 999).forEach(
                i -> result.putAll(_internal_findAllPreviousIndexEntries(
                        dictionaryEntry.getUuid().toString(),
                        i.stream().map(DiffLine::getKeyValue).collect(Collectors.toList()),
                        i.stream().map(IndexEntry::getId).collect(Collectors.toList())).stream()
                        .collect(Collectors.toMap(IndexEntry::getKeyValue, v -> v))));

        return result;
    }

    /* ################### PROJECTION TOOLS FOR PAYLOAD / CONTENT REBUILD FROM RAW INDEX #################### */

    private static Consumer<Object[]> projectionAsContentMap(Map<String, String> result) {
        return (l) -> {
            if (l != null) {
                // Internal convert to managed string - would fail if content > 2^32 bits
                Clob content = (Clob) l[1];
                result.put(l[0] != null ? l[0].toString() : null, clobToString(content, 1024));
            }
        };
    }

    private Consumer<Object[]> projectionAsProjectedDiffPayloadMap(Map<String, DiffPayloads> result) {
        return (rawLine) -> {
            if (rawLine[0] != null) {
                String key = rawLine[0].toString();
                // Internal convert of both payloads to managed string - would fail if content > 2^32 bits
                Clob payload = (Clob) rawLine[1];
                Clob previous = (Clob) rawLine[2];
                result.put(key, new ProjectedDiffPayloads(key, clobToString(payload, 1024), clobToString(previous, 512)));
            }
        };
    }

    private Consumer<Object[]> projectionAsProjectedDiffLineCommitMap(Map<String, List<Pair<UUID, DiffLine>>> result, final UUID dictionaryEntryUuid) {
        return (rawLine) -> {
            if (rawLine[0] != null) {
                final String key = rawLine[0].toString();
                final IndexAction action = IndexAction.valueOf(rawLine[1].toString().toUpperCase(Locale.ROOT));
                final long timestamp = Long.parseLong(rawLine[2].toString());

                // Internal convert of both payloads to managed string - would fail if content > 2^32 bits
                final String payload = clobToString((Clob) rawLine[3], 1024);
                final String previous = clobToString((Clob) rawLine[4], 512);

                final String commitUuid = rawLine[5].toString();

                // Directly project to
                result.computeIfAbsent(key, k -> new ArrayList<>())
                        .add(Pair.of(UUID.fromString(commitUuid), new DiffLine() {
                            @Override
                            public UUID getDictionaryEntryUuid() {
                                return dictionaryEntryUuid;
                            }

                            @Override
                            public IndexAction getAction() {
                                return action;
                            }

                            @Override
                            public long getTimestamp() {
                                return timestamp;
                            }

                            @Override
                            public String getPrevious() {
                                return previous;
                            }

                            @Override
                            public String getKeyValue() {
                                return key;
                            }

                            @Override
                            public String getPayload() {
                                return payload;
                            }
                        }));
            }
        };
    }

    /* ################### HELPERS FOR CONTENT CONVERT AND PROJECTION BUILD #################### */

    private static String clobToString(Clob clob, int bufferSize) {
        if (clob == null) {
            return null;
        }
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

    private static <T> Collection<List<T>> chop(Collection<T> inputList, int size) {
        final AtomicInteger counter = new AtomicInteger(0);
        return inputList.stream()
                .collect(Collectors.groupingBy(s -> counter.getAndIncrement() / size))
                .values();
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
