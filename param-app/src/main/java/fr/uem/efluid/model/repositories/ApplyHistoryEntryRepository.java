package fr.uem.efluid.model.repositories;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import fr.uem.efluid.model.entities.ApplyHistoryEntry;

/**
 * @author elecomte
 * @since v0.0.1
 * @version 1
 */
public interface ApplyHistoryEntryRepository extends JpaRepository<ApplyHistoryEntry, Long> {

	/**
	 * <p>
	 * Search for query content, paginated
	 * </p>
	 * 
	 * @param query
	 * @param pageable
	 * @return
	 */
	Page<ApplyHistoryEntry> findByQueryLikeOrderByTimestampAsc(String query, Pageable pageable);

}
