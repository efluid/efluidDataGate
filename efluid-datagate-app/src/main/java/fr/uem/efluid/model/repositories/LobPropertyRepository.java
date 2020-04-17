package fr.uem.efluid.model.repositories;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import fr.uem.efluid.model.entities.Commit;
import fr.uem.efluid.model.entities.LobProperty;

/**
 * @author elecomte
 * @since v0.0.1
 * @version 1
 */
public interface LobPropertyRepository extends JpaRepository<LobProperty, Long> {

	List<LobProperty> findByCommitUuidIn(List<UUID> commitUuids);

	List<LobProperty> findByCommit(Commit commit);

	LobProperty findFirstByHash(String hash);
}
