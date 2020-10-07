package fr.uem.efluid.services;

import fr.uem.efluid.model.AnomalyContextType;
import fr.uem.efluid.model.ContentLine;
import fr.uem.efluid.model.DiffLine;
import fr.uem.efluid.model.entities.*;
import fr.uem.efluid.model.repositories.*;
import fr.uem.efluid.model.repositories.ManagedExtractRepository.Extraction;
import fr.uem.efluid.services.types.*;
import fr.uem.efluid.tools.ManagedValueConverter;
import fr.uem.efluid.tools.MergeResolutionProcessor;
import fr.uem.efluid.utils.ApplicationException;
import fr.uem.efluid.utils.DatasourceUtils;
import fr.uem.efluid.utils.ErrorType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static fr.uem.efluid.services.types.DiffRemark.RemarkType.MISSING_ON_UNCHECKED_JOIN;

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
 * @version 2
 * @since v0.0.1
 */
@Service
@Transactional
public class PrepareIndexService extends AbstractApplicationService {

    private static final Logger LOGGER = LoggerFactory.getLogger(PrepareIndexService.class);

    private static final Logger MERGE_LOGGER = LoggerFactory.getLogger("merge.analysis");

    @Autowired
    private ManagedExtractRepository rawParameters;

    @Autowired
    private ManagedRegenerateRepository regeneratedParamaters;

    @Autowired
    private ManagedValueConverter valueConverter;

    @Autowired
    private MergeResolutionProcessor mergeResolutionProcessor;

    @Autowired
    private AnomalyAndWarningService anomalyService;

    @Value("${datagate-efluid.display.combine-similar-diff-after}")
    private long maxSimilarBeforeCombined;

    @Value("${datagate-efluid.display.test-row-max-size}")
    private long maxForTestExtract;

    @Autowired
    private IndexRepository indexes;

    @Autowired
    private TableLinkRepository links;


    /**
     * <p>
     * Prepare the diff content, by extracting current local content and building value to value
     * diff regarding the actual index content
     * </p>
     * <p>
     * Apply also some "remarks" to diff if required
     * </p>
     * <p>5 steps</p>
     *
     * @param preparation to complete
     * @param entry       dictionaryEntry
     * @param lobs
     * @param project
     */
    @Transactional(
            isolation = Isolation.READ_UNCOMMITTED,
            transactionManager = DatasourceUtils.MANAGED_TRANSACTION_MANAGER,
            propagation = Propagation.NOT_SUPPORTED
    )
    public void completeLocalDiff(
            PilotedCommitPreparation<PreparedIndexEntry> preparation,
            DictionaryEntry entry,
            Map<String, byte[]> lobs,
            Project project) {

        LOGGER.debug("Processing new diff for all content for managed table \"{}\"", entry.getTableName());

        // Here one of the app complexity : diff check using JDBC, for one table. Backlog
        // construction + restoration then diff.

        LOGGER.info("Start regenerate knew content for table \"{}\"", entry.getTableName());
        Map<String, String> knewContent = this.regeneratedParamaters.regenerateKnewContent(entry);

        // Intermediate step for better percent process
        preparation.incrementProcessStep();

        LOGGER.info("Regenerate done, start extract actual content for table \"{}\"", entry.getTableName());
        AtomicLong totalProcessed = new AtomicLong(0);

        try (Extraction extraction = this.rawParameters.extractCurrentContent(entry, lobs, project)) {

            // Process actual content in stream
            Collection<PreparedIndexEntry> index = generateDiffIndexFromContent(
                    extraction.stream(),
                    PreparedIndexEntry::new,
                    knewContent,
                    c -> totalProcessed.incrementAndGet(),
                    entry,
                    preparation);

            // Some diffs may add remarks
            LOGGER.debug("Check if some remarks can be added to diff for table \"{}\"", entry.getTableName());
            processOptionalCurrentContendDiffRemarks(preparation, entry, project, totalProcessed.get());

            // Intermediate step for better percent process
            preparation.incrementProcessStep();

            // Detect and process similar entries for display, unsorted
            List<PreparedIndexEntry> preparedDiff = combineSimilarDiffEntries(index, SimilarPreparedIndexEntry::fromSimilar);

            // Keep content and dict only if some results are found at the end
            if (!preparedDiff.isEmpty()) {
                preparation.getDiffContent().addAll(preparedDiff);
                preparation.getReferencedTables().put(entry.getUuid(), DictionaryEntrySummary.fromEntity(entry, "?"));
            }

            // Intermediate step for better percent process
            preparation.incrementProcessStep();
        }
    }

    /**
     * <p>
     * Prepare the diff content, by extracting current content and building value to value
     * diff regarding the actual index content
     * </p>
     * <p>
     * For merge diff, we run basically the standard process, but building the "knew
     * content" as a combination of local knew content "until last imported commit" and
     * the imported diff. =&gt; Produces the diff between current real content and "what we
     * should have if the imported diff was present". This way we produce the exact result
     * of the merge for an import.
     * </p>
     * <p>Merge diff is processed by transformer (with TransformerProcessor available in current preparation)</p>
     * <p>
     * Apply also some "remarks" to diff if required
     * </p>
     * <p>8 steps</p>
     *
     * @param preparation        current preparation
     * @param entry              dictionaryEntry
     * @param lobs               for any extracted lobs
     * @param mergeDiff          imported merge diff
     * @param project
     */
    @Transactional(
            isolation = Isolation.READ_UNCOMMITTED,
            transactionManager = DatasourceUtils.MANAGED_TRANSACTION_MANAGER,
            propagation = Propagation.NOT_SUPPORTED
    )
    public void completeMergeDiff(
            PilotedCommitPreparation<PreparedMergeIndexEntry> preparation,
            DictionaryEntry entry,
            Map<String, byte[]> lobs,
            List<PreparedMergeIndexEntry> mergeDiff,
            Project project) {

       long localIndexToTimeStamp = this.indexes.findMaxIndexTimestampOfLastImportedCommit();

       if

        // Index "previous"
        List<IndexEntry> localIndexToTimeStamp = this.indexes.findByDictionaryEntryAndTimestampLessThanEqual(entry,
                timeStampForSearch);

        preparation.incrementProcessStep();

        // Build "previous" content from index
        Map<String, String> previousContent = this.regeneratedParamaters.regenerateKnewContent(localIndexToTimeStamp);

        preparation.incrementProcessStep();

        // Get actual content
        LOGGER.info("Regenerate done, start extract actual content for table \"{}\"", entry.getTableName());
        Set<String> allActualKeys = ConcurrentHashMap.newKeySet();

        try (Extraction extraction = this.rawParameters.extractCurrentContent(entry, lobs, project)) {

            // Process actual content in stream
            Collection<PreparedIndexEntry> mineDiff = generateDiffIndexFromContent(
                    extraction.stream(),
                    PreparedIndexEntry::new,
                    previousContent,
                    c -> allActualKeys.add(c.getKeyValue()),
                    entry,
                    preparation);

            // Apply transformer on merge content
            List<? extends PreparedIndexEntry> transformedMergeDiff = preparation.getTransformerProcessor() != null
                    ? preparation.getTransformerProcessor().transform(entry, mergeDiff) : mergeDiff;

            preparation.incrementProcessStep();

            // Build merge diff entries from 2 source of Diff + previous content
            Collection<PreparedMergeIndexEntry> completedMergeDiff =
                    completeMergeIndexes(entry, allActualKeys, mineDiff, transformedMergeDiff, previousContent, preparation);

            preparation.incrementProcessStep();

            // Combine similar, unsorted
            List<PreparedMergeIndexEntry> preparedDiff = combineSimilarDiffEntries(completedMergeDiff, SimilarPreparedMergeIndexEntry::fromSimilar);

            // Keep content and dict only if some results are found at the end
            if (!preparedDiff.isEmpty()) {
                preparation.getDiffContent().addAll(preparedDiff);
                preparation.getReferencedTables().put(entry.getUuid(), DictionaryEntrySummary.fromEntity(entry, "?"));
            }

            preparation.incrementProcessStep();
        }
    }

    /**
     *
     */
    public void resetDiffCaches() {
        this.regeneratedParamaters.refreshAll();
    }

    /**
     * <p>
     * For content extraction in test context (will editing a dictionary entry for
     * example). Can process a stale dictionaryEntry, and will ignore links and blobs
     * </p>
     *
     * @param entry
     * @return
     */
    TestQueryData testActualContent(DictionaryEntry entry) {

        List<List<String>> table = new ArrayList<>();

        long count = this.rawParameters.testCurrentContent(entry, table, this.maxForTestExtract);

        return new TestQueryData(table, count);
    }

    /**
     * <p>
     * Utilitary fonction to complete given index entries with their respective HR Payload
     * for user friendly rendering. Provides the completed rendering list in return, as
     * some display process may require to combine contents when they are similar. In this
     * case they are provided as SimilarPreparedIndexEntry
     * </p>
     *
     * @param dictionaryEntryUuid uuid of dict entry
     * @param index               loaded index content
     * @param combineSimilars     true if the similar entries must be combined in single lines
     * @return List adapted for rendering : some results may be combined
     */
    List<PreparedIndexEntry> prepareDiffForRendering(UUID dictionaryEntryUuid, List<PreparedIndexEntry> index, boolean combineSimilars) {

        // Complete HR payloads
        index.forEach(e -> {
            if (e.getKeyValue().equals("EEE")) {
                System.out.println("gotcha");
            }
            e.setHrPayload(getConverter().convertToHrPayload(e.getPayload(), e.getPrevious()));
        });

        // And then combine for rendering (if asked)
        return combineSimilars ? combineSimilarDiffEntries(index, SimilarPreparedIndexEntry::fromSimilar) : index;
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
     * <p>Add 3 steps</p>
     *
     * @param actualContent
     * @param diffTypeBuilder
     * @param knewContent
     * @param eachLineAccumulator processed on all lines from actual
     * @param entry
     * @param preparation         for step update
     * @return
     */
    <T extends PreparedIndexEntry> Collection<T> generateDiffIndexFromContent(
            final Stream<ContentLine> actualContent,
            final Supplier<T> diffTypeBuilder,
            final Map<String, String> knewContent,
            final Consumer<ContentLine> eachLineAccumulator,
            final DictionaryEntry entry,
            final PilotedCommitPreparation<?> preparation) {

        LOGGER.info("Start diff index Generate for table \"{}\"", entry.getTableName());

        preparation.incrementProcessStep();

        Collection<T> diff = actualContent
                .peek(eachLineAccumulator)
                .map(l -> toDiff(diffTypeBuilder, l, knewContent, entry))
                .filter(Objects::nonNull)
                .collect(Collectors.toCollection(ConcurrentHashMap::newKeySet));

        // Intermediate step for better percent process
        preparation.incrementProcessStep();

        // Remaining in knewContent are deleted ones
        knewContent.forEach((k, v) -> {
            LOGGER.debug("New index entry for {} : REMOVE from \"{}\"", k, v);
            diff.add(preparedIndexEntry(diffTypeBuilder, IndexAction.REMOVE, k, null, v, entry));
        });

        preparation.incrementProcessStep();

        return diff;
    }

    /**
     * <p>
     * Search on each diff for used lob hashes, and prepare the corresponding LobProperty
     * to save
     * </p>
     *
     * @param diffs
     * @param lobs
     * @return
     */
    List<LobProperty> prepareUsedLobsForIndex(List<? extends DiffLine> diffs, Map<String, byte[]> lobs) {

        return diffs.stream()
                .map(d -> this.valueConverter.extractUsedBinaryHashs(d.getPayload()))
                .filter(v -> v != null && !v.isEmpty())
                .flatMap(List::stream).map(h -> {
                    LobProperty lob = new LobProperty();
                    lob.setHash(h);
                    lob.setData(lobs.get(h));
                    return lob;
                }).collect(Collectors.toList());

    }

    /**
     * Process the given index entries to get ALL used column names in payloads. Can be slow for large index as it will expand the payload string contents
     *
     * @param entries index
     * @return name of columns referenced in payload
     */
    Set<String> extractIndexEntryValueNames(Collection<IndexEntry> entries) {
        return entries.stream().map(IndexEntry::getPayload)
                .flatMap(p -> this.valueConverter.expandInternalValueNames(p))
                .collect(Collectors.toSet());
    }

    /**
     * @param diffWithSimilars
     * @return
     */
    @SuppressWarnings("unchecked")
    <T extends PreparedIndexEntry> List<T> splitCombinedSimilar(List<T> diffWithSimilars) {

        // Check if their is similar items
        if (diffWithSimilars.stream().anyMatch(Rendered::isDisplayOnly)) {

            return diffWithSimilars.stream().flatMap(d -> {

                // Combined to split + stream
                if (d.isDisplayOnly()) {
                    return ((CombinedSimilar<T>) d).split().stream();
                }

                // Standard to stream
                return Stream.of(d);
            }).collect(Collectors.toList());
        }

        // If no similar : return provided list
        return diffWithSimilars;
    }

    /**
     * For testability
     */
    protected ManagedValueConverter getConverter() {
        return this.valueConverter;
    }

    /**
     * <p>
     * Process detection and completion of some business rules on diff process. Will
     * create remarks if required.
     * </p>
     * <p>
     * <b>The currently supported checks are</b> :
     * <ul>
     * <li>Check if some content are missing because of unchecked link references, and add
     * details on missing data in the remark</li>
     * </ul>
     * </p>
     *
     * @param preparation       to complete
     * @param entry
     * @param project
     * @param actualContentSize
     */
    private void processOptionalCurrentContendDiffRemarks(
            PilotedCommitPreparation<?> preparation,
            DictionaryEntry entry,
            Project project,
            long actualContentSize) {

        // If parameter table has links, check also the count with unchecked joins
        if (this.links.countLinksForDictionaryEntry(entry) > 0) {
            LOGGER.debug("Start checking count of entries with unchecked joins for table \"{}\"", entry.getTableName());

            // If difference identified in content size with unchecked join, add a remark
            if (this.rawParameters.countCurrentContentWithUncheckedJoins(entry, project) > actualContentSize) {

                try (Extraction missingContents = this.rawParameters.extractCurrentMissingContentWithUncheckedJoins(entry, project)) {
                    // Get the missign payloads as display list
                    List<ContentLineDisplay> missingContent = missingContents.stream()
                            .map(e -> new ContentLineDisplay(e.getKeyValue(), e.getPayload(), entry.getKeyName()))
                            .collect(Collectors.toList());

                    if (missingContent.size() > 0) {
                        // Prepare the corresponding remark
                        DiffRemark<List<ContentLineDisplay>> remark = new DiffRemark<>(
                                MISSING_ON_UNCHECKED_JOIN, "table " + entry.getTableName(), missingContent);

                        preparation.getDiffRemarks().add(remark);

                        LOGGER.info("Found a count of {} missing entries with unchecked joins for table \"{}\"",
                                missingContent.size(), entry.getTableName());
                    }
                }
            }
        }
    }

    /**
     * <p>
     * Atomic search for update / addition on one actual item
     * </p>
     *
     * @param diffTypeBuilder
     * @param actualOne
     * @param knewContent
     * @param dic
     * @param <T>
     * @return null if not to keep in diff
     */
    private <T extends PreparedIndexEntry> T toDiff(
            final Supplier<T> diffTypeBuilder,
            final ContentLine actualOne,
            final Map<String, String> knewContent,
            final DictionaryEntry dic) {

        boolean wasKnew = knewContent.containsKey(actualOne.getKeyValue());

        // Found : for delete identification immediately remove from found ones
        String knewPayload = knewContent.remove(actualOne.getKeyValue());

        // Exist already
        if (!StringUtils.isEmpty(knewPayload)) {

            // Content is different : it's an Update
            if (!actualOne.getPayload().equals(knewPayload)) {
                LOGGER.debug("New endex entry for {} : UPDATED from \"{}\" to \"{}\"", actualOne.getKeyValue(), knewPayload,
                        actualOne.getPayload());
                return preparedIndexEntry(diffTypeBuilder, IndexAction.UPDATE, actualOne.getKeyValue(), actualOne.getPayload(), knewPayload,
                        dic);
            }
        }

        // Doesn't exist already : it's an addition
        else {

            // Except if new is also empty will content is knew : it's a managed empty line
            if (!(wasKnew && StringUtils.isEmpty(actualOne.getPayload()))) {
                LOGGER.debug("New endex entry for {} : ADD with \"{}\"", actualOne.getKeyValue(), actualOne.getPayload());
                return preparedIndexEntry(diffTypeBuilder, IndexAction.ADD, actualOne.getKeyValue(), actualOne.getPayload(), null, dic);
            }
        }

        // Nullify lines to ignore (already in knew content)
        return null;
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
        entry.setPrevious(previousPayload);

        // But for human readability, need a custom display payload (not saved)
        entry.setHrPayload(getConverter().convertToHrPayload(currentPayload, previousPayload));

        entry.setTimestamp(System.currentTimeMillis());

        // Complete from dict
        entry.setDictionaryEntryUuid(dic.getUuid());

        // Specify a temp id for diff management
        entry.setIndexForDiff(dic.getUuid() + "_" + key);

        return entry;
    }


    /**
     * @param dict
     * @param allActualKeys
     * @param mines
     * @param theirs
     * @param previousContent
     * @param preparation
     * @return
     */
    private Collection<PreparedMergeIndexEntry> completeMergeIndexes(
            DictionaryEntry dict,
            Set<String> allActualKeys,
            Collection<? extends DiffLine> mines,
            Collection<? extends DiffLine> theirs,
            Map<String, String> previousContent,
            PilotedCommitPreparation<?> preparation) {

        LOGGER.debug("Completing merge data from index for parameter table {}", dict.getTableName());

        Map<String, List<DiffLine>> minesByKey = mines.stream().collect(Collectors.groupingBy(DiffLine::getKeyValue));
        Map<String, List<DiffLine>> theirsByKey = theirs.stream().collect(Collectors.groupingBy(DiffLine::getKeyValue));

        Set<String> allKeys = new HashSet<>();
        allKeys.addAll(minesByKey.keySet());
        allKeys.addAll(theirsByKey.keySet());

        boolean recordWarnings = this.features.isEnabled(Feature.RECORD_IMPORT_WARNINGS);
        AtomicInteger count = new AtomicInteger(0);
        int fireIncrem = allKeys.size() / 2;

        // One combined key for each keys
        return allKeys.stream().map(key -> {

            try {
                String previous = previousContent.get(key);
                DiffLine mine = DiffLine.combinedOnSameTableAndKey(minesByKey.get(key), false);
                DiffLine their = DiffLine.combinedOnSameTableAndKey(theirsByKey.get(key), true);

                // Ignore dead entries (ADDED then DELETED in regenerated content)
                if (mine == null && their == null) {
                    return null;
                }

                String mineHr = getConverter().convertToHrPayload(mine != null ? mine.getPayload() : null, previous);
                String theirHr = getConverter().convertToHrPayload(their != null ? their.getPayload() : null, previous);

                PreparedIndexEntry mineEntry = mine != null ? PreparedIndexEntry.fromCombined(mine, dict.getTableName(), mineHr) : null;
                PreparedIndexEntry theirEntry = their != null ? PreparedIndexEntry.fromCombined(their, dict.getTableName(), theirHr) : null;

                // One increment only (when half are processed)
                if (count.incrementAndGet() == fireIncrem) {
                    preparation.incrementProcessStep();
                }

                PreparedMergeIndexEntry resolved = this.mergeResolutionProcessor.resolveMerge(mineEntry, theirEntry, allActualKeys.contains(key));

                // Dedicated logger output for resolutions
                if (MERGE_LOGGER.isDebugEnabled()) {
                    if (resolved != null) {
                        MERGE_LOGGER.debug("Resolved : Table \"{}\" - Key\"{}\" - mine = \"{}\", their = \"{}\", -> Get \"{}\" with rule \"{}\"",
                                dict.getTableName(), key, mineEntry, theirEntry, resolved, resolved.getResolutionRule());
                    } else {
                        MERGE_LOGGER.debug("Resolved : Table \"{}\" - Key\"{}\" - mine = \"{}\", their = \"{}\", -> droped",
                                dict.getTableName(), key, mineEntry, theirEntry);
                    }
                }

                if (recordWarnings && resolved != null && resolved.getResolutionWarning() != null) {
                    this.anomalyService.addAnomaly(AnomalyContextType.MERGE, preparation.getSourceFilename(),
                            "Warning from " + resolved.getResolutionRule(),
                            "On " + resolved.toLogRendering() + " : " + resolved.getResolutionWarning());
                }
                return resolved;
            } catch (Throwable t) {
                LOGGER.error("Failed to process merge index entry on " + dict.getTableName() + "." + key + ", get error", t);
                throw new ApplicationException(ErrorType.MERGE_FAILURE, t);
            }
        }).filter(Objects::nonNull).collect(Collectors.toList());
    }

    /**
     * <p>
     * Combines the diff which are similar as <tt>SimilarPreparedIndexEntry</tt> from
     * already prepared list of <tt>PreparedIndexEntry</tt>. The resulting list contains
     * the modified rendering
     * </p>
     *
     * @param readyToRender List of <tt>PreparedIndexEntry</tt> for similar content search
     * @return list to finally render
     */
    private <T extends PreparedIndexEntry> List<T> combineSimilarDiffEntries(
            Collection<T> readyToRender,
            Function<Collection<T>, ? extends T> similarConvert) {

        List<T> listToRender = new ArrayList<>();

        // Combine by HR payload
        Map<String, List<T>> combineds = readyToRender.stream()
                .collect(Collectors.groupingBy(p -> p.getHrPayload() != null ? p.getHrPayload() : ""));

        // Rendering display is based on combined
        combineds.values().stream().forEach(e -> {

            // Only one : not combined
            if (e.size() < this.maxSimilarBeforeCombined) {
                listToRender.addAll(e);
            }

            // Else a combined rendering
            else {
                listToRender.add(similarConvert.apply(e));
            }
        });

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Full combined result : ");
            listToRender.forEach(i -> {
                if (i.isDisplayOnly()) {
                    LOGGER.debug("[COMBINED] {} for {} : {}", i.getKeyValue(),
                            String.join(",", ((SimilarPreparedIndexEntry) i).getKeyValues()), i.getHrPayload());
                } else {
                    LOGGER.debug("[DIFF-ENTRY] {} : {}", i.getKeyValue(), i.getHrPayload());
                }
            });
        }

        return listToRender;
    }
}