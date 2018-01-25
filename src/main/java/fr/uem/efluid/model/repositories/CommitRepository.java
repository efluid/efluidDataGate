package fr.uem.efluid.model.repositories;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import fr.uem.efluid.model.entities.Commit;

/**
 * @author elecomte
 * @since v0.0.1
 * @version 1
 */
public interface CommitRepository extends JpaRepository<Commit, UUID> {

	// Crud features extensions
}
