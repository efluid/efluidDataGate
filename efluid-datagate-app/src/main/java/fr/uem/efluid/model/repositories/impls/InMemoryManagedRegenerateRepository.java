package fr.uem.efluid.model.repositories.impls;

import fr.uem.efluid.model.DiffLine;
import fr.uem.efluid.model.entities.DictionaryEntry;
import fr.uem.efluid.model.entities.IndexAction;
import fr.uem.efluid.model.repositories.IndexRepository;
import fr.uem.efluid.model.repositories.ManagedRegenerateRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

/**
 * <p>
 * Regenerate data from index
 * </p>
 *
 * @author elecomte
 * @version 2
 * @since v0.0.1
 */
@Transactional
public class InMemoryManagedRegenerateRepository implements ManagedRegenerateRepository {

    private static final Logger LOGGER = LoggerFactory.getLogger(InMemoryManagedRegenerateRepository.class);

    @Autowired
    private IndexRepository coreIndex;

    /**
     * Produces the knew content for specified table, from recorded index
     *
     * @param parameterEntry
     * @return
     */
    @Override
    public Map<String, String> regenerateKnewContent(DictionaryEntry parameterEntry) {

        LOGGER.debug("Regenerating values from local index for managed table {}", parameterEntry.getTableName());

        final Map<String, String> lines = new HashMap<>(10000);

        // Will process backlog by its natural order
        this.coreIndex.findByDictionaryEntryOrderByTimestampAsc(parameterEntry)
                .forEach(buildKnewContent(lines));

        return lines;
    }

    @Override
    public Map<String, String> regenerateKnewContentBefore(
            DictionaryEntry dictionaryEntry,
            long before,
            final Consumer<DiffLine> eachLineAccumulator) {

        LOGGER.debug("Regenerating values for table {} before {}", dictionaryEntry.getTableName(), before);

        // Content for playing back the backlog
        final Map<String, String> lines = new HashMap<>(10000);

        this.coreIndex.findByDictionaryEntryAndTimestampLessThanEqualOrderByTimestampAsc(dictionaryEntry, before)
                .peek(eachLineAccumulator)
                .forEach(buildKnewContent(lines));

        return lines;
    }

    /**
     * @see fr.uem.efluid.model.repositories.ManagedRegenerateRepository#refreshAll()
     */
    @Override
    public void refreshAll() {
        LOGGER.info("Regenerate cache droped. Will extract fresh data on next call");
    }

    /**
     * Single process for knewContentBuild
     */
    private static <D extends DiffLine> Consumer<D> buildKnewContent(final Map<String, String> lines) {
        return line -> {
            // Addition : add / update directly
            if (line.getAction() == IndexAction.ADD || line.getAction() == IndexAction.UPDATE) {
                lines.put(line.getKeyValue(), line.getPayload());
            } else {
                lines.remove(line.getKeyValue());
            }
        };
    }
}
