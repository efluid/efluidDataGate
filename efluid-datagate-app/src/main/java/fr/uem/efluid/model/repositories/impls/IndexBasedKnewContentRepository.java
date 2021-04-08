package fr.uem.efluid.model.repositories.impls;

import fr.uem.efluid.model.DiffPayloads;
import fr.uem.efluid.model.entities.DictionaryEntry;
import fr.uem.efluid.model.repositories.IndexRepository;
import fr.uem.efluid.model.repositories.KnewContentRepository;
import fr.uem.efluid.utils.ApplicationException;
import fr.uem.efluid.utils.ErrorType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Set;

/**
 * Access to knew content : based on index database only, all the extraction are processed
 * by minimal code, with heavy database querying
 *
 * @author elecomte
 * @version 1
 * @since v2.0.17
 */
@Repository
@Transactional
public class IndexBasedKnewContentRepository implements KnewContentRepository {

    protected static final Logger LOGGER = LoggerFactory.getLogger(IndexBasedKnewContentRepository.class);

    private final IndexRepository indexes;

    public IndexBasedKnewContentRepository(@Autowired IndexRepository indexes) {
        this.indexes = indexes;
    }

    @Override
    public Set<String> knewContentKeys(DictionaryEntry dictionaryEntry) {
        try {
            return this.indexes.getKeyValuesForDictionaryEntry(dictionaryEntry.getUuid().toString());
        } catch (Throwable t) {
            LOGGER.error("Knew content key read failed for dict of table " + dictionaryEntry.getTableName(), t);
            throw new ApplicationException(ErrorType.EXTRACTION_ERROR, t);
        }
    }

    @Override
    public Set<String> knewContentKeysBefore(DictionaryEntry dictionaryEntry, long timestamp) {
        try {
            return this.indexes.getKeyValuesForDictionaryEntryBefore(dictionaryEntry.getUuid().toString(), timestamp);
        } catch (Throwable t) {
            LOGGER.error("Knew content key read before " + timestamp + " failed for dict of table " + dictionaryEntry.getTableName(), t);
            throw new ApplicationException(ErrorType.EXTRACTION_ERROR, t);
        }
    }

    @Override
    public Map<String, String> knewContentForKeysBefore(DictionaryEntry dictionaryEntry, Collection<String> keys, long timestamp) {

        if (keys == null || keys.isEmpty()) {
            return Collections.emptyMap();
        }

        try {
            // Regenerate at DB level the corresponding content for these keys
            return this.indexes.findRegeneratedContentForDictionaryEntryAndBufferBefore(dictionaryEntry.getUuid(), keys, timestamp);
        } catch (Throwable t) {
            LOGGER.error("Knew content extraction failed for dict of table " + dictionaryEntry.getTableName(), t);
            throw new ApplicationException(ErrorType.EXTRACTION_ERROR, t);
        }
    }

    @Override
    public Map<String, DiffPayloads> knewContentPayloadsForKeysBefore(DictionaryEntry dictionaryEntry, Collection<String> keys, long timestamp) {

        if (keys == null || keys.isEmpty()) {
            return Collections.emptyMap();
        }

        try {
            return this.indexes.findDiffPayloadsForDictionaryEntryAndBufferBefore(dictionaryEntry.getUuid().toString(), keys, timestamp);
        } catch (Throwable t) {
            LOGGER.error("Knew content payload extraction failed for dict of table " + dictionaryEntry.getTableName(), t);
            throw new ApplicationException(ErrorType.EXTRACTION_ERROR, t);
        }
    }
}
