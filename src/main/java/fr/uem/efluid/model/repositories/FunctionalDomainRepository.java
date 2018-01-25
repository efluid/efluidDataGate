package fr.uem.efluid.model.repositories;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import fr.uem.efluid.model.entities.FunctionalDomain;

/**
 * @author elecomte
 * @since v0.0.1
 * @version 1
 */
public interface FunctionalDomainRepository extends JpaRepository<FunctionalDomain, UUID> {

	@Query("SELECT DISTINCT do.uuid FROM IndexEntry ind INNER JOIN ind.dictionaryEntry.domain do GROUP BY do.uuid")
	List<UUID> findUsedIds();
}
