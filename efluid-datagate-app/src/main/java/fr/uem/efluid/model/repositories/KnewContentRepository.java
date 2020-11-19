package fr.uem.efluid.model.repositories;

import fr.uem.efluid.model.DiffPayloads;
import fr.uem.efluid.model.entities.DictionaryEntry;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

/**
 * Abstraction of the index processing, as a "knew content" page
 * As for all extraction / index / merge / diff processing, every processes are related to a single DictionaryEntry
 * and the full diff must be generated for all table, consecutively or concurrently
 *
 * @author elecomte
 * @version 1
 * @since v2.0.17
 */
public interface KnewContentRepository {

    /**
     * Get all the keys knew in index for the specified table. Can includes keys of DELETED rows :
     * all keys of line knew at one moment are provided, without limitation in size
     *
     * @param dictionaryEntry current processed parameter table for which the keys are searched
     * @return all the keys (as managed keyValue) knew for specified table
     */
    Set<String> knewContentKeys(DictionaryEntry dictionaryEntry);

    /**
     * Get the <b>regenerated</b> knew content <b>payload</b> for the specified keys of the specified parameter table.
     *
     * @param dictionaryEntry current processed parameter table for which the content is process
     * @param keys            specified keys to search for. Allows to process content diff step by step. As this method could
     *                        be associated to direct querying in database, this should be limited to 1000 items
     * @param timestamp       pivot time. Search exactly before or equals it
     * @return regenerated knew content for the specified keys and table
     */
    Map<String, String> knewContentForKeysBefore(DictionaryEntry dictionaryEntry, Collection<String> keys, long timestamp);

    /**
     * Get all the keys knew in index for the specified table which existed before the specified pivot time. Can includes keys of DELETED rows :
     * all keys of line knew at one moment are provided, without limitation in size
     *
     * @param dictionaryEntry current processed parameter table for which the content is process
     * @param timestamp       pivot time. Search exactly before or equals it
     * @return all the keys (as managed keyValue) knew for specified table at the specified pivot time
     */
    Set<String> knewContentKeysBefore(DictionaryEntry dictionaryEntry, long timestamp);

    /**
     * Get the <b>regenerated</b> knew content for the specified keys of the specified parameter table, as full difflines. So the payload AND the previous payload are provided directly
     * Similar to {@link #knewContentForKeysBefore(DictionaryEntry, Collection, long)} but with full DiffLine content
     *
     * @param dictionaryEntry current processed parameter table for which the content is process
     * @param keys            specified keys to search for. Allows to process content diff step by step. As this method could
     *                        be associated to direct querying in database, this should be limited to 1000 items
     * @param timestamp       pivot time. Search exactly before or equals it
     * @return regenerated knew DiffPayloads for the specified keys and table
     */
    Map<String, DiffPayloads> knewContentPayloadsForKeysBefore(DictionaryEntry dictionaryEntry, Collection<String> keys, long timestamp);

}
