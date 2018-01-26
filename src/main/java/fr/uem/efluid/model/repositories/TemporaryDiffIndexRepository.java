package fr.uem.efluid.model.repositories;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import fr.uem.efluid.model.entities.IndexEntry;

/**
 * <p>
 * Temporary context for a diff before its validation and addition to index. Diff content
 * is prepared with an execution, and kept associated to an UUID for use. It can be
 * destroy once used or after a delay
 * </p>
 * 
 * @author elecomte
 * @since v0.0.1
 * @version 1
 */
public interface TemporaryDiffIndexRepository {

	/**
	 * @param identifier
	 * @param temp
	 *            Temp index mapped by DictionaryEntry UUID
	 */
	void keepTemporaryDiffIndex(UUID identifier, Map<UUID, List<IndexEntry>> temp);

	/**
	 * @param identifier
	 * @return Temp index mapped by DictionaryEntry UUID
	 */
	Map<UUID, List<IndexEntry>> getTemporaryDiffIndex(UUID identifier);

	/**
	 * @param identifier
	 */
	void dropTemporaryDiffIndex(UUID identifier);
}
