package fr.uem.efluid.services;

import fr.uem.efluid.model.DiffLine;
import fr.uem.efluid.model.entities.*;
import fr.uem.efluid.model.repositories.IndexRepository;
import fr.uem.efluid.model.repositories.ManagedExtractRepository;
import fr.uem.efluid.model.repositories.ManagedRegenerateRepository;
import fr.uem.efluid.model.repositories.TableLinkRepository;
import fr.uem.efluid.services.types.*;
import fr.uem.efluid.tools.ManagedValueConverter;
import fr.uem.efluid.tools.MergeResolutionProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
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
public class PrepareIndexService {

    private static final Logger LOGGER = LoggerFactory.getLogger(PrepareIndexService.class);

    private static final Logger MERGE_LOGGER = LoggerFactory.getLogger("merge.analysis");

    @Autowired
    private ManagedExtractRepository rawParameters;

    @Autowired
    private ManagedRegenerateRepository regeneratedParamaters;

    @Autowired
    private ManagedValueConverter valueConverter;

    @Autowired(required = false)
    private MergeResolutionProcessor mergeResolutionProcessor;

    @Value("${datagate-efluid.display.combine-similar-diff-after}")
    private long maxSimilarBeforeCombined;

    @Value("${datagate-efluid.display.test-row-max-size}")
    private long maxForTestExtract;

    @Autowired
    private IndexRepository indexes;

    @Autowired
    private TableLinkRepository links;

    private boolean useParallelDiff = false;

    /**
     * <p>
     * Prepare the diff content, by extracting current content and building value to value
     * diff regarding the actual index content
     * </p>
     * <p>
     * Apply also some "remarks" to diff if required
     * </p>
     * <p>5 steps</p>
     *
     * @param diffToComplete the specified <tt>LocalPreparedDiff</tt> to complete with content and
     *                       remarks
     * @param entry          dictionaryEntry
     * @param lobs
     * @param project
     */
    public void completeCurrentContentDiff(
            LocalPreparedDiff diffToComplete,
            DictionaryEntry entry,
            Map<String, byte[]> lobs,
            Project project,
            PilotedCommitPreparation<?> preparation) {

        LOGGER.debug("Processing new diff for all content for managed table \"{}\"", entry.getTableName());

        // Here one of the app complexity : diff check using JDBC, for one table. Backlog
        // construction + restoration then diff.

        LOGGER.info("Start regenerate knew content for table \"{}\"", entry.getTableName());
        Map<String, String> knewContent = this.regeneratedParamaters.regenerateKnewContent(entry);

        // Intermediate step for better percent process
        preparation.incrementProcessStep();

        LOGGER.info("Regenerate done, start extract actual content for table \"{}\"", entry.getTableName());
        Map<String, String> actualContent = this.rawParameters.extractCurrentContent(entry, lobs, project);

        // Intermediate step for better percent process
        preparation.incrementProcessStep();

        // Some diffs may add remarks
        LOGGER.debug("Check if some remarks can be added to diff for table \"{}\"", entry.getTableName());
        processOptionalCurrentContendDiffRemarks(diffToComplete, entry, project, actualContent);

        // Intermediate step for better percent process
        preparation.incrementProcessStep();

        // Completed diff
        Collection<PreparedIndexEntry> index = generateDiffIndexFromContent(PreparedIndexEntry::new, knewContent, actualContent, entry);

        // Intermediate step for better percent process
        preparation.incrementProcessStep();

        // Detect and process similar entries for display
        diffToComplete.setDiff(
                combineSimilarDiffEntries(index, SimilarPreparedIndexEntry::fromSimilar)
                        .stream()
                        .sorted(Comparator.comparing(PreparedIndexEntry::getKeyValue)).collect(Collectors.toList()));

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
     * <p>
     * Apply also some "remarks" to diff if required
     * </p>
     * <p>7 steps</p>
     *
     * @param diffToComplete     the specified <tt>MergePreparedDiff</tt> to complete with content and
     *                           remarks
     * @param entry              dictionaryEntry
     * @param lobs               for any extracted lobs
     * @param timeStampForSearch
     * @param mergeContent
     * @param project
     */
    public void completeMergeIndexDiff(
            MergePreparedDiff diffToComplete,
            DictionaryEntry entry,
            Map<String, byte[]> lobs,
            long timeStampForSearch,
            List<PreparedMergeIndexEntry> mergeContent,
            Project project,
            PilotedCommitPreparation<?> preparation) {

        // Temporary switchable process
        if (this.mergeResolutionProcessor != null) {
            newCompleteMergeIndexDiff(diffToComplete, entry, lobs, timeStampForSearch, mergeContent, project, preparation);
            return;
        }

        LOGGER.debug("Regenerating values from combined local + specified index for managed table \"{}\", using"
                + " timestamp for local index search {}", entry.getTableName(), timeStampForSearch);

        // Here one other complexity in the app : diff between 2 different indexes, which
        // can be related themselves to a content diff previously generated. Used for
        // merging process

        List<IndexEntry> localIndexToTimeStamp = this.indexes.findByDictionaryEntryAndTimestampGreaterThanEqual(entry,
                timeStampForSearch);

        preparation.incrementProcessStep();

        // Prepare simulated "knew index"
        List<DiffLine> toProcess = new ArrayList<>();
        toProcess.addAll(localIndexToTimeStamp);
        toProcess.addAll(mergeContent);

        LOGGER.info("Start regenerate knew content for table \"{}\"", entry.getTableName());
        Map<String, String> knewContent = this.regeneratedParamaters.regenerateKnewContent(toProcess);

        preparation.incrementProcessStep();

        LOGGER.info("Regenerate done, start extract actual content for table \"{}\"", entry.getTableName());
        Map<String, String> actualContent = this.rawParameters.extractCurrentContent(entry, lobs, project);

        preparation.incrementProcessStep();

        // Some diffs may add remarks on local contents
        LOGGER.debug("Check if some remarks can be added to diff for table \"{}\"", entry.getTableName());
        processOptionalCurrentContendDiffRemarks(diffToComplete, entry, project, actualContent);

        preparation.incrementProcessStep();

        Collection<PreparedMergeIndexEntry> diff = generateDiffIndexFromContent(PreparedMergeIndexEntry::new, knewContent, actualContent,
                entry);

        preparation.incrementProcessStep();

        LOGGER.info("Diff prepared. Complete merge data for table \"{}\"", entry.getTableName());

        // Then apply from local and from import to identify diff changes
        completeMergeIndexes(entry, localIndexToTimeStamp, mergeContent, diff);

        preparation.incrementProcessStep();

        diff.forEach(line -> {
            line.setSelected(true);
        });

        diffToComplete.setDiff(
                combineSimilarDiffEntries(diff, SimilarPreparedMergeIndexEntry::fromSimilar).stream()
                        .sorted(Comparator.comparing(PreparedIndexEntry::getKeyValue)).collect(Collectors.toList()));

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
     * <p>
     * Apply also some "remarks" to diff if required
     * </p>
     * <p>7 steps</p>
     *
     * @param diffToComplete     the specified <tt>MergePreparedDiff</tt> to complete with content and
     *                           remarks
     * @param entry              dictionaryEntry
     * @param lobs               for any extracted lobs
     * @param timeStampForSearch
     * @param mergeDiff
     * @param project
     */
    public void newCompleteMergeIndexDiff(
            MergePreparedDiff diffToComplete,
            DictionaryEntry entry,
            Map<String, byte[]> lobs,
            long timeStampForSearch,
            List<? extends PreparedIndexEntry> mergeDiff,
            Project project,
            PilotedCommitPreparation<?> preparation) {

        // Index "previous"
        List<IndexEntry> localIndexToTimeStamp = this.indexes.findByDictionaryEntryAndTimestampLessThanEqual(entry,
                timeStampForSearch);

        preparation.incrementProcessStep();

        // Build "previous" content from index
        Map<String, String> previousContent = this.regeneratedParamaters.regenerateKnewContent(localIndexToTimeStamp);

        preparation.incrementProcessStep();

        // Get actual content
        Map<String, String> actualContent = this.rawParameters.extractCurrentContent(entry, lobs, project);

        preparation.incrementProcessStep();

        // Build a direct diff between previous content and actual content
        Collection<PreparedIndexEntry> mineDiff = generateDiffIndexFromContent(PreparedIndexEntry::new, previousContent, actualContent, entry);

        preparation.incrementProcessStep();

        // Build merge diff entries from 2 source of Diff + previous content
        Collection<PreparedMergeIndexEntry> completedMergeDiff = newCompleteMergeIndexes(entry, actualContent, mineDiff, mergeDiff, previousContent, preparation);

        preparation.incrementProcessStep();

        // Combine similar
        diffToComplete.setDiff(
                combineSimilarDiffEntries(completedMergeDiff, SimilarPreparedMergeIndexEntry::fromSimilar).stream()
                        .sorted(Comparator.comparing(PreparedIndexEntry::getKeyValue)).collect(Collectors.toList()));

        preparation.incrementProcessStep();
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
     * @return test data in dedicated bean with total count + configured extracted lines
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
     * @param dic
     * @param index
     * @return List adapted for rendering : some results may be combined
     */
    List<PreparedIndexEntry> prepareDiffForRendering(DictionaryEntry dic, List<PreparedIndexEntry> index) {

        // Get all previouses for HR payload init
        Map<String, IndexEntry> previouses = this.indexes.findAllPreviousIndexEntriesExcludingExisting(dic, index);

        // Complete HR payloads
        index.forEach(e -> {
            IndexEntry previous = previouses.get(e.getKeyValue());
            String hrPayload = getConverter().convertToHrPayload(e.getPayload(), previous != null ? previous.getPayload() : null);
            e.setHrPayload(hrPayload);
        });

        // And then combine for rendering
        return combineSimilarDiffEntries(index, SimilarPreparedIndexEntry::fromSimilar);
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

        LOGGER.info("Start diff index Generate for table \"{}\"", dic.getTableName());

        final Set<T> diff = ConcurrentHashMap.newKeySet();

        // Use parallel process for higher distribution of verification
        switchedStream(actualContent).forEach(actualOne -> searchDiff(diffTypeBuilder, actualOne, knewContent, diff, dic));

        LOGGER.info("Existing vs Knew content diff completed for table \"{}\". Start not managed knew content", dic.getTableName());

        // Remaining in knewContent are deleted ones
        switchedStream(knewContent).forEach(e -> {
            LOGGER.debug("New endex entry for {} : REMOVE from \"{}\"", e.getKey(), e.getValue());
            diff.add(preparedIndexEntry(diffTypeBuilder, IndexAction.REMOVE, e.getKey(), null, e.getValue(), dic));
        });

        LOGGER.info("Not managed knew content completed for table \"{}\".", dic.getTableName());

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
     * @param parallel
     */
    protected void applyParallelMode(boolean parallel) {
        this.useParallelDiff = parallel;
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
     * @param diffToComplete
     * @param entry
     * @param project
     * @param actualContent
     */
    private void processOptionalCurrentContendDiffRemarks(
            DiffDisplay<?> diffToComplete,
            DictionaryEntry entry,
            Project project,
            Map<String, String> actualContent) {

        // If parameter table has links, check also the count with unchecked joins
        if (this.links.countLinksForDictionaryEntry(entry) > 0) {
            LOGGER.debug("Start checking count of entries with unchecked joins for table \"{}\"", entry.getTableName());

            // If difference identified in content size with unchecked join, add a remark
            if (this.rawParameters.countCurrentContentWithUncheckedJoins(entry, project) > actualContent.size()) {

                // Get the missign payloads as display list
                List<ContentLineDisplay> missingContent = this.rawParameters.extractCurrentMissingContentWithUncheckedJoins(entry, project)
                        .entrySet().stream()
                        .map(e -> new ContentLineDisplay(e.getKey(), e.getValue()))
                        .collect(Collectors.toList());

                if (missingContent.size() > 0) {
                    // Prepare the corresponding remark
                    DiffRemark<List<ContentLineDisplay>> remark = new DiffRemark<>(
                            MISSING_ON_UNCHECKED_JOIN, "table " + entry.getTableName(), missingContent);

                    diffToComplete.addRemark(remark);

                    LOGGER.info("Found a count of {} missing entries with unchecked joins for table \"{}\"",
                            missingContent.size(), entry.getTableName());
                }
            }
        }
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
     * @param diffTypeBuilder
     * @param actualOne
     * @param knewContent
     * @param diff
     * @param dic
     * @param <T>
     */
    private <T extends PreparedIndexEntry> void searchDiff(
            final Supplier<T> diffTypeBuilder,
            final Map.Entry<String, String> actualOne,
            final Map<String, String> knewContent,
            final Set<T> diff,
            final DictionaryEntry dic) {

        boolean wasKnew = knewContent.containsKey(actualOne.getKey());

        // Found : for delete identification immediately remove from found ones
        String knewPayload = knewContent.remove(actualOne.getKey());

        // Exist already
        if (!StringUtils.isEmpty(knewPayload)) {

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

            // Except if new is also empty will content is knew : it's a managed empty line
            if (!(wasKnew && StringUtils.isEmpty(actualOne.getValue()))) {
                LOGGER.debug("New endex entry for {} : ADD with \"{}\"", actualOne.getKey(), actualOne.getValue());
                diff.add(preparedIndexEntry(diffTypeBuilder, IndexAction.ADD, actualOne.getKey(), actualOne.getValue(), null, dic));
            }
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

        entry.setTimestamp(System.currentTimeMillis());

        // Complete from dict
        entry.setDictionaryEntryUuid(dic.getUuid());

        return entry;
    }


    /**
     * @param dict
     * @param actualContent
     * @param mines
     * @param theirs
     * @param previousContent
     * @param preparation
     * @return
     */
    private Collection<PreparedMergeIndexEntry> newCompleteMergeIndexes(
            DictionaryEntry dict,
            Map<String, String> actualContent,
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

        AtomicInteger count = new AtomicInteger(0);
        int fireIncrem = allKeys.size() / 2;

        // One combined key for each keys
        return allKeys.stream().map(key -> {

            String previous = previousContent.get(key);
            DiffLine mine = DiffLine.combinedOnSameTableAndKey(minesByKey.get(key), false);
            DiffLine their = DiffLine.combinedOnSameTableAndKey(theirsByKey.get(key), true);

            // Ignore dead entries (ADDED then DELETED in regenerated content)
            if (mine == null && their == null) {
                return null;
            }

            String mineHr = getConverter().convertToHrPayload(mine != null ? mine.getPayload() : null, previous);
            String theirHr = getConverter().convertToHrPayload(their != null ? their.getPayload() : null, previous);

            PreparedIndexEntry mineEntry = mine != null ? PreparedIndexEntry.fromCombined(mine, mineHr) : null;
            PreparedIndexEntry theirEntry = their != null ? PreparedIndexEntry.fromCombined(their, theirHr) : null;

            // One increment only (when half are processed)
            if (count.incrementAndGet() == fireIncrem) {
                preparation.incrementProcessStep();
            }

            return this.mergeResolutionProcessor.resolveMerge(mineEntry, theirEntry, actualContent.get(key));
        }).filter(Objects::nonNull).collect(Collectors.toList());
    }


    /**
     * @param dict
     * @param local
     * @param mergeSource
     * @param preparingMergeIndexToComplete
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

        if (MERGE_LOGGER.isDebugEnabled()) {
            MERGE_LOGGER.debug(
                    "Beggin Merge resolution search for dict {} (table {}). Will process on {} previous, with {} \"mines\" and {} \"theirs\"",
                    dict.getUuid(), dict.getTableName(), previouses.size(), minesByKey.size(), theirsByKey.size());
        }

        // For each merge result (Only one possible for each keyValue)
        preparingMergeIndexToComplete.forEach(mergeEntry -> {

            // Expose properties for debug
            String key = mergeEntry.getKeyValue();
            List<? extends DiffLine> mineDiff = minesByKey.get(key);
            List<? extends DiffLine> theirDiff = theirsByKey.remove(key);
            IndexEntry previous = previouses.get(key);
            DiffLine mineCombined = DiffLine.combinedOnSameTableAndKey(mineDiff, false);
            DiffLine theirCombined = DiffLine.combinedOnSameTableAndKey(theirDiff, true);

            // Adapt auto-resolution of entry
            mergeResolution(mergeEntry, previous, mineCombined, theirCombined);

            // Mark as a diff which need action
            mergeEntry.setNeedAction(true);

            LOGGER.debug("Completion on merge data for table {}, key \"{}\", \"HrPayload\"=\"{}\", \"mine\"={}, \"their\"={}",
                    dict.getTableName(), mergeEntry.getKeyValue(), mergeEntry.getHrPayload(), mergeEntry.getMine(), mergeEntry.getTheir());
        });

        // Add references to remaining "theirs"
        theirsByKey.forEach((key, value) -> {

            IndexEntry localPrevious = previouses.get(key);
            DiffLine foundTheir = DiffLine.combinedOnSameTableAndKey(value, true);

            // Ignore when no "mine" and "auto-erased" import
            if (localPrevious != null && foundTheir != null) {

                // Create "nothing to do" MergeEntry
                PreparedMergeIndexEntry merge = mergeAutoApplyTheir(localPrevious, foundTheir);

                // If not found in the diff : was already imported
                merge.setNeedAction(false);

                preparingMergeIndexToComplete.add(merge);
            }
        });
    }

    /**
     * Apply "their" immediately
     *
     * @param localPrevious
     * @param foundTheir
     * @return
     */
    private PreparedMergeIndexEntry mergeAutoApplyTheir(
            IndexEntry localPrevious,
            DiffLine foundTheir) {

        // Prepare payload for "their" regarding local previous (if any)
        String theirHrPayload = foundTheir != null ? getConverter().convertToHrPayload(foundTheir.getPayload(),
                localPrevious != null ? localPrevious.getPayload() : null) : null;

        // Corresponding details for the "their"
        PreparedIndexEntry theirEntry = PreparedIndexEntry.fromCombined(foundTheir, theirHrPayload);

        // Create "nothing to do" MergeEntry
        PreparedMergeIndexEntry their = PreparedMergeIndexEntry.fromExistingTheir(theirEntry);

        if (MERGE_LOGGER.isDebugEnabled()) {

            if (foundTheir == null) {
                MERGE_LOGGER.debug("Auto applied \"their\" on one Entry, but without specified foundTheir");
            } else {
                MERGE_LOGGER.debug(
                        "Auto applied \"their\" on Entry {}, entry key {} (no resolution needed): localPrevious={}/{}, foundTheir={}/{}, preparedTheir={}/{}/{}",
                        foundTheir.getDictionaryEntryUuid(), foundTheir.getKeyValue(),
                        localPrevious != null ? localPrevious.getAction() : "?",
                        localPrevious != null ? localPrevious.getPayload() : " - N/A - ",
                        foundTheir.getAction(), foundTheir.getPayload(),
                        their.getAction(), their.getPayload(), their.getHrPayload());
            }
        }

        return their;
    }

    /**
     * Process resolution of merge
     *
     * @param mergeEntry
     * @param localPrevious
     * @param foundMine
     * @param foundTheir
     */
    private void mergeResolution(
            PreparedMergeIndexEntry mergeEntry,
            IndexEntry localPrevious,
            DiffLine foundMine,
            DiffLine foundTheir) {

        String previousPayload = localPrevious != null && !localPrevious.equals(foundMine) ? localPrevious.getPayload() : null;

        // For clean HR, need to mark it precisely on addition
        boolean localInitOnly = foundMine != null && foundMine.getAction() == IndexAction.ADD && foundMine.getPayload() != null
                && foundMine.getPayload().equals(previousPayload);

        // Complete current entry HR payload for rendering
        String currentHrPayload = getConverter().convertToHrPayload(mergeEntry.getPayload(), localInitOnly ? null : previousPayload);
        mergeEntry.setHrPayload(currentHrPayload);

        // If mine is there, complete payload and add combined
        if (foundMine != null) {
            String mineHrPayload = getConverter().convertToHrPayload(foundMine.getPayload(), localInitOnly ? null : previousPayload);
            mergeEntry.setMine(PreparedIndexEntry.fromCombined(foundMine, mineHrPayload));

            // Special case : Added on mine, nothing on their => Propose deletion of mine
            if (foundTheir == null) {
                mergeEntry.setAction(IndexAction.REMOVE);
            }
        }

        // Else copy proposition (case if current diff entry not yet commited)
        else {
            mergeEntry.setMine(PreparedIndexEntry.fromCombined(mergeEntry, currentHrPayload));
        }

        // If their is there, complete payload and add combined
        if (foundTheir != null) {
            String theirHrPayload = getConverter().convertToHrPayload(foundTheir.getPayload(), previousPayload);
            mergeEntry.setTheir(PreparedIndexEntry.fromCombined(foundTheir, theirHrPayload));

            // Case : their was modified after mine => Default resolution became "their"
            if (foundMine == null || foundMine.getTimestamp() < foundTheir.getTimestamp()) {
                String combinedHrPayload = localPrevious != null
                        ? getConverter().convertToHrPayload(foundTheir.getPayload(), localPrevious.getPayload())
                        : theirHrPayload;
                mergeEntry.applyResolution(foundTheir, combinedHrPayload);
            }

            // Default resolution is always their : can select individually
            else {
                mergeEntry.setHrPayload(getConverter().convertToHrPayload(foundTheir.getPayload(), foundMine.getPayload()));
                mergeEntry.setPayload(foundTheir.getPayload());
            }
        }

        if (MERGE_LOGGER.isDebugEnabled()) {
            MERGE_LOGGER.debug(
                    "Merge Resolution on Entry {}, entry key {} (resolution needed) : previous={}/{}/{}, mine={}/{}/{}, their={}/{}/{}, mergedResolution={}/{}/{}",
                    mergeEntry.getDictionaryEntryUuid(), mergeEntry.getKeyValue(),
                    localPrevious != null ? localPrevious.getAction() : "?",
                    localPrevious != null ? localPrevious.getPayload() : " - N/A - ", currentHrPayload,
                    mergeEntry.getMine().getAction(), mergeEntry.getMine().getPayload(), mergeEntry.getMine().getHrPayload(),
                    mergeEntry.getTheir().getAction(), mergeEntry.getTheir().getPayload(), mergeEntry.getTheir().getHrPayload(),
                    mergeEntry.getAction(), mergeEntry.getPayload(), mergeEntry.getHrPayload());
        }

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
    private <T extends PreparedIndexEntry> List<T> combineSimilarDiffEntries(Collection<T> readyToRender,
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