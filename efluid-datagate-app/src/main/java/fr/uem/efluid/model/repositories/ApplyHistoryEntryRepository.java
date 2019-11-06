package fr.uem.efluid.model.repositories;

import fr.uem.efluid.model.entities.ApplyHistoryEntry;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * @author elecomte
 * @version 1
 * @since v0.0.1
 */
public interface ApplyHistoryEntryRepository extends JpaRepository<ApplyHistoryEntry, Long> {

    /**
     * <p>
     * Search for query content, paginated
     * </p>
     *
     * @param query    processed query content in history
     * @param pageable Page detail
     * @return Page of ApplyHistoryEntry
     */
    Page<ApplyHistoryEntry> findByQueryLikeOrderByTimestampDesc(String query, Pageable pageable);

}
