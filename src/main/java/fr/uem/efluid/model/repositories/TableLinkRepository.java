package fr.uem.efluid.model.repositories;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import fr.uem.efluid.model.entities.DictionaryEntry;
import fr.uem.efluid.model.entities.TableLink;

/**
 * @author elecomte
 * @since v0.0.1
 * @version 1
 */
public interface TableLinkRepository extends JpaRepository<TableLink, UUID> {

	/**
	 * @param dictionaryEntry
	 * @return
	 */
	List<TableLink> findByDictionaryEntry(DictionaryEntry dictionaryEntry);
}
