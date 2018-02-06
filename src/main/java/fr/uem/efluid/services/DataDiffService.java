package fr.uem.efluid.services;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import fr.uem.efluid.model.entities.DictionaryEntry;
import fr.uem.efluid.model.entities.IndexAction;
import fr.uem.efluid.model.repositories.ManagedParametersRepository;
import fr.uem.efluid.services.types.PreparedIndexEntry;
import fr.uem.efluid.tools.ManagedValueConverter;

/**
 * <p>
 * Core service for diff processes
 * </p>
 * 
 * @author elecomte
 * @since v0.0.1
 * @version 1
 */
@Service
@Transactional
public class DataDiffService {

	private static final Logger LOGGER = LoggerFactory.getLogger(DataDiffService.class);

	@Autowired
	private ManagedParametersRepository rawParameters;

	@Autowired
	private ManagedValueConverter valueConverter;

	private boolean useParallelDiff = false;

	/**
	 * @param dictionaryEntryUuid
	 * @return
	 */
	public Collection<PreparedIndexEntry> processDiff(DictionaryEntry entry) {

		// Here the main complexity : diff check using JDBC, for one table. Backlog
		// construction + restoration then diff.

		Map<String, String> knewContent = this.rawParameters.regenerateKnewContent(entry);
		Map<String, String> actualContent = this.rawParameters.extractCurrentContent(entry);

		return generateDiffIndex(knewContent, actualContent, entry, this.valueConverter);
	}

	/**
	 * <p>
	 * Process the map compare to provide new <tt>IndexEntry</tt> describing the diffences
	 * as additions, removals or updates.
	 * </p>
	 * <p>
	 * Their is no need for natural order process, so use parallel processes whenever it's
	 * possible.
	 * </p>
	 * 
	 * @param knewContent
	 * @param actualContent
	 * @param dic
	 * @param converter
	 *            specified as method imput as this process need to be extensible AND
	 *            easily testable
	 * @return
	 */
	protected Collection<PreparedIndexEntry> generateDiffIndex(
			final Map<String, String> knewContent,
			final Map<String, String> actualContent,
			DictionaryEntry dic,
			ManagedValueConverter converter) {

		final Set<PreparedIndexEntry> diff = ConcurrentHashMap.newKeySet();

		// Todo : add also atomic counters for summary of updates
		final boolean debug = LOGGER.isDebugEnabled();

		// Use parallel process for higher distribution of verification
		switchedStream(actualContent).forEach(actualOne -> searchDiff(actualOne, knewContent, diff, dic, converter, debug));

		// Remaining in knewContent are deleted ones
		switchedStream(knewContent).forEach(e -> {
			if (debug) {
				LOGGER.debug("New endex entry for {} : REMOVE from \"{}\"", e.getKey(), e.getValue());
			}
			diff.add(preparedIndexEntry(IndexAction.REMOVE, e.getKey(), null, e.getValue(), dic, converter));
		});

		return diff;
	}

	/**
	 * @param parallel
	 */
	void applyParallelMode(boolean parallel) {
		this.useParallelDiff = parallel;
	}

	/**
	 * @param source
	 * @return
	 */
	private Stream<Map.Entry<String, String>> switchedStream(Map<String, String> source) {

		if (this.useParallelDiff) {
			return source.entrySet().parallelStream();
		}

		return source.entrySet().stream();
	}

	/**
	 * <p>
	 * Atomic search for update / addition on one actual item
	 * </p>
	 * 
	 * @param actualOne
	 * @param knewContent
	 * @param diff
	 * @param debug
	 */
	private static void searchDiff(
			final Map.Entry<String, String> actualOne,
			final Map<String, String> knewContent,
			final Set<PreparedIndexEntry> diff,
			final DictionaryEntry dic,
			final ManagedValueConverter converter,
			final boolean debug) {

		// Found : for delete identification immediately remove from found ones
		String knewPayload = knewContent.remove(actualOne.getKey());

		// Exist already
		if (knewPayload != null) {

			// TODO : add independency over column model

			// Content is different : it's an Update
			if (!actualOne.getValue().equals(knewPayload)) {
				if (debug) {
					LOGGER.debug("New endex entry for {} : UPDATED from \"{}\" to \"{}\"", actualOne.getKey(), knewPayload,
							actualOne.getValue());
				}
				diff.add(preparedIndexEntry(IndexAction.UPDATE, actualOne.getKey(), actualOne.getValue(), knewPayload, dic, converter));
			}
		}

		// Doesn't exist already : it's an addition
		else {
			if (debug) {
				LOGGER.debug("New endex entry for {} : ADD with \"{}\" to \"{}\"", actualOne.getKey(), actualOne.getValue());
			}
			diff.add(preparedIndexEntry(IndexAction.ADD, actualOne.getKey(), actualOne.getValue(), null, dic, converter));
		}
	}

	/**
	 * @param action
	 * @param key
	 * @param currentPayload
	 * @param previousPayload
	 * @param dic
	 * @return
	 */
	private static PreparedIndexEntry preparedIndexEntry(
			IndexAction action,
			String key,
			String currentPayload,
			String previousPayload,
			DictionaryEntry dic,
			ManagedValueConverter converter) {

		PreparedIndexEntry entry = new PreparedIndexEntry();

		entry.setAction(action);
		entry.setKeyValue(key);

		// No need to save from / to payload : from is always already in index table !
		entry.setPayload(currentPayload);

		// But for human readability, need a custom display payload (not saved)
		entry.setHrPayload(converter.convertToHrPayload(currentPayload, previousPayload));

		// TODO : other source of timestamp ?
		entry.setTimestamp(System.currentTimeMillis());

		// Complete from dict
		entry.setDictionaryEntryName(dic.getParameterName());
		entry.setDictionaryEntryUuid(dic.getUuid());
		entry.setDomainName(dic.getDomain().getName());
		entry.setDomainUuid(dic.getDomain().getUuid());

		return entry;
	}
}