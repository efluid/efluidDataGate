package fr.uem.efluid.model.repositories;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import fr.uem.efluid.model.entities.DictionaryEntry;

/**
 * @author elecomte
 * @since v0.0.1
 * @version 1
 */
public interface DictionaryRepository extends JpaRepository<DictionaryEntry, UUID> {

	@Query("SELECT DISTINCT dic.uuid FROM IndexEntry ind INNER JOIN ind.dictionaryEntry dic GROUP BY dic.uuid")
	List<UUID> findUsedIds();
}
