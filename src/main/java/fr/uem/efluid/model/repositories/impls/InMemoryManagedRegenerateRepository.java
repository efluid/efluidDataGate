package fr.uem.efluid.model.repositories.impls;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import fr.uem.efluid.model.DiffLine;
import fr.uem.efluid.model.entities.DictionaryEntry;
import fr.uem.efluid.model.entities.IndexAction;
import fr.uem.efluid.model.repositories.IndexRepository;
import fr.uem.efluid.model.repositories.ManagedRegenerateRepository;

/**
 * <p>
 * Regenerate data from index
 * </p>
 * 
 * @author elecomte
 * @since v0.0.1
 * @version 1
 */
@Repository
public class InMemoryManagedRegenerateRepository implements ManagedRegenerateRepository {

	private static final Logger LOGGER = LoggerFactory.getLogger(InMemoryManagedRegenerateRepository.class);

	// TODO : check cache issue : seems to have pb (maybe related to concurrency ?)

	@Autowired
	private IndexRepository coreIndex;

	/**
	 * Produces the knew content for specified table, from recorded index
	 * 
	 * @param parameterEntry
	 * @return
	 */
	@Override
	// @Cacheable(cacheNames = "regenerated", key="#parameterEntry")
	public Map<String, String> regenerateKnewContent(DictionaryEntry parameterEntry) {

		LOGGER.debug("Regenerating values from local index for managed table {}", parameterEntry.getTableName());

		// Will process backlog by its natural order
		return regenerateKnewContent(this.coreIndex.findByDictionaryEntryOrderByTimestamp(parameterEntry));
	}

	/**
	 * @param parameterEntry
	 * @param specifiedIndex
	 * @return
	 * @see fr.uem.efluid.model.repositories.ManagedRegenerateRepository#regenerateKnewContent(fr.uem.efluid.model.entities.DictionaryEntry,
	 *      java.util.List)
	 */
	@Override
	public Map<String, String> regenerateKnewContent(List<? extends DiffLine> specifiedIndex) {

		LOGGER.debug("Regenerating values from specified index with {} items", Integer.valueOf(specifiedIndex.size()));

		// Content for playing back the backlog
		Map<String, String> lines = new ConcurrentHashMap<>(1000);

		for (DiffLine line : specifiedIndex) {

			// Addition : add / update directly
			if (line.getAction() == IndexAction.ADD || line.getAction() == IndexAction.UPDATE) {
				lines.put(line.getKeyValue(), line.getPayload());
			}

			else {
				lines.remove(line.getKeyValue());
			}
		}

		return lines;
	}

	/**
	 * 
	 * @see fr.uem.efluid.model.repositories.ManagedRegenerateRepository#refreshAll()
	 */
	@Override
	// @CacheEvict(cacheNames = "regenerated", allEntries = true)
	public void refreshAll() {
		LOGGER.info("Regenerate cache droped. Will extract fresh data on next call");
	}
}
