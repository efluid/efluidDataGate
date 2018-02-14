package fr.uem.efluid.services;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import fr.uem.efluid.model.DiffLine;
import fr.uem.efluid.model.entities.DictionaryEntry;
import fr.uem.efluid.model.entities.IndexAction;
import fr.uem.efluid.model.entities.IndexEntry;
import fr.uem.efluid.model.repositories.IndexRepository;
import fr.uem.efluid.model.repositories.ManagedExtractRepository;
import fr.uem.efluid.model.repositories.ManagedRegenerateRepository;
import fr.uem.efluid.services.types.PreparedIndexEntry;
import fr.uem.efluid.services.types.PreparedMergeIndexEntry;
import fr.uem.efluid.tools.ManagedValueConverter;

/**
 * <p>
 * Core service for diff processes and index preparation. Support 2 flows :
 * <ul>
 * <li>The standard diff process for local index creation, using a basic difference
 * analaysis between the actual managed DB, and a simulated "knew" managed DB generated
 * from actual index. This produce the exact "modification identification", ie, the index
 * content for a new commit</li>
 * <li>For merging process, the initial diff is using the same process, but the
 * "simulated" DB is constructed from Actual Index <b>until last commit import</b> and the
 * import index <b>starting the last commit import</b>. This produces a view of
 * differences between "actual DB" and "what we should have if we process all the import".
 * The produced diff is a "merge" result. This merge result is then completed, searching
 * for each corresponding index KeyValue, by a combined diff of all similar local index
 * ("what was really modified locally") and similar imported index ("what was really
 * modified in remote instance"), giving help to the user to select the diff entries to
 * apply or to cancel.</li>
 * </ul>
 * </p>
 * 
 * @author elecomte
 * @since v0.0.1
 * @version 1
 */
@Service
@Transactional
public class PrepareDiffService {

	private static final Logger LOGGER = LoggerFactory.getLogger(PrepareDiffService.class);

	@Autowired
	private ManagedExtractRepository rawParameters;

	@Autowired
	private ManagedRegenerateRepository regeneratedParamaters;

	@Autowired
	private ManagedValueConverter valueConverter;

	@Autowired
	private IndexRepository indexes;

	private boolean useParallelDiff = false;

	/**
	 * @param dictionaryEntryUuid
	 * @return
	 */
	public Collection<PreparedIndexEntry> currentContentDiff(DictionaryEntry entry) {

		LOGGER.debug("Processing new diff for all content for managed table \"{}\"", entry.getTableName());

		// Here one of the app complexity : diff check using JDBC, for one table. Backlog
		// construction + restoration then diff.

		Map<String, String> knewContent = this.regeneratedParamaters.regenerateKnewContent(entry);
		Map<String, String> actualContent = this.rawParameters.extractCurrentContent(entry);

		return generateDiffIndexFromContent(PreparedIndexEntry::new, knewContent, actualContent, entry);
	}

	/**
	 * <p>
	 * For merge diff, we run basically the standard process, but building the "knew
	 * content" as a combination of local knew content "until last imported commit" and
	 * the imported diff. => Produces the diff between current real content and "what we
	 * should have if the imported diff was present". This way we produce the exact result
	 * of the merge for an import.
	 * </p>
	 * 
	 * @param entry
	 * @param timeStampForSearch
	 * @param mergeContent
	 * @return
	 */
	public Collection<PreparedMergeIndexEntry> mergeIndexDiff(
			DictionaryEntry entry,
			long timeStampForSearch,
			List<PreparedMergeIndexEntry> mergeContent) {

		LOGGER.debug("Regenerating values from combined local + specified index for managed table \"{}\", using"
				+ " timestamp for local index search {}", entry.getTableName(), Long.valueOf(timeStampForSearch));

		// Here one other complexity in the app : diff between 2 different indexes, which
		// can be related themselves to a content diff previously generated. Used for
		// merging process

		List<IndexEntry> localIndexToTimeStamp = this.indexes.findByDictionaryEntryAndTimestampGreaterThanEqualOrderByTimestamp(entry,
				timeStampForSearch);

		// Prepare simulated "knew index"
		List<DiffLine> toProcess = new ArrayList<>();
		toProcess.addAll(localIndexToTimeStamp);
		toProcess.addAll(mergeContent);

		Map<String, String> knewContent = this.regeneratedParamaters.regenerateKnewContent(toProcess);
		Map<String, String> actualContent = this.rawParameters.extractCurrentContent(entry);

		Collection<PreparedMergeIndexEntry> diff = generateDiffIndexFromContent(PreparedMergeIndexEntry::new, knewContent, actualContent,
				entry);

		// Then apply from local and from import to identify diff changes
		completeMergeIndexes(entry, localIndexToTimeStamp, mergeContent, diff);

		return diff;
	}

	/**
	 * 
	 */
	public void resetDiffCaches() {
		this.regeneratedParamaters.refreshAll();
	}

	/**
	 * For testability
	 */
	protected ManagedValueConverter getConverter() {
		return this.valueConverter;
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
	 * @return
	 */
	<T extends PreparedIndexEntry> Collection<T> generateDiffIndexFromContent(
			final Supplier<T> diffTypeBuilder,
			final Map<String, String> knewContent,
			final Map<String, String> actualContent,
			DictionaryEntry dic) {

		final Set<T> diff = ConcurrentHashMap.newKeySet();

		// Use parallel process for higher distribution of verification
		switchedStream(actualContent).forEach(actualOne -> searchDiff(diffTypeBuilder, actualOne, knewContent, diff, dic));

		// Remaining in knewContent are deleted ones
		switchedStream(knewContent).forEach(e -> {
			LOGGER.debug("New endex entry for {} : REMOVE from \"{}\"", e.getKey(), e.getValue());
			diff.add(preparedIndexEntry(diffTypeBuilder, IndexAction.REMOVE, e.getKey(), null, e.getValue(), dic));
		});

		return diff;
	}

	/**
	 * @param parallel
	 */
	protected void applyParallelMode(boolean parallel) {
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
	private <T extends PreparedIndexEntry> void searchDiff(
			final Supplier<T> diffTypeBuilder,
			final Map.Entry<String, String> actualOne,
			final Map<String, String> knewContent,
			final Set<T> diff,
			final DictionaryEntry dic) {

		// Found : for delete identification immediately remove from found ones
		String knewPayload = knewContent.remove(actualOne.getKey());

		// Exist already
		if (knewPayload != null) {

			// TODO : add independency over column model

			// Content is different : it's an Update
			if (!actualOne.getValue().equals(knewPayload)) {
				LOGGER.debug("New endex entry for {} : UPDATED from \"{}\" to \"{}\"", actualOne.getKey(), knewPayload,
						actualOne.getValue());
				diff.add(preparedIndexEntry(diffTypeBuilder, IndexAction.UPDATE, actualOne.getKey(), actualOne.getValue(), knewPayload,
						dic));
			}
		}

		// Doesn't exist already : it's an addition
		else {
			LOGGER.debug("New endex entry for {} : ADD with \"{}\" to \"{}\"", actualOne.getKey(), actualOne.getValue());
			diff.add(preparedIndexEntry(diffTypeBuilder, IndexAction.ADD, actualOne.getKey(), actualOne.getValue(), null, dic));
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
	private <T extends PreparedIndexEntry> T preparedIndexEntry(
			final Supplier<T> diffTypeBuilder,
			IndexAction action,
			String key,
			String currentPayload,
			String previousPayload,
			DictionaryEntry dic) {

		T entry = diffTypeBuilder.get();

		entry.setAction(action);
		entry.setKeyValue(key);

		// No need to save from / to payload : from is always already in index table !
		entry.setPayload(currentPayload);

		// But for human readability, need a custom display payload (not saved)
		entry.setHrPayload(getConverter().convertToHrPayload(currentPayload, previousPayload));

		// TODO : other source of timestamp ?
		entry.setTimestamp(System.currentTimeMillis());

		// Complete from dict
		entry.setDictionaryEntryUuid(dic.getUuid());

		return entry;
	}

	/**
	 * @param dict
	 * @param indexStartTime
	 * @param preparingMergeIndexToComplete
	 * @param mergeSource
	 */
	private void completeMergeIndexes(
			DictionaryEntry dict,
			List<? extends DiffLine> local,
			List<? extends DiffLine> mergeSource,
			Collection<PreparedMergeIndexEntry> preparingMergeIndexToComplete) {

		LOGGER.debug("Completing merge data from index for parameter table {}", dict.getTableName());

		Map<String, List<DiffLine>> minesByKey = local.stream().collect(Collectors.groupingBy(DiffLine::getKeyValue));
		Map<String, List<DiffLine>> theirsByKey = mergeSource.stream().collect(Collectors.groupingBy(DiffLine::getKeyValue));

		// Prepare "previous" if any for HR Payload
		Map<String, IndexEntry> previouses = this.indexes.findAllPreviousIndexEntries(dict,
				preparingMergeIndexToComplete.stream().map(DiffLine::getKeyValue).collect(Collectors.toList()));

		// For each merge result (Only one possible for each keyValue)
		preparingMergeIndexToComplete.stream().forEach(m -> {

			// Prepared combined diffs
			DiffLine mine = DiffLine.combinedOnSameTableAndKey(minesByKey.get(m.getKeyValue()));
			DiffLine their = DiffLine.combinedOnSameTableAndKey(theirsByKey.get(m.getKeyValue()));

			// Will apply HR Payload (previous is the same for current, "mine" & "their")
			IndexEntry previous = previouses.get(m.getKeyValue());

			// HrPayload values (3 required for each entry)
			String currentHrPayload = getConverter().convertToHrPayload(m.getPayload(), previous != null ? previous.getPayload() : null);
			String mineHrPayload = getConverter().convertToHrPayload(mine != null ? mine.getPayload() : null,
					previous != null ? previous.getPayload() : null);
			String theirHrPayload = getConverter().convertToHrPayload(their != null ? their.getPayload() : null,
					previous != null ? previous.getPayload() : null);

			// Complete current entry HR payload for rendering
			m.setHrPayload(currentHrPayload);

			// Complete corresponding "mine" and "their"
			m.setMine(PreparedIndexEntry.fromCombined(mine, mineHrPayload));
			m.setTheir(PreparedIndexEntry.fromCombined(their, theirHrPayload));

			LOGGER.debug("Completion on merge data for table {}, key \"{}\", \"HrPayload\"=\"{}\", \"mine\"={}, \"their\"={}",
					dict.getTableName(), m.getKeyValue(), m.getHrPayload(), m.getMine(), m.getTheir());
		});

		// For whatever is NOT found in preparingMergeIndex, IGNORE !!!
	}

	// /**
	// * <p>
	// * When differences exist in both local and merge index, need to identify conflict,
	// * and try to resolve it if it's possible
	// * </p>
	// *
	// * @param dict
	// * @param keyValue
	// * @param merge
	// * @param local
	// * nullable
	// */
	// private static void completeConflictingMergeForOneKey(DictionaryEntry dict, String
	// keyValue, List<PreparedMergeIndexEntry> merge,
	// List<IndexEntry> local) {
	//
	// List<PreparedIndexEntry> mergeEntries =
	// merge.stream().map(PreparedMergeIndexEntry::getTheir).collect(Collectors.toList());
	//
	// // Merge local
	// DiffLine combinedLocal = DiffLine.combinedOnSameTableAndKey(local);
	//
	// PreparedMergeIndexEntry last = merge.get(merge.size() - 1);
	//
	// // Just one remote change : compare with combinedLocal
	// if (merge.size() == 1) {
	//
	// // Nothing in local index : get "their" directly
	// if (combinedLocal == null) {
	// last.setResult(last.getTheir());
	// last.setMergeAction(KEEP);
	// } else {
	//
	// // Same change local and remote : no conflict, just keep
	// if (combinedLocal.equals(last.getTheir())) {
	// last.setResult(last.getTheir());
	// last.setMergeAction(KEEP);
	// }
	//
	// // Not the same changes : it's a conflict (use local as default)
	// else {
	// last.setResult(PreparedIndexEntry.fromCombined(combinedLocal, dict, keyValue,
	// last.getTheir().getTimestamp()));
	// last.setMergeAction(RESOLVE_CONFLICT);
	// }
	// }
	// }
	//
	// // Else compare with combined merge
	// else {
	//
	// DiffLine combinedMerge = DiffLine.combinedOnSameTableAndKey(mergeEntries);
	//
	// // Change last item with combined
	// last.getTheir().setAction(combinedMerge.getAction());
	// last.getTheir().setPayload(combinedMerge.getPayload());
	//
	// /*
	// * Note on merge Action : as default value in PreparedMergeIndexEntry is
	// * "DROP", for X changes in "their", they will all be droped as default, and
	// * only the last one is kept as valid "their". Every droped entries are not
	// * even displayed to user in merge screen, so he will only see the
	// * "real final changes"*.
	// */
	//
	// // Nothing in local index : get "their" directly
	// if (combinedLocal == null) {
	// last.setResult(PreparedIndexEntry.fromCombined(combinedMerge, dict, keyValue,
	// last.getTheir().getTimestamp()));
	// last.setMergeAction(KEEP);
	// }
	//
	// // Combined diff on both local and merge "their"
	// else {
	//
	// // Same change local and remote : no conflict, just keep
	// if (combinedLocal.equals(combinedMerge)) {
	// last.setResult(PreparedIndexEntry.fromCombined(combinedMerge, dict, keyValue,
	// last.getTheir().getTimestamp()));
	// last.setMergeAction(KEEP);
	// }
	//
	// // Not the same changes : it's a conflict (use local as default)
	// else {
	// last.setResult(PreparedIndexEntry.fromCombined(combinedLocal, dict, keyValue,
	// last.getTheir().getTimestamp()));
	// last.setMergeAction(RESOLVE_CONFLICT);
	// }
	// }
	// }
	// }
}