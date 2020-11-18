package fr.uem.efluid.model.repositories;

import fr.uem.efluid.model.DiffPayloads;
import fr.uem.efluid.model.entities.DictionaryEntry;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

/**
 * Abstraction of the index processing, as a "knew content" page
 */
public interface KnewContentRepository {

    Set<String> knewContentKeys(DictionaryEntry dictionaryEntry);

    Map<String, String> knewContentForKeys(DictionaryEntry dictionaryEntry, Collection<String> keys);

    Set<String> knewContentKeysBefore(DictionaryEntry dictionaryEntry, long timestamp);

    Map<String, DiffPayloads> knewContentPayloadsForKeys(DictionaryEntry dictionaryEntry, Collection<String> keys) ;

}
