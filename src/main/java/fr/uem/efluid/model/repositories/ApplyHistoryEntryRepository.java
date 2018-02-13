package fr.uem.efluid.model.repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import fr.uem.efluid.model.entities.ApplyHistoryEntry;

/**
 * @author elecomte
 * @since v0.0.1
 * @version 1
 */
public interface ApplyHistoryEntryRepository extends JpaRepository<ApplyHistoryEntry, Long> {

	// Use only basic crud
}
