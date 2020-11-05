package fr.uem.efluid.model.repositories.impls;

import fr.uem.efluid.model.DiffLine;
import fr.uem.efluid.model.entities.DictionaryEntry;
import fr.uem.efluid.model.entities.IndexAction;
import fr.uem.efluid.model.repositories.IndexRepository;
import fr.uem.efluid.model.repositories.ManagedRegenerateRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Collectors;

@Transactional
@Repository
public class InDatabaseManagedRegenerateRepository implements ManagedRegenerateRepository {

    @Autowired
    private IndexRepository coreIndex;

    @Override
    public Map<String, String> regenerateKnewContent(DictionaryEntry parameterEntry) {
        return this.coreIndex.findRegeneratedContentForDictionaryEntry(parameterEntry.getUuid());
    }

    @Override
    public Map<String, String> regenerateKnewContentBefore(
            DictionaryEntry parameterEntry, long timestamp, final Consumer<DiffLine> eachLineAccumulator) {

        final Map<String, String> lines = new HashMap<>(10000);

        this.coreIndex.findAccumulableRegeneratedContentForDictionaryEntry(parameterEntry.getUuid(), timestamp)
                .peek(eachLineAccumulator) // Accumulate on all
                .forEach(line -> {
                    // Drop deleted for exact "know content" at specified time
                    if (line.getAction() != IndexAction.REMOVE) {
                        lines.put(line.getKeyValue(), line.getPayload());
                    }
                });

        return lines;
    }

    @Override
    public void refreshAll() {
        // Nothing
    }
}
