package fr.uem.efluid.tools;

import fr.uem.efluid.model.DiffLine;
import fr.uem.efluid.model.entities.Commit;
import fr.uem.efluid.model.entities.DictionaryEntry;
import fr.uem.efluid.model.entities.IndexAction;
import fr.uem.efluid.model.repositories.CommitRepository;
import fr.uem.efluid.model.repositories.DictionaryRepository;
import fr.uem.efluid.model.repositories.IndexRepository;
import fr.uem.efluid.services.types.*;
import fr.uem.efluid.utils.ApplicationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.function.BiFunction;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static fr.uem.efluid.utils.ErrorType.PREPARATION_INTERRUPTED;

/**
 * Comparator driver between 2 commits
 *
 * @author elecomte
 * @version 1
 * @since v3.1.11
 */
@Component
public class CommitIndexComparator {

    private static final Logger LOGGER = LoggerFactory.getLogger(CommitIndexComparator.class);

    @Autowired
    private AsyncDriver async;

    @Autowired
    private DictionaryRepository dictionary;

    @Autowired
    private ManagedValueConverter converter;

    @Autowired
    private IndexRepository indexes;

    @Autowired
    private CommitRepository commits;

    public CommitCompareResult startCompareProcessOnImportedCommits(
            List<Commit> importedCommits,
            List<DictionaryEntrySummary> referencedDictionaries) {

        CommitCompareResult preparingResult = new CommitCompareResult(referencedDictionaries, importedCommits);

        // Start asynchronous process for compare
        this.async.start(preparingResult,
                (x) -> this.processOnAllDictionaryEntries(preparingResult, (r, d) -> callCompareForOneDictionaryEntryOnCommitIndexes(r, d, importedCommits)));

        // And provide immediately the building result
        return preparingResult;

    }

    /**
     * For "local" commit compare : analyze all the commits between two specified one, building an index content on all common keys
     * identified in the specified range of commits
     * <p>
     * The process will automatically sort the commits to always validate between the oldest and the youngest one
     * <p>
     * Start the process asynchronously : the result is built table after table, and the status should be checked to validate that
     * the compare is completed
     *
     * @param one range commit spec limit 1
     * @param two range commit spec limit 2
     * @return building result
     */
    public CommitCompareResult startCompareProcessBetweenCommits(Commit one, Commit two) {

        // Sort commits by date
        boolean oneIsFirst = oneIsFirst(one, two);
        Commit first = oneIsFirst ? one : two;
        Commit second = oneIsFirst ? two : one;

        // Get all COMMON processing tables
        List<DictionaryEntry> dictOne = this.dictionary.findUsedByCommitUuid(one.getUuid());
        Set<UUID> dictTwo = this.dictionary.findUsedByCommitUuid(two.getUuid()).stream().map(DictionaryEntry::getUuid).collect(Collectors.toSet());
        List<DictionaryEntrySummary> dictCommon = dictOne.stream()
                .filter(d -> dictTwo.contains(d.getUuid()))
                .map(d -> DictionaryEntrySummary.fromEntity(d, null))
                .collect(Collectors.toList());

        // Process only if there is some common tables between commits
        if (dictCommon.size() > 0) {

            // Get range of date for commits
            long rangeStart = this.indexes.findMaxIndexTimestampOfCommit(first.getUuid().toString());
            long rangeEnd = this.indexes.findMaxIndexTimestampOfCommit(second.getUuid().toString());

            CommitCompareResult preparingResult = new CommitCompareResult(dictCommon, Arrays.asList(first, second));

            // Start asynchronous process for compare
            this.async.start(preparingResult,
                    (x) -> this.processOnAllDictionaryEntries(preparingResult, (r, d) -> callCompareForOneDictionaryEntryOnRange(r, d, rangeStart, rangeEnd)));

            // And provide immediately the building result
            return preparingResult;
        }

        return CommitCompareResult.empty(Arrays.asList(first, second));
    }

    public List<CommitCompareHistoryEntry> getHistoryForComparedValue(
            CommitCompareResult currentCompare,
            UUID dictionaryEntryUuid,
            String keyValue) {

        // Get current commit range def
        CommitEditData first = currentCompare.getCompareCommits().iterator().next();
        CommitEditData second = currentCompare.getCompareCommits().get(currentCompare.getCompareCommits().size() - 1);

        // Get range of date for these current commits
        long rangeStart = this.indexes.findMaxIndexTimestampOfCommit(first.getUuid().toString());
        long rangeEnd = this.indexes.findMaxIndexTimestampOfCommit(second.getUuid().toString());

        // Get current table name
        String tableName = currentCompare.getReferencedTables().get(dictionaryEntryUuid).getTableName();

        // Then get all changes between the 2 commit for the specified key
        return this.indexes.findDiffLinesForKeysOnDictionaryEntryBetweenTimeRangeMappedToCommit(
                dictionaryEntryUuid,
                Collections.singletonList(keyValue),
                rangeStart,
                rangeEnd).get(keyValue).stream()
                .map(p -> {
                    UUID commitUuid = p.getFirst();
                    DiffLine diff = p.getSecond();

                    // Dedicated rendering for history of entry
                    CommitCompareHistoryEntry entry = CommitCompareHistoryEntry.fromCombined(diff, tableName, this.converter.convertToHrPayload(diff.getPayload(), diff.getPrevious()));
                    entry.setCommitUuid(commitUuid);

                    // Mark the entry as identified in compared commits
                    entry.setInComparedCommit(currentCompare.getCompareCommits().stream().anyMatch(c -> c.getUuid().equals(commitUuid)));

                    // Add details for associated commit
                    entry.setCommitDetails(CommitEditData.fromEntity(this.commits.getOne(commitUuid)));

                    return entry;
                }).collect(Collectors.toList());
    }

    /**
     * Global process, started asynchronous, with X callable to run also asynchronously
     *
     * @param result         preparing compare between commits
     * @param callableSource source for callable building the comparing result
     */
    private void processOnAllDictionaryEntries(
            CommitCompareResult result,
            BiFunction<CommitCompareResult, DictionaryEntrySummary, Callable<Void>> callableSource) {

        LOGGER.info("Begin compare process between {} commits", result.getCompareCommits().size());

        try {
            long startTimeout = System.currentTimeMillis();

            // Init callable to Process details for each common tables
            List<Callable<?>> callables = result.getReferencedTables().values()
                    .stream()
                    .map(d -> callableSource.apply(result, d))
                    .collect(Collectors.toList());

            LOGGER.info("Commit compare process started between {} commits with {} process to run",
                    result.getCompareCommits().size(), callables.size());

            result.setStatus(CommitCompareStatus.COMPARE_RUNNING);

            // Run compare on initialized callables
            this.async.processSteps(callables, result);

            // Mark compare as completed
            result.setStatus(CommitCompareStatus.COMPLETED);

            // And stop survey (on own thread)
            this.async.dropFromSurvey(result);

            LOGGER.info("Compare process completed between {} commits. Found {} index entries. Total process duration was {} ms",
                    result.getCompareCommits().size(),
                    result.getDiffContent().size(),
                    System.currentTimeMillis() - startTimeout);

        } catch (ApplicationException a) {
            LOGGER.error("Identified Local process error. Sharing", a);
            throw a;
        } catch (Throwable e) {
            LOGGER.error("Error will processing commit compare", e);
            result.fail(new ApplicationException(PREPARATION_INTERRUPTED, "Interrupted process", e));
        }
    }

    /**
     * <p>
     * Compare commit : Execution for one table, as a <tt>Callable</tt>, on a specified range of commits
     * </p>
     *
     * @param current building compare result
     * @param dict    current processing table
     * @return compare process as a callable
     */
    private Callable<Void> callCompareForOneDictionaryEntryOnRange(
            final CommitCompareResult current,
            final DictionaryEntrySummary dict,
            final long rangeStart,
            final long rangeEnd) {

        // Compare commit callable
        return () -> {

            CommitEditData firstCommit = current.getCompareCommits().get(0);
            CommitEditData secondCommit = current.getCompareCommits().get(current.getCompareCommits().size() - 1);

            // Search the common keys
            Set<String> keys = this.indexes.getCommonKeyValuesForCommitAndDictionaryEntry(
                    firstCommit.getUuid().toString(),
                    secondCommit.getUuid().toString(),
                    dict.getUuid().toString());

            // Then get all changes between the 2 commit for these keys
            Map<String, List<Pair<UUID, DiffLine>>> rawDiffs = this.indexes.findDiffLinesForKeysOnDictionaryEntryBetweenTimeRangeMappedToCommit(
                    dict.getUuid(),
                    keys,
                    rangeStart,
                    rangeEnd);

            // Combine and store changes
            rawDiffs.forEach((k, byCommits) -> {
                DiffLine combined = DiffLine.combinedOnSameTableAndKey(
                        byCommits.stream().map(Pair::getSecond).collect(Collectors.toList()), true);
                current.getDiffContent().add(CommitRangeCompareIndexEntry.fromCombined(
                        combined,
                        dict.getTableName(),
                        this.converter.convertToHrPayload(combined.getPayload(), combined.getPrevious()),
                        byCommits.stream().map(Pair::getFirst).collect(Collectors.toList())
                ));
            });

            // Update remaining
            int rem = current.getProcessRemaining().decrementAndGet();
            LOGGER.info("Completed 1 table compare between commits {} and {}. Remaining : {} / {}",
                    firstCommit.getUuid(), secondCommit.getUuid(), rem, current.getTotalTableCount());

            return null;
        };
    }

    /**
     * <p>
     * Compare commit : Execution for one table, as a <tt>Callable</tt>, on specified commit indexes
     * </p>
     *
     * @param current   building compare result
     * @param dict      current processing table
     * @param toCompare commits to compare
     * @return compare process as a callable
     */
    private Callable<Void> callCompareForOneDictionaryEntryOnCommitIndexes(
            final CommitCompareResult current,
            final DictionaryEntrySummary dict,
            final List<Commit> toCompare) {

        // Compare commit callable
        return () -> {

            // Extract items in a compliant format for compare
            Map<String, List<ComparedIndex>> allItems = toCompare.stream()
                    .flatMap(c -> ComparedIndex.splitCommitIndex(c, dict))
                    .collect(Collectors.groupingBy(ComparedIndex::getKeyValue));

            // Reference commit for rendering
            UUID referenceCommit = toCompare.iterator().next().getUuid();

            // Keep only entries identified in multiple commits
            allItems.entrySet().stream()
                    .filter(e -> e.getValue().size() > 1) // In multiple commits
                    .forEach(e -> {
                        Map<UUID, Pair<DiffLine, String>> contents = e.getValue().stream()
                                .collect(Collectors.toMap(ComparedIndex::getCommitUuid,
                                        c -> Pair.of(c.toDiffLine(dict), c.getHrPayload(this.converter))));

                        current.getDiffContent().add(CommitContentCompareIndexEntry.fromContents(contents, dict.getTableName(), referenceCommit));
                    });

            // Update remaining
            int rem = current.getProcessRemaining().decrementAndGet();
            LOGGER.info("Completed 1 table compare on {} commits. Remaining : {} / {}",
                    toCompare.size(), rem, current.getTotalTableCount());

            return null;
        };
    }

    /**
     * return true if commit specified in position one is first, using createdTime
     * or importedTime, regarding which one is specified
     */
    private static boolean oneIsFirst(Commit one, Commit two) {

        LocalDateTime forOne = one.getImportedTime() == null ? one.getCreatedTime() : one.getImportedTime();
        LocalDateTime forTwo = two.getImportedTime() == null ? two.getCreatedTime() : two.getImportedTime();

        return forOne.isBefore(forTwo);
    }

    private static class ComparedIndex {

        private final UUID commitUuid;
        private final IndexAction action;
        private final String keyValue;
        private final String payload;
        private final String previous;
        private final long timestamp;

        private ComparedIndex(UUID commitUuid, IndexAction action, String keyValue, String payload, String previous, long timestamp) {
            this.commitUuid = commitUuid;
            this.action = action;
            this.keyValue = keyValue;
            this.payload = payload;
            this.previous = previous;
            this.timestamp = timestamp;
        }

        public String getKeyValue() {
            return keyValue;
        }

        public UUID getCommitUuid() {
            return commitUuid;
        }

        public String getHrPayload(ManagedValueConverter converter) {
            return converter.convertToHrPayload(this.payload, this.previous);
        }

        public DiffLine toDiffLine(DictionaryEntrySummary dict) {

            return new DiffLine() {
                @Override
                public UUID getDictionaryEntryUuid() {
                    return dict.getUuid();
                }

                @Override
                public IndexAction getAction() {
                    return ComparedIndex.this.action;
                }

                @Override
                public long getTimestamp() {
                    return ComparedIndex.this.timestamp;
                }

                @Override
                public String getPrevious() {
                    return ComparedIndex.this.previous;
                }

                @Override
                public String getKeyValue() {
                    return ComparedIndex.this.keyValue;
                }

                @Override
                public String getPayload() {
                    return ComparedIndex.this.payload;
                }
            };

        }

        public static Stream<ComparedIndex> splitCommitIndex(Commit commit, DictionaryEntrySummary filter) {
            return commit.getIndex().stream()
                    .filter(i -> i.getDictionaryEntry().getUuid().equals(filter.getUuid()))
                    .map(i -> new ComparedIndex(
                            commit.getUuid(),
                            i.getAction(),
                            i.getKeyValue(),
                            i.getPayload(),
                            i.getPrevious(),
                            i.getTimestamp())
                    );
        }
    }
}
