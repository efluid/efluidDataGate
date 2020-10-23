package fr.uem.efluid.model.repositories.impls;

import fr.uem.efluid.model.ContentLine;
import fr.uem.efluid.model.DiffLine;
import fr.uem.efluid.model.entities.DictionaryEntry;
import fr.uem.efluid.model.entities.IndexAction;
import fr.uem.efluid.model.repositories.IndexRepository;
import fr.uem.efluid.model.repositories.ManagedRegenerateRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * <p>
 * Regenerate data from index
 * </p>
 *
 * @author elecomte
 * @version 1
 * @since v0.0.1
 */
@Repository
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

        // Will process backlog by its natural order
        return regenerateKnewContent(this.coreIndex.findByDictionaryEntryOrderByTimestampAsc(parameterEntry), null);
    }

    /**
     * @param specifiedIndex
     * @return
     */
    @Override
    public <D extends DiffLine> Map<String, String> regenerateKnewContent(
            final Stream<D> specifiedIndex,
            final Consumer<D> eachLineAccumulator) {

        LOGGER.debug("Regenerating values from specified index");

        // Content for playing back the backlog
        final Map<String, String> lines = new HashMap<>(10000);

        // Cached check on accumulator process
        boolean acc = eachLineAccumulator != null;

        // Sorting must be specified at list level as the order may be non
        // consistent regarding database model for timestamp based sort

        // Switch process with minimal check on accumulator content
        if (eachLineAccumulator != null) {
            specifiedIndex.forEach(buildKnewContentAndAccumulate(lines, eachLineAccumulator));
        } else {
            specifiedIndex.forEach(buildKnewContent(lines));
        }

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

    /**
     * Embed accumulator call for limited stream peek / forEach process
     */
    private static <D extends DiffLine> Consumer<D> buildKnewContentAndAccumulate(
            final Map<String, String> lines,
            final Consumer<D> eachLineAccumulator) {
        return line -> {
            eachLineAccumulator.accept(line);
            // Addition : add / update directly
            if (line.getAction() == IndexAction.ADD || line.getAction() == IndexAction.UPDATE) {
                lines.put(line.getKeyValue(), line.getPayload());
            } else {
                lines.remove(line.getKeyValue());
            }
        };
    }
}
