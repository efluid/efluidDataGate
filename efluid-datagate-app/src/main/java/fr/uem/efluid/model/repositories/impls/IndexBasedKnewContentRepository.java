package fr.uem.efluid.model.repositories.impls;

import fr.uem.efluid.model.ContentLine;
import fr.uem.efluid.model.DiffPayloads;
import fr.uem.efluid.model.entities.DictionaryEntry;
import fr.uem.efluid.model.repositories.IndexRepository;
import fr.uem.efluid.model.repositories.KnewContentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Transactional
@Repository
public class IndexBasedKnewContentRepository implements KnewContentRepository {

    private final IndexRepository indexes;

    public IndexBasedKnewContentRepository(@Autowired IndexRepository indexes) {
        this.indexes = indexes;
    }

    @Override
    public Set<String> knewContentKeys(DictionaryEntry dictionaryEntry) {
        return this.indexes.getKeyValuesForDictionaryEntry(dictionaryEntry.getUuid().toString());
    }

    @Override
    public Set<String> knewContentKeysBefore(DictionaryEntry dictionaryEntry, long timestamp) {
        return this.indexes.getKeyValuesForDictionaryEntryBefore(dictionaryEntry.getUuid().toString(), timestamp);
    }

    @Override
    public Map<String, String> knewContentForKeys(DictionaryEntry dictionaryEntry, Collection<String> keys) {

        // Regenerate at DB level the corresponding content for these keys
        return this.indexes.findRegeneratedContentForDictionaryEntryAndBuffer(dictionaryEntry.getUuid(), keys);
    }

    @Override
    public Map<String, DiffPayloads> knewContentPayloadsForKeys(DictionaryEntry dictionaryEntry, Collection<String> keys) {
        return this.indexes.findDiffPayloadsForDictionaryEntryAndBuffer(dictionaryEntry.getUuid().toString(), keys)
                .collect(Collectors.toMap(DiffPayloads::getKeyValue, i -> i));
    }
}
