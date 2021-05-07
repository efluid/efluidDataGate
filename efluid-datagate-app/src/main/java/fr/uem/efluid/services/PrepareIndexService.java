package fr.uem.efluid.services;

import fr.uem.efluid.model.AnomalyContextType;
import fr.uem.efluid.model.ContentLine;
import fr.uem.efluid.model.DiffLine;
import fr.uem.efluid.model.DiffPayloads;
import fr.uem.efluid.model.entities.*;
import fr.uem.efluid.model.repositories.IndexRepository;
import fr.uem.efluid.model.repositories.KnewContentRepository;
import fr.uem.efluid.model.repositories.ManagedExtractRepository;
import fr.uem.efluid.model.repositories.ManagedExtractRepository.Extraction;
import fr.uem.efluid.model.repositories.TableLinkRepository;
import fr.uem.efluid.services.types.*;
import fr.uem.efluid.tools.ManagedValueConverter;
import fr.uem.efluid.tools.MergeResolutionProcessor;
import fr.uem.efluid.tools.RollbackConverter;
import fr.uem.efluid.utils.ApplicationException;
import fr.uem.efluid.utils.DatasourceUtils;
import fr.uem.efluid.utils.ErrorType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.*;
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
 * @version 3
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
    private KnewContentRepository knewContents;

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

    @Autowired
    private RollbackConverter rollbackConverter;

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
/*    @Transactional(
            transactionManager = DatasourceUtils.MANAGED_TRANSACTION_MANAGER
    )*/
    public void completeLocalDiff(
            PilotedCommitPreparation<PreparedIndexEntry> preparation,
            DictionaryEntry entry,
            Map<String, byte[]> lobs,
            Project project) {

        LOGGER.debug("Processing new diff for all content for managed table \"{}\"", entry.getTableName());

        // Here one of the app complexity : diff check using JDBC, for one table. Backlog
        // construction + restoration then diff.

        // We search index just before now
        long pivotTimestamp = System.currentTimeMillis();

        LOGGER.info("Start regenerate knew content for table \"{}\"", entry.getTableName());
        Set<String> knewKeys = getKnewContents().knewContentKeys(entry);

        // Intermediate step for better percent process
        preparation.incrementProcessStep();

        LOGGER.info("Regenerate done, start extract actual content for table \"{}\"", entry.getTableName());
        AtomicLong totalProcessed = new AtomicLong(0);

        try (Extraction extraction = this.rawParameters.extractCurrentContent(entry, lobs, project)) {

            // Process actual content in stream
            Collection<PreparedIndexEntry> index = generateDiffIndexFromContent(
                    extraction.stream(),
                    PreparedIndexEntry::new,
                    knewKeys,
                    c -> totalProcessed.incrementAndGet(),
                    entry,
                    preparation,
                    pivotTimestamp);

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
     * Prepare data for a revert preparation on one table
     *
     * @param preparation current preparation
     * @param entry       processing table
     */
    public void completeRevertDiff(
            PilotedCommitPreparation<PreparedRevertIndexEntry> preparation,
            DictionaryEntry entry) {

        LOGGER.debug("Processing new diff for all content for managed table \"{}\"", entry.getTableName());

        this.indexes.findByCommitUuidAndDictionaryEntry(preparation.getCommitData().getRevertSourceCommitUuid(), entry)
                .map(e -> PreparedRevertIndexEntry.fromEntityToRevert(e, this.rollbackConverter))
                .peek(e -> e.setHrPayload(getConverter().convertToHrPayload(e.getPayload(), e.getPrevious())))
                .forEach(preparation.getDiffContent()::add);

        preparation.getReferencedTables().put(entry.getUuid(), DictionaryEntrySummary.fromEntity(entry, "?"));

        // Intermediate step for better percent process
        preparation.incrementProcessStep();
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
     * @param preparation current preparation
     * @param entry       dictionaryEntry
     * @param lobs        for any extracted lobs
     * @param mergeDiff   imported merge diff
     * @param project     current corresponding project
     */
    public void completeMergeDiff(
            PilotedCommitPreparation<PreparedMergeIndexEntry> preparation,
            DictionaryEntry entry,
            Map<String, byte[]> lobs,
            List<PreparedMergeIndexEntry> mergeDiff,
            Project project) {

        Long searchTimeStamp = this.indexes.findMaxIndexTimestampOfLastImportedCommit();

        // On 1st merge, get everything until now
        if (searchTimeStamp == null) {
            searchTimeStamp = System.currentTimeMillis();
        }

        preparation.incrementProcessStep();

        // Get knew keys before pivot time for merge
        Set<String> knewKeys = getKnewContents().knewContentKeysBefore(entry, searchTimeStamp);

        // Will be populated from extraction
        final Map<String, String> actualContent = new HashMap<>();

        preparation.incrementProcessStep();

        // Get actual content
        LOGGER.info("Regenerate done, start extract actual content for table \"{}\"", entry.getTableName());

        // Identify the last knew previous for lines without changes (used for merged item build)
        try (Extraction extraction = this.rawParameters.extractCurrentContent(entry, lobs, project)) {

            // Process actual content in stream
            Collection<PreparedIndexEntry> mineDiff =
                    generateDiffIndexFromContent(
                            extraction.stream(),
                            PreparedIndexEntry::new,
                            knewKeys,
                            l -> actualContent.put(l.getKeyValue(), l.getPayload()),
                            entry,
                            preparation,
                            searchTimeStamp);

            // Apply transformer on merge content
            List<? extends PreparedIndexEntry> transformedMergeDiff = preparation.getTransformerProcessor() != null
                    ? preparation.getTransformerProcessor().transform(entry, mergeDiff) : mergeDiff;

            preparation.incrementProcessStep();

            // Build merge diff entries from 2 source of Diff + previous content
            Collection<PreparedMergeIndexEntry> completedMergeDiff =
                    completeMergeIndexes(
                            entry,
                            actualContent,
                            mineDiff,
                            transformedMergeDiff,
                            preparation,
                            searchTimeStamp);

            preparation.incrementProcessStep();

            // Combine similar, unsorted
            List<PreparedMergeIndexEntry> preparedDiff =
                    combineSimilarDiffEntries(completedMergeDiff, SimilarPreparedMergeIndexEntry::fromSimilar);

            // Keep content and dict only if some results are found at the end
            if (!preparedDiff.isEmpty()) {
                preparation.getDiffContent().addAll(preparedDiff);
                preparation.getReferencedTables().put(entry.getUuid(), DictionaryEntrySummary.fromEntity(entry, "?"));
            }

            preparation.incrementProcessStep();
        } catch (Throwable e) {
            LOGGER.warn("Unprocessed error on merge diff on table " + entry.getTableName() + " : " + e.getMessage(), e);
            throw new ApplicationException(ErrorType.MERGE_FAILURE, "Unprocessed error on merge diff", e);
        }
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
     * Utils function to complete given index entries with their respective HR Payload
     * for user friendly rendering. Provides the completed rendering list in return, as
     * some display process may require to combine contents when they are similar. In this
     * case they are provided as SimilarPreparedIndexEntry
     * </p>
     *
     * @param index           loaded index content
     * @param combineSimilars true if the similar entries must be combined in single lines
     * @return List adapted for rendering : some results may be combined
     */
    List<PreparedIndexEntry> prepareDiffForRendering(List<PreparedIndexEntry> index, boolean combineSimilars) {

        // Complete HR payloads
        index.forEach(e -> {
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
     * @param knewKeys        the keys of the knew content from the index
     * @param preparation     for step update
     * @return
     */
    <T extends PreparedIndexEntry> Collection<T> generateDiffIndexFromContent(
            final Stream<ContentLine> actualContent,
            final Supplier<T> diffTypeBuilder,
            final Set<String> knewKeys,
            final Consumer<ContentLine> eachLineAccumulator,
            final DictionaryEntry dic,
            final PilotedCommitPreparation<?> preparation,
            final long pivotTimestamp) {

        LOGGER.info("Start diff index Generate for table \"{}\"", dic.getTableName());

        preparation.incrementProcessStep();

        DiffProcessingBuffer buffer = new DiffProcessingBuffer(1000);

        List<T> diff = new ArrayList<>();

        actualContent
                .peek(eachLineAccumulator)
                .forEach(line -> {

                    // We don't know it at all : it's an ADD
                    if (!knewKeys.remove(line.getKeyValue())) {
                        diff.add(preparedIndexEntry(diffTypeBuilder, IndexAction.ADD, line.getKeyValue(), line.getPayload(), null, dic));
                    } else {
                        buffer.keep(line);
                    }

                    // Process diff everytime the buffer is full
                    if (buffer.isBufferFull()) {
                        generateUpdateDiffOnBuffer(buffer, diffTypeBuilder, dic, diff, pivotTimestamp);
                    }
                });

        // One run on remaining update content in buffer
        generateUpdateDiffOnBuffer(buffer, diffTypeBuilder, dic, diff, pivotTimestamp);

        // Intermediate step for better percent process
        preparation.incrementProcessStep();

        // Then init "not found" knew keys as DELETE
        generateDeleteDiffOnRemainingKnewKeys(knewKeys, diffTypeBuilder, dic, diff, pivotTimestamp);

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
    Set<String> extractIndexEntryValueNames(Collection<? extends DiffLine> entries) {
        return entries.stream().map(DiffLine::getPayload)
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
     * Accessor to knew Content for testability
     *
     * @return
     */
    protected KnewContentRepository getKnewContents() {
        return this.knewContents;
    }

    /**
     * Identify all updates and (rare) "re-addition"
     */
    private <T extends PreparedIndexEntry> void generateUpdateDiffOnBuffer(
            final DiffProcessingBuffer buff,
            final Supplier<T> diffTypeBuilder,
            DictionaryEntry dic,
            List<T> diff,
            long pivotTimestamp) {

        // Process only if we have some content to process !
        if (buff.getIndex() > 0) {

            /*
             * Here we process only contents for which the key was present in local index.
             * If items is here, then it's because it's key was knew.
             *
             * Three possibilities here :
             *   * The knew content for this key is different from the actual one => It's a MODIFY in diff
             *   * The knew content for this key is equal to the actual one => No change, nothing added in diff
             *   * No knew content (while the key was knew) => It's a knew deleted item which is "recreated" => Rare "ADD" case in diff
             */

            // Generate the knew content for the current lines from index to get the exact "knew" content
            Map<String, String> knewContent = getKnewContents().knewContentForKeysBefore(dic, buff.extractKeys(), pivotTimestamp);

            boolean debug = LOGGER.isDebugEnabled();

            for (int i = 0; i < buff.getIndex(); i++) {

                ContentLine actualOne = buff.getContent()[i];

                // Found : for delete identification immediately remove from found ones
                String knewPayload = knewContent.get(actualOne.getKeyValue());

                // Nothing in knew payload : it's a (rare) "re-add"
                if (knewPayload == null && !knewContent.containsKey(actualOne.getKeyValue())) {

                    // Except if new is also empty will content is knew : it's a managed empty line
                    if (!(StringUtils.isEmpty(actualOne.getPayload()))) {

                        if (debug) {
                            LOGGER.debug("New endex entry for {} : ADD with \"{}\"",
                                    actualOne.getKeyValue(), actualOne.getPayload());
                        }

                        diff.add(preparedIndexEntry(diffTypeBuilder, IndexAction.ADD, actualOne.getKeyValue(),
                                actualOne.getPayload(), null, dic));
                    }
                }

                // Content is different : it's an Update
                else if (!Objects.equals(actualOne.getPayload(), knewPayload)) {

                    if (debug) {
                        LOGGER.debug("New index entry for {} : UPDATED from \"{}\" to \"{}\"",
                                actualOne.getKeyValue(), knewPayload, actualOne.getPayload());
                    }

                    diff.add(preparedIndexEntry(diffTypeBuilder, IndexAction.UPDATE, actualOne.getKeyValue(),
                            actualOne.getPayload(), knewPayload, dic));
                }

                // Else it's not modified
            }

            buff.reset();
        }
    }

    /**
     * Identify all updates and (rare) "re-addition"
     */
    private <T extends PreparedIndexEntry> void generateDeleteDiffOnRemainingKnewKeys(final Set<String> remainingKeys, final Supplier<T> diffTypeBuilder, DictionaryEntry dic, List<T> diff, long pivotTimestamp) {

        // Process only if we have some content to process !
        if (!remainingKeys.isEmpty()) {

            boolean debug = LOGGER.isDebugEnabled();

            Map<String, String> remainingKnewContents = getKnewContents().knewContentForKeysBefore(dic, remainingKeys, pivotTimestamp);

            // Remaining in knewContent are deleted ones
            remainingKnewContents.forEach((k, v) -> {
                if (debug) {
                    LOGGER.debug("New index entry for {} : REMOVE from \"{}\"", k, v);
                }
                diff.add(preparedIndexEntry(diffTypeBuilder, IndexAction.REMOVE, k, null, v, dic));
            });
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
     * @param mines
     * @param theirs
     * @param preparation
     * @return
     */
    private Collection<PreparedMergeIndexEntry> completeMergeIndexes(
            DictionaryEntry dict,
            Map<String, String> actualContent,
            Collection<? extends DiffLine> mines,
            Collection<? extends DiffLine> theirs,
            PilotedCommitPreparation<?> preparation,
            long pivotTimestamp) {

        LOGGER.debug("Completing merge data from index for parameter table {}", dict.getTableName());

        Collection<PreparedMergeIndexEntry> merge = new ArrayList<>();

        Map<String, List<DiffLine>> minesByKey = mines.stream().collect(Collectors.groupingBy(DiffLine::getKeyValue));
        Map<String, List<DiffLine>> theirsByKey = theirs.stream().collect(Collectors.groupingBy(DiffLine::getKeyValue));

        Set<String> allKeys = new HashSet<>();
        allKeys.addAll(minesByKey.keySet());
        allKeys.addAll(theirsByKey.keySet());

        boolean recordWarnings = this.features.isEnabled(Feature.RECORD_IMPORT_WARNINGS);
        AtomicInteger count = new AtomicInteger(0);
        int fireIncrem = allKeys.size() / 2;

        MergeProcessingBuffer buffer = new MergeProcessingBuffer(1000);

        for (String key : allKeys) {

            buffer.keep(key,
                    /* mine */DiffLine.combinedOnSameTableAndKey(minesByKey.get(key), false),
                    /* their */DiffLine.combinedOnSameTableAndKey(theirsByKey.get(key), true));

            // Process one page of buffer if ready
            if (buffer.isBufferFull()) {
                generateMergeDiffOnBuffer(buffer, actualContent, dict, merge, recordWarnings, preparation, pivotTimestamp);
            }

            // One increment only (when half are processed)
            if (count.incrementAndGet() == fireIncrem) {
                preparation.incrementProcessStep();
            }
        }

        // One final run for remaining buffer content
        generateMergeDiffOnBuffer(buffer, actualContent, dict, merge, recordWarnings, preparation, pivotTimestamp);

        return merge;
    }

    // For merge processing, process the merge result for one buffer
    private void generateMergeDiffOnBuffer(
            final MergeProcessingBuffer buff,
            Map<String, String> actualContent,
            DictionaryEntry dict,
            Collection<PreparedMergeIndexEntry> merge,
            boolean recordWarnings,
            PilotedCommitPreparation<?> preparation,
            long pivotTimestamp) {

        // Load knew payloads only for current buffer
        Map<String, DiffPayloads> buffKnewPayloads = getKnewContents().knewContentPayloadsForKeysBefore(dict, buff.extractKeys(), pivotTimestamp);

        // For the buffer content
        for (Map.Entry<String, ProcessingMergeLine> entry : buff.getContent().entrySet()) {

            try {
                // We will use both knew payload and previous payload
                DiffPayloads knewPayloads = buffKnewPayloads.get(entry.getKey());

                // Prepared mine / their pair
                DiffLine mine = entry.getValue().getMine();
                DiffLine their = entry.getValue().getTheir();

                // Ignore dead entries (ADDED then DELETED in regenerated content)
                if (mine != null || their != null) {

                    String knewPayload = null, knewPrevious = null;

                    if (knewPayloads != null) {
                        knewPayload = knewPayloads.getPayload();
                        knewPrevious = knewPayloads.getPrevious();
                    }

                    // Build HR
                    String mineHr = getConverter().convertToHrPayload(mine != null ? mine.getPayload() : null, knewPayload);
                    String theirHr = getConverter().convertToHrPayload(their != null ? their.getPayload() : null, knewPayload);

                    PreparedIndexEntry mineEntry = mine != null ? PreparedIndexEntry.fromCombined(mine, dict.getTableName(), mineHr) : null;
                    PreparedIndexEntry theirEntry = their != null ? PreparedIndexEntry.fromCombined(their, dict.getTableName(), theirHr) : null;

                    PreparedMergeIndexEntry resolved = this.mergeResolutionProcessor.resolveMerge(mineEntry, theirEntry, actualContent.get(entry.getKey()), knewPrevious);

                    // Dedicated logger output for resolutions
                    if (MERGE_LOGGER.isDebugEnabled()) {
                        if (resolved.isKept()) {
                            MERGE_LOGGER.debug("Resolved : Table \"{}\" - Key\"{}\" - mine = \"{}\", their = \"{}\", -> Get \"{}\" with rule \"{}\"",
                                    dict.getTableName(), entry.getKey(), mineEntry, theirEntry, resolved, resolved.getResolutionRule());
                        } else {
                            MERGE_LOGGER.debug("Resolved : Table \"{}\" - Key\"{}\" - mine = \"{}\", their = \"{}\", -> droped by rule \"{}\"",
                                    dict.getTableName(), entry.getKey(), mineEntry, theirEntry, resolved.getResolutionRule());
                        }
                    }

                    if (recordWarnings && resolved.getResolutionWarning() != null) {
                        this.anomalyService.addAnomaly(AnomalyContextType.MERGE, preparation.getSourceFilename(),
                                "Warning from " + resolved.getResolutionRule(),
                                "On " + resolved.toLogRendering() + " : " + resolved.getResolutionWarning());
                    }

                    // Drop immediately unselected line after resolutions (we wanted only to log / trace warnings for them)
                    if (resolved.isKept()) {
                        merge.add(resolved);
                    }
                }
            } catch (Throwable t) {
                LOGGER.error("Failed to process merge index entry on " + dict.getTableName() + "." + entry.getKey() + ", get error", t);
                throw new ApplicationException(ErrorType.MERGE_FAILURE, t);
            }
        }
        buff.reset();
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

        // TODO : Add too much complexity, should be dropped

        List<T> listToRender = new ArrayList<>();

        // Combine by HR payload
        Map<String, List<T>> combineds = readyToRender.stream()
                .collect(Collectors.groupingBy(p -> p.getHrPayload() != null ? p.getHrPayload() : ""));

        // Rendering display is based on combined
        combineds.values().forEach(e -> {

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

    /**
     * A simplified buffer of the content processed for diff generation.
     * Holds each <tt>ContentLine</tt> and process them each time the "buffer" is completed
     */
    private static class DiffProcessingBuffer {

        private final ContentLine[] content;
        private final int last;
        private int index = 0;

        DiffProcessingBuffer(int size) {
            this.last = size - 1;
            this.content = new ContentLine[size];
        }

        void keep(ContentLine line) {
            this.content[index] = line;
            index++;
        }

        boolean isBufferFull() {
            return this.index == this.last;
        }

        int getIndex() {
            return this.index;
        }

        ContentLine[] getContent() {
            return this.content;
        }

        void reset() {
            this.index = 0;
        }

        Collection<String> extractKeys() {

            // Extract keys from buffer
            List<String> keys = new ArrayList<>(this.index);

            // Simplest key extraction
            for (int i = 0; i < this.index; i++) {
                keys.add(this.content[i].getKeyValue());
            }

            return keys;
        }
    }

    /**
     * A simplified buffer of the content processed for merge generation.
     * Holds each <tt>ContentLine</tt> and process them each time the "buffer" is completed
     */
    private static class MergeProcessingBuffer {

        private final Map<String, ProcessingMergeLine> content;
        private final int size;

        MergeProcessingBuffer(int size) {
            this.size = size;
            this.content = new HashMap<>(size);
        }

        void keep(String key, DiffLine mine, DiffLine their) {
            this.content.put(key, new ProcessingMergeLine(mine, their));
        }

        boolean isBufferFull() {
            return this.content.size() == this.size;
        }

        Map<String, ProcessingMergeLine> getContent() {
            return this.content;
        }

        void reset() {
            this.content.clear();
        }

        Collection<String> extractKeys() {
            return this.content.keySet();
        }

    }

    private static class ProcessingMergeLine {

        private final DiffLine mine;
        private final DiffLine their;

        public ProcessingMergeLine(DiffLine mine, DiffLine their) {
            this.mine = mine;
            this.their = their;
        }

        public DiffLine getMine() {
            return mine;
        }

        public DiffLine getTheir() {
            return their;
        }
    }
}