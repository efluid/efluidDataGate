package fr.uem.efluid.model.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

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
	 * Load full index detail for one DictionaryEntry (= on managed table)
	 * </p>
	 * 
	 * @param dictionaryEntry
	 * @return
	 */
	List<IndexEntry> findByDictionaryEntryOrderByTimestamp(DictionaryEntry dictionaryEntry);
}
