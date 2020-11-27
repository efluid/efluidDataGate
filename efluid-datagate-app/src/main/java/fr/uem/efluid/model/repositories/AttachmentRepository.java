package fr.uem.efluid.model.repositories;

import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

import org.springframework.data.jpa.repository.JpaRepository;

import fr.uem.efluid.model.entities.Attachment;
import fr.uem.efluid.model.entities.Commit;

/**
 * @author elecomte
 * @since v0.0.8
 * @version 1
 */
public interface AttachmentRepository extends JpaRepository<Attachment, UUID> {

	List<Attachment> findByCommit(Commit commit);

	Stream<Attachment> findByCommitIn(List<Commit> commit);
}
