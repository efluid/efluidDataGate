package fr.uem.efluid.model.repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import fr.uem.efluid.model.entities.IndexEntry;

/**
 * @author elecomte
 * @since v0.0.1
 * @version 1
 */
public interface IndexRepository extends JpaRepository<IndexEntry, Long> {

	// Default for index management
}
