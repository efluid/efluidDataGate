package fr.uem.efluid.services;

import fr.uem.efluid.model.DiffLine;
import fr.uem.efluid.model.entities.*;
import fr.uem.efluid.model.metas.ManagedModelDescription;
import fr.uem.efluid.model.metas.TableDescription;
import fr.uem.efluid.model.repositories.*;
import fr.uem.efluid.services.types.*;
import fr.uem.efluid.tools.attachments.AttachmentProcessor;
import fr.uem.efluid.tools.diff.CommitIndexComparator;
import fr.uem.efluid.tools.diff.RollbackConverter;
import fr.uem.efluid.tools.versions.VersionContentChangesGenerator;
import fr.uem.efluid.utils.ApplicationException;
import fr.uem.efluid.utils.Associate;
import fr.uem.efluid.utils.ErrorType;
import fr.uem.efluid.utils.FormatUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static fr.uem.efluid.utils.ErrorType.*;

/**
 * <p>
 * Features for commit preparation. Everything that can need a new diff preparation
 * <b>must</b> be used only from {@link PilotableCommitPreparationService} for init.
 * </p>
 *
 * @author elecomte
 * @version 6
 * @since v0.0.1
 */
@Transactional
@Service
public class CommitService extends AbstractApplicationService {

    public static final String PCKG_ALL = "commits-all";
    public static final String PCKG_AFTER = "commits-part";

    public static final String PCKG_LOBS = "lobs";
    public static final String PCKG_ATTACHS = "attachs";
    public static final String PCKG_TRANSFORMERS = "transformers";
    public static final String PCKG_VERSIONS = "versions";
    public static final String PCKG_INDEX = "indexes";

    private static final String PCKG_CHERRY_PICK = "commits-cherry-pick";

    private static final Logger LOGGER = LoggerFactory.getLogger(CommitService.class);

    @Value("${datagate-efluid.display.details-page-size}")
    private int detailsDisplayPageSize;

    @Value("${datagate-efluid.display.diff-page-size}")
    private int diffDisplayPageSize;

    @Autowired
    private CommitRepository commits;

    @Autowired
    private PrepareIndexService diffs;

    @Autowired
    private IndexRepository indexes;

    @Autowired
    private ExportRepository exports;

    @Autowired
    private FunctionalDomainRepository domains;

    @Autowired
    private DictionaryRepository dictionary;

    @Autowired
    private LobPropertyRepository lobs;

    @Autowired
    private ExportImportService exportImportService;

    @Autowired
    private ApplyDiffService applyDiffService;

    @Autowired
    private ProjectManagementService projectService;

    @Autowired
    private VersionRepository versions;

    @Autowired
    private AttachmentRepository attachments;

    @Autowired
    private AttachmentProcessor.Provider attachProcs;

    @Autowired
    private TransformerService transformerService;

    @Autowired
    private ApplicationDetailsService appDetailsService;

    @Autowired
    private VersionContentChangesGenerator changesGenerator;

    @Autowired
    private RollbackConverter rollbackConverter;

    @Autowired
    private CommitIndexComparator commitIndexComparator;

    @Autowired
    private DatabaseDescriptionRepository metadatas;

    // One active only - not a session : JUST 1 FOR ALL APP BY PROJECT
    private final Map<UUID, CommitCompareResult> currentCommitCompareResults = new HashMap<>();

    /**
     *
     */
    public CommitExportEditData initCommitExport(CommitExportEditData.CommitSelectType type, UUID commitUUID) {

        this.projectService.assertCurrentUserHasSelectedProject();

        Project project = this.projectService.getCurrentSelectedProjectEntity();

        LOGGER.debug("Asking for a commit export init for project {}, with type {} from commit \"{}\"",
                project.getName(), type, commitUUID != null ? "UUID " + commitUUID : "ALL");

        CommitExportEditData preparation = new CommitExportEditData();

        preparation.setCommitSelectType(type);

        // Get comment on specified commit if any
        if (commitUUID != null) {

            Commit commit = this.commits.findById(commitUUID)
                    .orElseThrow(() -> new ApplicationException(COMMIT_EXISTS, "Specified commit " + commitUUID + " doesn't exist"));

            preparation.setSelectedCommitUuid(commitUUID);
            preparation.setSelectedCommitComment(commit.getComment());
            preparation.setSelectedCommitVersion(commit.getVersion().getName());
        }

        // Else it's an "export all" situation
        else {
            preparation.setSelectedCommitComment("ALL");
            Version version = this.versions.getLastVersionForProject(project);
            if (version != null) {
                preparation.setSelectedCommitVersion(version.getName());
            } else {
                preparation.setSelectedCommitVersion(NOT_SET_VERSION_NAME);
            }
        }

        // Init transformer config map
        preparation.setSpecificTransformers(
                this.transformerService.getAllTransformerDefConfigs().entrySet().stream()
                        .collect(Collectors.toMap(Map.Entry::getKey,
                                e -> new CommitExportEditData.CustomTransformerConfiguration(e.getValue())))
        );

        return preparation;
    }

    /**
     * Save prepared export and provides UUID. Real export process will be started from specified uuid
     *
     * @param editData prepared export details
     * @return saved export display details if no error in process.
     */
    public CommitExportDisplay saveCommitExport(CommitExportEditData editData) {

        this.projectService.assertCurrentUserHasSelectedProject();
        Project project = this.projectService.getCurrentSelectedProjectEntity();

        // Prepare export content
        Export export = new Export(UUID.randomUUID());

        // Cherry-pick - range with same commit
        if (editData.getCommitSelectType() == CommitExportEditData.CommitSelectType.SINGLE_ONE) {
            export.setStartCommit(new Commit(editData.getSelectedCommitUuid()));
            export.setEndCommit(new Commit(editData.getSelectedCommitUuid()));
            export.setFilename(commitExportName("single", editData.getSelectedCommitUuid()));
        }

        // Range : get a start and an end commit
        else {
            List<Commit> allCommits = sortedCommits(project).collect(Collectors.toList());

            // All range exports until last ...
            export.setEndCommit(allCommits.get(allCommits.size() - 1));

            if (editData.getSelectedCommitUuid() == null) {
                // ... From first
                export.setStartCommit(allCommits.get(0));
                export.setFilename(commitExportName("all", null));
            } else {
                // ... From selected
                export.setStartCommit(new Commit(editData.getSelectedCommitUuid()));
                export.setFilename(commitExportName("until", editData.getSelectedCommitUuid()));
            }
        }

        // Transformer customization (store ALL)
        if (editData.getSpecificTransformers() != null) {
            editData.getSpecificTransformers().forEach((k, v) -> {
                ExportTransformer et = new ExportTransformer();
                et.setConfiguration(v.getConfiguration());
                et.setDisabled(v.isDisabled());
                et.setTransformerDef(new TransformerDef(k));
                et.setExport(export);
                export.getTransformers().add(et);
            });
        }

        export.setCreatedTime(LocalDateTime.now());
        export.setProject(project);

        return new CommitExportDisplay(this.exports.save(export));
    }

    /**
     * (for test needs) prepare an export and process it immediately, in one single transaction
     *
     * @param editData export data
     * @return ExportImportResult
     */
    public ExportImportResult<ExportFile> saveAndExportImmediate(CommitExportEditData editData) {

        // From the edited data, create an export without transformer customization ...
        CommitExportDisplay exportDisplay = saveCommitExport(editData);

        // And process it
        return processCommitExport(exportDisplay.getUuid());
    }

    /**
     * Check if the specified commit export has been downloaded : download is processed only when the file is completed, so if not
     * downloaded = the file is generated, if downloaded = a download time has been added to export entity = the file has
     * been fully generated = the export has been downloaded.
     * <p>
     * Used for loading screen on export download
     *
     * @param commitExportUuid prepared commit export ready to be downloaded / downloaded
     * @return true if already downloaded
     */
    public boolean isCommitExportDownloaded(UUID commitExportUuid) {
        return this.exports.getOne(commitExportUuid).getDownloadedTime() != null;
    }

    /**
     * <p>
     * Export complete commit list, with option to include only one fraction of the index
     * content. Support ALL, UNTIL and CHERRY PICK export
     * </p>
     * <p>Update the export to mark it as downloaded</p>
     *
     * @param commitExportUuid Id of prepared commit <tt>Export</tt>
     */
    public ExportImportResult<ExportFile> processCommitExport(UUID commitExportUuid) {

        /*
         * Export contains all commits. But it is possible to load commit "ref" only for
         * old commits, and to do a chery pick, with one selected commit
         */

        this.projectService.assertCurrentUserHasSelectedProject();

        Project project = this.projectService.getCurrentSelectedProjectEntity();

        LOGGER.debug("Asking to process the prepared commit export {} for project {}",
                commitExportUuid, project.getName());

        Export preparedExport = this.exports.getOne(commitExportUuid);

        LOGGER.info("Prepare partial commit export. Will use ref only for all commits BEFORE {} into project {}",
                preparedExport.getStartCommit(), project.getName());

        LocalDateTime rangeEnd = preparedExport.getEndCommit().getCreatedTime();
        LocalDateTime rangeBegin = preparedExport.getStartCommit().getCreatedTime();

        // Will process every commits in stream, work with atomic references
        final AtomicLong refOnlyCommitCount = new AtomicLong(0);
        final AtomicBoolean processedFirst = new AtomicBoolean(false);

        // If start from first, it's an "ALL"
        final AtomicReference<String> pckgName = new AtomicReference<>(PCKG_ALL);

        List<UUID> commitUuids = new ArrayList<>();

        // Need identified and prepared commits (stream must be processed)
        List<Commit> commitsToExport = sortedCommits(project)
                // If do not start with the first = it's a partial export
                .peek(c -> {
                    if (!processedFirst.get() && !preparedExport.getStartCommit().equals(c)) {

                        // Single selection while it's not the last one, mark it as a partial
                        if (preparedExport.getStartCommit().equals(preparedExport.getEndCommit())) {
                            pckgName.set(PCKG_CHERRY_PICK);
                        } else {
                            pckgName.set(PCKG_AFTER);
                        }
                        processedFirst.set(true);
                    }
                })
                // Keep in range
                .filter(c -> !c.getCreatedTime().isAfter(rangeEnd))
                // And mark previous ones as "ref only"
                .peek(c -> {
                    if (c.getCreatedTime().isBefore(rangeBegin)) {
                        c.setAsRefOnly();
                        refOnlyCommitCount.incrementAndGet();
                    } else {
                        commitUuids.add(c.getUuid());
                    }
                })
                // Sorted for processing
                .sorted(Comparator.comparing(Commit::getCreatedTime))
                .collect(Collectors.toList());

        // Then export :
        ExportFile file = exportContent(project, pckgName.get(), preparedExport.getTransformers(), commitsToExport, commitUuids);
        ExportImportResult<ExportFile> result = new ExportImportResult<>(file);

        // Update count is the "ref only" count
        result.addCount(pckgName.get(), commitsToExport.size() - refOnlyCommitCount.get(), refOnlyCommitCount.get(), 0);

        LOGGER.info("Export package for commit is ready. {} total commits exported for project \"{}\", "
                        + "uncluding {} exported as ref only. File size is {}b",
                commitsToExport.size(), project.getName(), refOnlyCommitCount.get(), file.getSize());

        // Mark as completed
        preparedExport.setDownloadedTime(LocalDateTime.now());
        this.exports.saveAndFlush(preparedExport);

        // Result is for display / File load
        return result;
    }

    /**
     * Process export call for the prepared commit content
     *
     * @param project           source project for export
     * @param commitPackageName destination package name
     * @param transformers      transformers to include in export
     * @param commitsToExport   list of commit to export
     * @param commitUuids       range of commit for export source
     * @return ExportFile of selected commit
     */
    @Transactional(propagation = Propagation.NEVER)
    protected ExportFile exportContent(
            Project project,
            String commitPackageName,
            Collection<ExportTransformer> transformers,
            List<Commit> commitsToExport,
            List<UUID> commitUuids) {

        Stream<IndexEntry> indexToExport = this.indexes.findByCommitUuidInOrderByTimestamp(commitUuids);

        return this.exportImportService.exportPackages(Arrays.asList(
                // Add customized transformers
                new TransformerDefPackage(PCKG_TRANSFORMERS, LocalDateTime.now())
                        .from(this.transformerService.getCustomizedTransformerDefForExport(project, transformers)),

                // Add referenced versions (for dict content compatibility check at import)
                new VersionPackage(PCKG_VERSIONS, LocalDateTime.now())
                        .from(this.versions.findVersionForCommitUuidsIn(commitUuids).peek(v -> v.setSerializeDictionaryContents(true))),

                // Add selected commit(s)
                new CommitPackage(commitPackageName, LocalDateTime.now())
                        .from(commitsToExport.stream()),

                // Add associated index
                new IndexEntryPackage(PCKG_INDEX, LocalDateTime.now())
                        .from(indexToExport),

                // Add lobs associated to commits
                new LobPropertyPackage(PCKG_LOBS, LocalDateTime.now())
                        .from(this.lobs.findByCommitUuidIn(commitUuids)),

                // Add all commit attachments
                new AttachmentPackage(PCKG_ATTACHS, LocalDateTime.now())
                        .from(this.attachments.findByCommitUuidIn(commitUuids)))
        );
    }

    /**
     * @return existing commits
     */
    public List<CommitEditData> getAvailableCommits() {

        this.projectService.assertCurrentUserHasSelectedProject();
        Project project = this.projectService.getCurrentSelectedProjectEntity();

        LOGGER.debug("Request for list of available commits for project ");

        Map<UUID, List<String>> domainNames = this.domains.loadAllDomainNamesByCommitUuids(project);

        return this.commits.findByProject(project).stream()
                .map(CommitEditData::fromEntity)
                .peek(c -> {
                    // Add domain names for each commit (if any)
                    List<String> dns = domainNames.get(c.getUuid());
                    if (dns != null && dns.size() > 0) {
                        c.setDomainNames(String.join(", ", dns));
                    }
                })
                .sorted(Comparator.comparing(CommitEditData::getCreatedTime).reversed())
                .collect(Collectors.toList());
    }

    /**
     * <p>Paginated / sorted access on an existing commit index.</p>
     * <p>Not optimized (load, populate, filter and order ALL of index before pagination, on every call !!!)</p>
     * <p>Do NOT combine similar lines</p>
     *
     * @param pageIndex     requested paginated page index
     * @param currentSearch all search (filter / sort) criteria in a combined component
     * @return one page of content, filtered, sorted and paginated
     */
    public DiffContentPage getPaginatedExistingCommitContent(UUID commitUUID, int pageIndex, DiffContentSearch currentSearch) {

        Map<UUID, DictionaryEntrySummary> referencedTables = getReferencedTablesForCommit(commitUUID);

        /* Search paginated content for page, using Specification built from search content */
        Page<IndexEntry> pageContent = this.indexes.findAll(
                currentSearch.toSpecification(referencedTables, commitUUID),
                PageRequest.of(pageIndex, this.detailsDisplayPageSize));

        // Standard paginated display
        return new DiffContentPage(pageContent,
                // We must "re-sort" page after complete which use a grouping by table and destroy the existing sort
                c -> currentSearch.sortDiffContent(
                        referencedTables,
                        completeCommitIndexForProjectDict(c, referencedTables, false)
                )
        );
    }

    /**
     * Update specified commit name
     *
     * @param commitUUID selected commit
     * @param name       new name
     */
    public void renameCommit(UUID commitUUID, String name) {

        // Must exist
        assertCommitExists(commitUUID);

        Commit commit = this.commits.getOne(commitUUID);

        commit.setComment(name);

        this.commits.save(commit);
    }

    /**
     * @param commitUUID       requested commit
     * @param loadIndexContent true if the index content will be also loaded in the CommitDetails
     * @return CommitDetails to display
     */
    public CommitDetails getExistingCommitDetails(UUID commitUUID, boolean loadIndexContent) {

        LOGGER.debug("Request for details on existing commit {}", commitUUID);

        // Must exist
        assertCommitExists(commitUUID);

        // Load details (without the content)
        CommitDetails details = loadIndexContent
                // With full content - for testing, reading ...
                ? CommitDetails.fromEntityAndContent(
                this.commits.getOne(commitUUID),
                loadCommitIndex(commitUUID),
                getReferencedTablesForCommit(commitUUID))
                // No content - just get the size, for paginated navigation
                : CommitDetails.fromEntityWithoutContent(
                this.commits.getOne(commitUUID),
                this.indexes.countByCommitUuid(commitUUID),
                getReferencedTablesForCommit(commitUUID));

        List<Attachment> commitAtt = this.attachments.findByCommit(new Commit(commitUUID));

        // Attachment data if any
        if (commitAtt != null && commitAtt.size() > 0) {

            // Prepare and set for display (not content for now)
            details.setAttachments(commitAtt.stream().map(AttachmentLine::fromEntity).collect(Collectors.toList()));
        }

        // Add support for display if any
        details.setAttachmentDisplaySupport(this.attachProcs.isDisplaySupport());

        return details;
    }

    /**
     * <p>Init a standard CommitDetails for a specified commit entity, without checking any
     * referenced items in local db : can be used from imported commit data</p>
     *
     * @param project      project where the corresponding tables will be loaded
     * @param commitEntity a commit to map as a "Details"
     * @param index        a given index content
     * @return Prepared CommitDetails to display / test
     */
    public CommitDetails getCommitDetailsFromSpecifiedContent(Project project, Commit commitEntity, Collection<IndexEntry> index) {

        // Needs referenced table as summaries, loaded for project
        Map<UUID, DictionaryEntrySummary> referencedTables = this.dictionary.findByDomainProject(project).stream()
                .map(d -> DictionaryEntrySummary.fromEntity(d, "?"))
                .collect(Collectors.toMap(DictionaryEntrySummary::getUuid, s -> s));

        return CommitDetails.fromEntityAndContent(
                commitEntity,
                completeCommitIndexForProjectDict(index, referencedTables, true),
                referencedTables);
    }

    /**
     * Complete the given index for rendering regarding the specified project dictionary
     *
     * @param index            a given index content
     * @param referencedTables currently processing DictionaryEntries mapped to there uuids
     * @param combineSimilars  true if the similar entries must be combined in single lines
     * @return unsorted collection of index entries
     */
    public List<PreparedIndexEntry> completeCommitIndexForProjectDict(
            Collection<IndexEntry> index,
            Map<UUID, DictionaryEntrySummary> referencedTables,
            boolean combineSimilars) {

        // On given index ...
        return index.stream()
                // ... Prepare rendering types (at this point without HR payload) ...
                .map(PreparedIndexEntry::fromExistingEntity)
                // ... With associated dictionaryEntry ...
                .collect(Collectors.groupingBy(PreparedIndexEntry::getDictionaryEntryUuid))
                .entrySet().stream()
                // ... Then complete rendering of index entries, for each dict entry
                .flatMap(e -> this.diffs.prepareDiffForRendering(e.getValue(), combineSimilars).stream())
                .peek(i -> {
                    DictionaryEntrySummary dic = referencedTables.get(i.getDictionaryEntryUuid());
                    if (dic != null) {
                        i.setTableName(dic.getTableName());
                        i.setDomainName(dic.getDomainName());
                    }
                })
                .collect(Collectors.toList());
    }

    /**
     * Load commit details for rendering regarding the specified project dictionary, and the given index content
     *
     * @param commitUuid commit uuid which index will be loaded
     * @return PreparedIndexEntry : populated and completed for display
     */
    public Collection<PreparedIndexEntry> loadCommitIndex(UUID commitUuid) {

        Map<UUID, DictionaryEntrySummary> referencedTables = getReferencedTablesForCommit(commitUuid);

        // Load index and complete details
        return completeCommitIndexForProjectDict(this.indexes.findByCommitUuid(commitUuid), referencedTables, true);
    }

    /**
     * Only last commit can be reverted if not reverted yet and
     * if it is not a reverted commit, check if can revert
     */
    public UUID getRevertCompliantCommit() {

        this.projectService.assertCurrentUserHasSelectedProject();

        Project project = this.projectService.getCurrentSelectedProjectEntity();

        // "Revertable" is last one if not a revert
        return this.commits.findRevertableCommitUuid(project.getUuid());
    }

    /* ############################ COMMIT COMPARE PROCESS ENTRYPOINTS ############################## */

    public CompareState startCompareBetweenCommits(UUID firstCommit, UUID secondCommit) {

        Commit first = this.commits.getOne(firstCommit);
        Commit second = this.commits.getOne(secondCommit);

        // Start comparator (first / second will be sorted anyway)
        return manageStartedCompare(this.commitIndexComparator.startCompareProcessBetweenCommits(first, second));
    }


    /**
     * From a specified list of imported packages, we get the commits, contents and identified dictionary entries
     *
     * @param importFiles
     * @return
     */
    public CompareState startCompareImportedCommits(List<ExportFile> importFiles) {

        // We need commit, indexes and version (only for dictionary read)
        List<CommitPackage> commitPckgs = new ArrayList<>();
        List<IndexEntryPackage> indexPckgs = new ArrayList<>();
        List<VersionPackage> versionPckgs = new ArrayList<>();

        List<SharedPackage<?>> allPackages = importFiles.stream()
                .map(this.exportImportService::importPackages)
                .flatMap(List::stream)
                .collect(Collectors.toList());

        // Check package validity (just need to comply with current datagate version)
        assertImportPackageIsValid(allPackages);

        // Organize all required packages
        allPackages.forEach(p -> {
            if (p.getClass() == CommitPackage.class) {
                commitPckgs.add((CommitPackage) p);
            } else if (p.getClass() == IndexEntryPackage.class) {
                indexPckgs.add((IndexEntryPackage) p);
            } else if (p.getClass() == VersionPackage.class) {
                versionPckgs.add((VersionPackage) p);
            }
        });

        // Load index contents once
        Map<UUID, List<IndexEntry>> entries = indexPckgs.stream().flatMap(IndexEntryPackage::content)
                .collect(Collectors.groupingBy(i -> i.getCommit().getUuid()));

        // Extract commits with their associated index content
        List<Commit> comparedCommits = commitPckgs.stream()
                .flatMap(CommitPackage::content)
                .filter(c -> !c.isRefOnly())
                .peek(c -> {
                    if (c.getIndex().isEmpty()) {
                        c.getIndex().addAll(entries.get(c.getUuid()));
                    }
                }).collect(Collectors.toList());

        // And prepare the dictionary overview from version
        List<DictionaryEntrySummary> dicts = versionPckgs.stream()
                .flatMap(VersionPackage::content)
                .map(VersionContentChangesGenerator::readDict)
                .flatMap(List::stream)
                .map(d -> DictionaryEntrySummary.fromEntity(d, null))
                .collect(Collectors.toList());

        // Start compare and share current result
        return manageStartedCompare(this.commitIndexComparator.startCompareProcessOnImportedCommits(comparedCommits, dicts));
    }

    /**
     * <p>
     * For checked status
     * </p>
     *
     * @return
     */
    public CompareState getCurrentCommitCompareState() {

        CommitCompareResult result = getCurrentCompareResult();

        return result != null
                ? new CompareState(result.getStatus(), result.getPercentDone())
                : new CompareState(CommitCompareStatus.NOT_LAUNCHED, 0);
    }

    /**
     * @return
     */
    public CommitCompareResult getCompletedCurrentCompareResult() {

        // Get and drop from map of current
        CommitCompareResult result = getCurrentCompareResult();

        // Fail if none processed
        if (result == null) {
            throw new ApplicationException(COMMIT_COMPARE_FAILED, "No commit compare found");
        }

        return result;
    }

    public DiffContentPage getPaginatedCompareContent(int pageIndex, DiffContentSearch currentSearch) {

        // Apply pagination on filtered content directly
        return new DiffContentPage(pageIndex, getFilteredCompareContent(currentSearch), this.diffDisplayPageSize);
    }

    private CommitCompareResult getCurrentCompareResult() {

        this.projectService.assertCurrentUserHasSelectedProject();
        Project project = this.projectService.getCurrentSelectedProjectEntity();

        return this.currentCommitCompareResults.get(project.getUuid());
    }

    private List<CommitCompareIndexEntry> getFilteredCompareContent(DiffContentSearch currentSearch) {

        CommitCompareResult result = getCurrentCompareResult();

        // Filter and sort content regarding the specified search
        if (result != null) {

            List<CommitCompareIndexEntry> diffContent = currentSearch.filterAndSortDiffContentInMemory(result);

            // Complete tableName / DomainName to display (only on listed results)
            diffContent.forEach(i -> {
                DictionaryEntrySummary dic = result.getReferencedTables().get(i.getDictionaryEntryUuid());
                if (dic != null) {
                    i.setTableName(dic.getTableName());
                    i.setDomainName(dic.getDomainName());
                }
            });
            return diffContent;
        }
        throw new ApplicationException(COMMIT_COMPARE_NOT_READY, "Cannot get content of current commit compare to filter");
    }

    public List<CommitCompareHistoryEntry> getCompareEntryHistory(UUID dictionaryEntryUuid, String keyValue) {

        CommitCompareResult result = getCurrentCompareResult();

        // Filter and sort content regarding the specified search
        if (result != null) {
            return this.commitIndexComparator.getHistoryForComparedValue(result, dictionaryEntryUuid, keyValue);
        }
        throw new ApplicationException(COMMIT_COMPARE_NOT_READY, "Cannot get content of current commit compare to filter");
    }

    // Keep processing result and prepare compare state
    private CompareState manageStartedCompare(CommitCompareResult result) {

        // If failed immediately, throws
        if (result.getStatus() == CommitCompareStatus.FAILED) {
            throw result.getSourceFailure();
        }

        // Else keep and provide result
        this.currentCommitCompareResults.put(this.projectService.getCurrentSelectedProjectEntity().getUuid(), result);

        return new CompareState(result.getStatus(), result.getPercentDone());
    }

    private void assertCanStartCommitCompare() {

        this.projectService.assertCurrentUserHasSelectedProject();
        Project project = this.projectService.getCurrentSelectedProjectEntity();

        // Cannot start if already running
        if (this.currentCommitCompareResults.containsKey(project.getUuid())) {
            throw new ApplicationException(COMMIT_COMPARE_FAILED,
                    "Cannot start a new compare of commit if one is already running");
        }
    }

    private void checkReferencedPhysicalTablesExistHere(Map<UUID, DictionaryEntry> importedTables, Collection<? extends DiffLine> diff, List<String> errorMessages) {

        // Always check that specified table at least exist physically
        Set<String> existingTableNames = this.metadatas.getTables().stream()
                .map(TableDescription::getName)
                .collect(Collectors.toSet());

        Collection<DictionaryEntry> missings = diff.stream()
                .map(DiffLine::getDictionaryEntryUuid).distinct()
                .map(importedTables::get)
                .filter(n -> !existingTableNames.contains(n.getTableName()))
                .collect(Collectors.toSet());

        if (!missings.isEmpty()) {
            missings.forEach(m -> {
                errorMessages.add("Referenced table exist in dictionary entry \"" +
                        m.getParameterName() + "\" but physical table with name \"" +
                        m.getTableName() + "\" is not yet created locally. Create it in current schema first");
            });
        }
    }

    /* ################################# INTERNAL PROCESS ON COMMITS ################################ */

    /**
     * @param encodedLobHash
     * @return
     */
    byte[] getExistingLobData(String encodedLobHash) {

        String decHash = FormatUtils.decodeAsString(encodedLobHash);

        LOGGER.debug("Request for binary content with hash {}", decHash);

        LobProperty lob = this.lobs.findFirstByHash(decHash);

        return lob.getData();
    }

    /**
     * @param uuid
     * @return
     */
    public byte[] getExistingAttachmentData(UUID uuid) {

        LOGGER.debug("Request for binary content from attachment \"{}\"", uuid);

        return this.attachProcs.display(this.attachments.getOne(uuid));
    }

    /**
     * <p>
     * From the prepared commit, rollback in local managed DB everything which was
     * rejected. Must be used only for local commit preparation : on merge commit, the
     * "ignored" items are not rollbacked : they are simply not run
     * </p>
     */
    void applyExclusionsFromLocalCommit(
            PilotedCommitPreparation<?> prepared, Commit commit) {

        LOGGER.debug("Process preparation of rollback from prepared commit, if any");

        List<DiffLine> rollbacked = prepared
                .streamDiffContentMappedToDictionaryEntryUuid()
                .flatMap(e -> toDiffRollbacks(e.getValue()))
                .collect(Collectors.toList());

        if (rollbacked.size() > 0) {

            LOGGER.info("In current commit preparation, a total of {} rollback entries were identified and are going to be applied",
                    rollbacked.size());

            this.applyDiffService.applyDiff(rollbacked, prepared.getDiffLobs(), commit, ApplyType.ROLLBACK);
        }
    }

    /**
     * <p>
     * Apply the changes from the prepared local diff, and store the commit (including the
     * index content)
     * </p>
     *
     * @param prepared preparation source for commit. Can be a local or a merge commit
     *                 preparation
     * @return created commit uuid
     */
    UUID saveAndApplyPreparedCommit(PilotedCommitPreparation<?> prepared) {

        LOGGER.debug("Process apply and saving of a new commit with state {} into project {}", prepared.getPreparingState(),
                prepared.getProjectUuid());

        // Init commit and immediately save it for id gen and link validity
        final Commit commit = this.commits.save(createCommit(prepared));

        LOGGER.debug("Processing commit {} : commit initialized, preparing index content", commit.getUuid());

        List<IndexEntry> entries = prepared.streamDiffContentMappedToDictionaryEntryUuid()
                .flatMap(l -> this.diffs.splitCombinedSimilar(l.getValue()).stream())
                .filter(PreparedIndexEntry::isSelected)
                .map(PreparedIndexEntry::toEntity)
                .peek(e -> e.setCommit(commit))
                .collect(Collectors.toList());

        LOGGER.info("Prepared index with {} items for new commit {}", entries.size(), commit.getUuid());

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("New commit {} of state {} with comment {} prepared with {} index lines",
                    commit.getUuid(), prepared.getPreparingState(), commit.getComment(), entries.size());
        }

        // Prepare used lobs
        List<LobProperty> newLobs = this.diffs.prepareUsedLobsForIndex(entries, prepared.getDiffLobs());

        LOGGER.info("Start saving {} index items for new commit {}", entries.size(), commit.getUuid());

        // Save index
        this.indexes.saveAll(entries);

        LOGGER.info("Start saving {} lobs items for new commit {}", newLobs.size(), commit.getUuid());

        // Add commit to lobs and save
        newLobs.forEach(l -> l.setCommit(commit));
        this.lobs.saveAll(newLobs);

        // Immediately store commit
        this.commits.save(commit);

        // For revert : keep revert source and
        if (prepared.getPreparingState() == CommitState.REVERT) {
            LOGGER.info("Processing revert commit {} : now apply all {} modifications prepared from source commit",
                    commit.getUuid(), entries.size());

            this.applyDiffService.applyDiff(entries, prepared.getDiffLobs(), commit, ApplyType.REVERT);
            LOGGER.debug("Processing revert commit {} : diff applied with success", commit.getUuid());
        }

        // For merge : apply (will rollback previous steps if error found)
        else if (prepared.getPreparingState() == CommitState.MERGED) {

            LOGGER.info("Processing merge commit {} : now apply all {} modifications prepared from imported values",
                    commit.getUuid(), entries.size());
            this.applyDiffService.applyDiff(entries, prepared.getDiffLobs(), commit, ApplyType.IMPORT);
            LOGGER.debug("Processing merge commit {} : diff applied with success", commit.getUuid());

            // And execute attachments if needed
            if (this.attachProcs.isExecuteSupport()) {
                List<AttachmentLine> runnableAtts = prepared.getCommitData().getAttachments().stream()
                        .filter(a -> a.getType().isRunnable() && a.isExecuted()).collect(Collectors.toList());

                // Process only if some found
                if (runnableAtts.size() > 0) {

                    LOGGER.info("Processing merge commit {} : now run {} executable scripts",
                            commit.getUuid(), runnableAtts.size());

                    User user = new User(getCurrentUser().getLogin());

                    // Run each with identified processor. Processor keep history if
                    // needed
                    runnableAtts.forEach(a -> {
                        AttachmentProcessor proc = this.attachProcs.getFor(a);
                        proc.execute(user, a, commit);
                        LOGGER.debug("Processing merge commit {} : attachements {} executed with success",
                                commit.getUuid(), a.getName());
                    });
                }
            }
        }

        // Update commit attachments
        if (prepared.getCommitData().getAttachments() != null) {
            this.attachments.saveAll(prepareAttachments(prepared.getCommitData().getAttachments(), commit));
        }

        LOGGER.info("Commit {} saved with {} items and {} lobs", commit.getUuid(), entries.size(), newLobs.size());

        return commit.getUuid();
    }

    /**
     * <p>
     * Reserved for launch from <tt>PilotableCommitPreparationService</tt>
     *
     * @param importFile         content to import
     * @param currentPreparation preparation in process, initialized for import
     */
    ExportImportResult<PilotedCommitPreparation<PreparedMergeIndexEntry>> importCommits(
            ExportFile importFile,
            final PilotedCommitPreparation<PreparedMergeIndexEntry> currentPreparation) {

        LOGGER.debug("Asking for an import of commit in piloted preparation context {}", currentPreparation.getIdentifier());

        Project project = this.projectService.getCurrentSelectedProjectEntity();
        Version currentLastVersion = this.versions.getLastVersionForProject(project);

        // Must have at least one local version
        if (currentLastVersion == null) {
            throw new ApplicationException(ErrorType.VERSION_LOCAL_MISSING, "no local version found. Required for compatibility checks at import");
        }

        // #1 Load import
        List<SharedPackage<?>> commitPackages = this.exportImportService.importPackages(importFile);

        // #2 Check package validity
        assertImportPackageIsValid(commitPackages);

        // Get package files - commits
        CommitPackage commitPckg = (CommitPackage) commitPackages.stream().filter(p -> p.getClass() == CommitPackage.class).findFirst()
                .orElseThrow(() -> new ApplicationException(COMMIT_IMPORT_INVALID,
                        "Import of commits doesn't contain the expected package types - can't found commits"));

        // Get package files - index (if new model)
        IndexEntryPackage indexPckg = !commitPckg.isCompatibilityMode() ? (IndexEntryPackage) commitPackages.stream().filter(p -> p.getClass() == IndexEntryPackage.class).findFirst()
                .orElseThrow(() -> new ApplicationException(COMMIT_IMPORT_INVALID,
                        "Import of commits doesn't contain the expected package types - can't found index")) : null;

        // Get package files - lobs
        LobPropertyPackage lobsPckg = (LobPropertyPackage) commitPackages.stream().filter(p -> p.getClass() == LobPropertyPackage.class)
                .findFirst().orElseThrow(() -> new ApplicationException(COMMIT_IMPORT_INVALID,
                        "Import of commits doesn't contain the expected package types - can't found lobs package"));

        // Get package files - attachments
        AttachmentPackage attachsPckg = (AttachmentPackage) commitPackages.stream().filter(p -> p.getClass() == AttachmentPackage.class)
                .findFirst().orElseThrow(() -> new ApplicationException(COMMIT_IMPORT_INVALID,
                        "Import of commits doesn't contain the expected package types - can't found attachment package"));

        // Get package files - versions (used ones)
        VersionPackage versionPckg = (VersionPackage) commitPackages.stream().filter(p -> p.getClass() == VersionPackage.class)
                .findFirst().orElseThrow(() -> new ApplicationException(COMMIT_IMPORT_INVALID,
                        "Import of commits doesn't contain the expected package types - can't found version package"));

        // Get package files - transformers (optional for compatibility with legacy exports) - apply only if present
        commitPackages.stream().filter(p -> p.getClass() == TransformerDefPackage.class)
                .map(p -> (TransformerDefPackage) p).findFirst().ifPresent(p -> currentPreparation.setTransformerProcessor(this.transformerService.importTransformerDefsAndPrepareProcessor(p)));

        LOGGER.debug("Import of commits from package {} initiated", commitPckg);

        // #3 Extract local data to merge with
        Map<UUID, Commit> localCommits = this.commits.findAll().stream()
                .collect(Collectors.toMap(Commit::getUuid, v -> v));
        Map<UUID, Commit> mergedCommits = localCommits.values().stream()
                .flatMap(c -> Associate.onFlatmapOf(c.getMergeSources(), c))
                .collect(Collectors.toMap(Associate::getOne, Associate::getTwo));

        // Need to be sorted by create time when processing old model (new model is always sorted)
        if (commitPckg.isCompatibilityMode()) {
            commitPckg.from(commitPckg.content().sorted(Comparator.comparing(Commit::getCreatedTime)));
        }

        // Prepare referenced version related to commits
        Map<UUID, Version> referencedVersions = versionPckg.content().distinct().collect(Collectors.toMap(Version::getUuid, v -> v));

        // Version checking is a dynamic feature
        boolean checkVersion = this.features.isEnabled(Feature.VALIDATE_VERSION_FOR_IMPORT);

        // Dictionary compatibility control
        boolean checkDictionaryCompatibility = this.features.isEnabled(Feature.CHECK_DICTIONARY_COMPATIBILITY_FOR_IMPORT);

        // Control on referenced commits - allowing to import on missing ref commits
        boolean validateMissingRefCommits = this.features.isEnabled(Feature.VALIDATE_MISSING_REF_COMMITS_FOR_IMPORT);

        // Load identified content
        if (indexPckg != null) {
            // TODO : store in DB when it's possible for less memory use
            indexPckg.content()
                    .map(PreparedMergeIndexEntry::fromImportedEntity)
                    .forEach(currentPreparation.getDiffContent()::add);
        }

        // Prepare a listing of errors (will abort full process if any error exists)
        List<String> errorMessages = new ArrayList<>();

        // Create the future merge commit info
        currentPreparation.setCommitData(new CommitEditData());
        currentPreparation.getCommitData().setMergeSources(new ArrayList<>());
        currentPreparation.getCommitData().setImportedTime(LocalDateTime.now());

        if (currentPreparation.getPreparingState() != CommitState.IMPORTED) {
            currentPreparation.getCommitData().setComment(":twisted_rightwards_arrows: Merging Sources :");
        } else {
            currentPreparation.getCommitData().setComment(":tada: Imported from " + importFile.getFilename());
        }

        final AtomicLong processedCommits = new AtomicLong(0);

        // #4 Process commits, one by one
        commitPckg.content().forEach(imported -> {

            // Check if already stored localy (in "local" or "merged")
            boolean hasItLocaly = localCommits.containsKey(imported.getUuid()) || mergedCommits.containsKey(imported.getUuid());

            // It's a ref : we MUST have it locally (imported as this or merged)
            if (imported.isRefOnly()) {

                // Impossible situation
                if (!hasItLocaly && validateMissingRefCommits) {
                    throw new ApplicationException(COMMIT_IMPORT_MISS_REF,
                            "Imported package is not compliant : the requested ref commit " + imported.getUuid()
                                    + " is not imported yet nore merged in local commit base.", imported.getUuid().toString());
                }

                LOGGER.debug("Imported ref commit {} is already managed in local db. As a valid reference, ignore it", imported.getUuid());

            } else {

                if (hasItLocaly) {
                    LOGGER.debug("Imported commit {} is already managed in local db. Ignore it", imported.getUuid());
                }

                // This one is not yet imported or merged : keep it for processing
                else {
                    // For real import, can check version
                    Version referencedVersion = referencedVersions.get(imported.getVersion().getUuid());

                    // Referenced version must exist locally if feature is enabled
                    if (checkVersion) {
                        checkImportedCommitHasExpectedVersion(imported, referencedVersion, errorMessages);
                    }

                    Map<UUID, DictionaryEntry> importedTables = VersionContentChangesGenerator.readDict(referencedVersion).stream()
                            .collect(Collectors.toMap(DictionaryEntry::getUuid, t -> t));

                    // For compatibility we need to process the referenced version which has the full content included
                    if (checkDictionaryCompatibility) {

                        LOGGER.debug("Check dictionary compatibility for imported commit {}", imported.getUuid());

                        // Need the referenced index -> It's a perf bottleneck ...
                        Collection<? extends DiffLine> referencedIndex = commitPckg.isCompatibilityMode()
                                ? imported.getIndex()
                                : currentPreparation.getDiffContent().stream().filter(i -> i.getCommitUuid().equals(imported.getUuid())).collect(Collectors.toList());

                        checkImportedCommitCompatibilityWithLocalDictionary(imported, referencedIndex, importedTables, referencedVersion, currentLastVersion, errorMessages);
                    }

                    // Ignore anyway if there is an identified error as we will abort import
                    if (errorMessages.isEmpty()) {

                        LOGGER.debug("Imported commit {} is not yet managed in local db. Will process it", imported.getUuid());

                        // Keep it as merge source
                        currentPreparation.getCommitData().getMergeSources().add(imported.getUuid());

                        // On compatibility mode, Init prepared merge with imported index extracted from commit
                        if (commitPckg.isCompatibilityMode()) {
                            imported.getIndex().stream()
                                    .map(PreparedMergeIndexEntry::fromImportedEntity)
                                    .forEach(currentPreparation.getDiffContent()::add);
                        }

                        // Generate comment part for imported commit
                        if (currentPreparation.getPreparingState() != CommitState.IMPORTED && imported.getComment() != null) {
                            currentPreparation.getCommitData().setComment(
                                    currentPreparation.getCommitData().getComment() + "\n * " + imported.getComment()
                            );
                        }

                        // Check if reference tables exist locally (need to check imported index) - only on compliant versions
                        if (referencedVersion.getDictionaryContent() != null) {
                            checkReferencedPhysicalTablesExistHere(importedTables, currentPreparation.getDiffContent(), errorMessages);
                        }

                        // Start range on 1st imported commit
                        if (currentPreparation.getCommitData().getRangeStartTime() == null) {
                            currentPreparation.getCommitData().setRangeStartTime(imported.getCreatedTime());
                            LOGGER.debug("As the imported commit {} is the first missing one, will use it's time {} to identify"
                                    + " local diff to run", imported.getUuid(), imported.getCreatedTime());
                        }
                    }
                }
            }
            processedCommits.incrementAndGet();
        });

        // #5 Abort if any check error exist
        if (errorMessages.size() > 0) {
            throw new ApplicationException(MERGE_DICT_NOT_COMPATIBLE,
                    "Import cannot be processed as some compatibility issues have been identified",
                    String.join(",\n", errorMessages));
        }

        // #5 Get all lobs
        currentPreparation.getDiffLobs().putAll(
                lobsPckg.content()
                        .distinct()
                        .collect(Collectors.toConcurrentMap(LobProperty::getHash, LobProperty::getData)));

        // Add attachment - managed in temporary version first
        currentPreparation.getCommitData().setAttachments(attachsPckg.toAttachmentLines());

        // Remove the already imported attachments
        removeAlreadyImportedAttachments(currentPreparation);

        // Result for direct display (with ref to preparation)
        ExportImportResult<PilotedCommitPreparation<PreparedMergeIndexEntry>> result = new ExportImportResult<>(currentPreparation);

        // Can show number of processing commits
        result.addCount(PCKG_ALL, processedCommits.get(),
                currentPreparation.getCommitData().getMergeSources().size(), 0);

        LOGGER.info("Import of commits from package {} done  : now the merge data is ready with {} source commits", commitPckg,
                processedCommits.get());

        return result;

    }

    /**
     * <p>
     * Simple search for existing attachments (imported by uuid). Remove the ones we
     * already have processed
     * </p>
     *
     * @param prepa
     */
    private void removeAlreadyImportedAttachments(PilotedCommitPreparation<?> prepa) {
        if (prepa.getCommitData().getAttachments() != null) {
            prepa.getCommitData().getAttachments()
                    .removeIf(line -> this.attachments.existsById(line.getUuid()));
        }
    }

    /**
     * @param prepared
     * @return
     */
    private Commit createCommit(PilotedCommitPreparation<?> prepared) {

        Project project = new Project(prepared.getProjectUuid());
        Version version = this.versions.getLastVersionForProject(project);

        LOGGER.debug("Current project version is \"{}\" ({}). Will not check if dictionnary was modified", version.getName(),
                version.getUuid());


        Commit newCommit = CommitEditData.toEntity(prepared.getCommitData());
        newCommit.setCreatedTime(LocalDateTime.now());
        newCommit.setUser(new User(getCurrentUser().getLogin()));
        newCommit.setOriginalUserEmail(getCurrentUser().getEmail());
        newCommit.setState(prepared.getPreparingState());
        newCommit.setProject(project);
        newCommit.setVersion(version);

        // Prepared commit uuid
        UUID commitUUID = UUID.randomUUID();

        // UUID generate (not done by HBM / DB)
        newCommit.setUuid(commitUUID);

        // Init commit
        return this.commits.save(newCommit);
    }

    /**
     * <p>
     * Complete given diff as a one to rollback
     * </p>
     *
     * @param diffContent
     * @return
     */
    private Stream<DiffLine> toDiffRollbacks(Collection<? extends PreparedIndexEntry> diffContent) {

        // Split combined lines and convert to rollbacks
        return diffContent.stream()
                .filter(PreparedIndexEntry::isRollbacked)
                .flatMap(l -> {
                    if (l instanceof SimilarPreparedIndexEntry) {
                        SimilarPreparedIndexEntry combinedDiffLine = (SimilarPreparedIndexEntry) l;
                        return combinedDiffLine.split().stream();
                    } else {
                        return Stream.of(l);
                    }
                })
                .map(this.rollbackConverter::toRollbackLine)
                ;
    }

    /**
     * @param commitUUID
     */
    private void assertCommitExists(UUID commitUUID) {
        if (commitUUID == null || !this.commits.existsById(commitUUID)) {
            throw new ApplicationException(COMMIT_EXISTS, "Specified commit " + commitUUID + " doesn't exist");
        }
    }

    /**
     * @param refCommit
     */
    private void checkImportedCommitHasExpectedVersion(Commit refCommit, Version referencedVersion, List<String> errorMessages) {

        Optional<Version> vers = this.versions.findById(referencedVersion.getUuid());

        if (vers.isEmpty()) {
            errorMessages.add("Referenced version \"" + referencedVersion.getName() + "\" is not managed locally" +
                    " for commit \"" + refCommit.getComment() + "\" (" + refCommit.getUuid() + ")");
        }
    }

    /**
     * @param refCommit
     */
    private void checkImportedCommitCompatibilityWithLocalDictionary(
            Commit refCommit,
            Collection<? extends DiffLine> index,
            Map<UUID, DictionaryEntry> importedTables,
            Version importedVersion,
            Version localLastVersion,
            List<String> errorMessages) {

        // 1 : imported tables already initialized

        // 2 : extract concerned tables and indexes from commit - will process by table (map to table directly)
        Map<DictionaryEntry, Collection<? extends DiffLine>> indexByTables = index.stream()
                .collect(Collectors.groupingBy(DiffLine::getDictionaryEntryUuid)).entrySet().stream()
                .collect(Collectors.toMap(e -> importedTables.get(e.getKey()), Map.Entry::getValue));

        // 3 : run a full change generation using dict content in dictionary - will process only concerned items
        Map<String, VersionCompare.DictionaryTableChanges> allTableChanges = this.changesGenerator.generateChanges(localLastVersion, importedVersion).stream()
                .filter(d -> d.getTableChanges() != null)
                .flatMap(d -> d.getTableChanges().stream())
                .collect(Collectors.toMap(VersionCompare.DictionaryTableChanges::getTableName, c -> c));

        // 4 : Check compatibility by table - check referenced entry, keys and columns (consistent order)
        indexByTables.entrySet().stream()
                // TODO [RISK01] : for multiple dict entries for same table in one project, need to be updated
                .sorted(Comparator.comparing(e -> e.getKey().getTableName()))
                .forEach((e) -> checkCommitIndexDictionaryEntryCompatibility(
                        e.getValue(),
                        e.getKey(),
                        refCommit,
                        allTableChanges,
                        errorMessages
                ));
    }

    private Map<UUID, DictionaryEntrySummary> getReferencedTablesForCommit(UUID commitUUID) {
        return this.dictionary.findAllById(this.dictionary.findUsedUuidsByCommitUuid(commitUUID)).stream()
                .map(d -> DictionaryEntrySummary.fromEntity(d, "?"))
                .collect(Collectors.toMap(DictionaryEntrySummary::getUuid, s -> s));
    }

    /**
     * Internal check for dictionary compatibility for one set of index from a commit, for a specified dictionary entry,
     *
     * @param index                   current index section (for one DictionaryEntry)
     * @param importedDictionaryEntry current imported DictionaryEntry
     * @param refCommit               associated commit
     * @param allTableChanges         all identified changes between commit tables and local tables
     * @param errorMessages           message holder to complete
     */
    private void checkCommitIndexDictionaryEntryCompatibility(
            Collection<? extends DiffLine> index,
            DictionaryEntry importedDictionaryEntry,
            Commit refCommit,
            Map<String, VersionCompare.DictionaryTableChanges> allTableChanges,
            List<String> errorMessages) {

        Optional<DictionaryEntry> localDictionaryEntry = this.dictionary.findById(importedDictionaryEntry.getUuid());

        // DictionaryEntry is not present at all -> Cannot check changes, and cannot import commit
        if (localDictionaryEntry.isEmpty()) {
            LOGGER.error("Incompatibility for import of commit {} \"{}\" : referenced dict entry {} for table \"{}\" doesn't exist locally",
                    refCommit.getUuid(), refCommit.getComment(), importedDictionaryEntry.getUuid(), importedDictionaryEntry.getTableName());
            errorMessages.add("Referenced dictionary table \"" + importedDictionaryEntry.getTableName() + "\" is not managed locally");
        }

        // DictionaryEntry is present : continue to check table / key / column changes
        else {
            String tablename = importedDictionaryEntry.getTableName();
            // TODO [RISK01] : for multiple dict entries for same table in one project, need to be updated
            VersionCompare.DictionaryTableChanges tableChanges = allTableChanges.get(tablename);

            // Check columns adn keys only if the table is identified as changed (= not UNCHANGED here)
            if (tableChanges.getChangeType() != VersionCompare.ChangeType.UNCHANGED) {

                // Detect all column used in index and check that they are unchanged. If an used column is changed, we cannot import commit
                this.diffs.extractIndexEntryValueNames(index).stream().sorted().forEach(colName -> {
                    Optional<VersionCompare.ColumnChanges> columnChanges = tableChanges.getColumnChanges().stream().filter(c -> c.getName().equals(colName)).findFirst();
                    if (columnChanges.isEmpty() || columnChanges.get().getChangeType() != VersionCompare.ChangeType.UNCHANGED) {
                        LOGGER.error("Incompatibility for import of commit {} \"{}\" : column \"{}\" for table \"{}\" is different, cannot process data",
                                refCommit.getUuid(), refCommit.getComment(), colName, tablename);
                        errorMessages.add("Table \"" + tablename + "\" : column \"" + colName + "\" used for commit \""
                                + refCommit.getComment() + "\" has been modified");
                    }
                });

                // If key has changed, we cannot import commit
                if (tableChanges.getColumnChanges().stream().filter(c -> c.isKey() || c.isKeyChange()).anyMatch(c -> c.getChangeType() != VersionCompare.ChangeType.UNCHANGED)) {
                    LOGGER.error("Incompatibility for import of commit {} \"{}\" : key definition for table \"{}\" is different, cannot process data",
                            refCommit.getUuid(), refCommit.getComment(), tablename);
                    errorMessages.add("Table \"" + tablename + "\" : key definition used for commit \""
                            + refCommit.getComment() + "\" has been modified");
                }
            }
        }
    }

    private Stream<Commit> sortedCommits(Project project) {
        return this.commits.findByProject(project).stream().sorted(Comparator.comparing(Commit::getCreatedTime));
    }

    /**
     * @param source
     * @param commit
     * @return
     */
    private static Collection<Attachment> prepareAttachments(Collection<AttachmentLine> source, Commit commit) {
        return source.stream()
                .map(l -> {
                    Attachment at = AttachmentLine.toEntity(l);

                    at.setCommit(commit);

                    // Create uuid if required (new item)
                    if (at.getUuid() == null) {
                        at.setUuid(UUID.randomUUID());
                    }

                    // If selected for executed => Update exec time
                    else if (l.isExecuted()) {
                        at.setExecuteTime(LocalDateTime.now());
                    }

                    return at;
                })
                .collect(Collectors.toList());
    }

    /**
     * Rules for commit package : one commit package + lobs package only
     *
     * @param commitPackages
     */
    private static void assertImportPackageIsValid(List<SharedPackage<?>> commitPackages) {

        boolean commitIndexCompatibilityMode = commitPackages.stream()
                .filter(p -> p instanceof CommitPackage)
                .findFirst()
                .map(p -> ((CommitPackage) p).isCompatibilityMode())
                .orElse(false);

        // On compatibility mode we will have 5 files. Else default mode is 6 packages
        if (commitIndexCompatibilityMode && commitPackages.size() != 5 || commitPackages.size() != 6) {
            throw new ApplicationException(COMMIT_IMPORT_INVALID,
                    "Import of commits can contain only commit package file + lobs package " +
                            "+ attachment package file + a transformer package + last version");
        }

        if (commitPackages.stream().noneMatch(p -> p instanceof CommitPackage
                || p instanceof LobPropertyPackage
                || p instanceof AttachmentPackage
                || p instanceof TransformerDefPackage
                || p instanceof VersionPackage)) {
            throw new ApplicationException(COMMIT_IMPORT_INVALID, "Import of commits doens't contains the expected package types");
        }

        if (!commitIndexCompatibilityMode && commitPackages.stream().noneMatch(p -> p instanceof IndexEntryPackage)) {
            throw new ApplicationException(COMMIT_IMPORT_INVALID, "Import of commits doens't contains the expected package types "
                    + "(new package mode with index file)");
        }
    }

    private String commitExportName(String type, UUID id) {

        ManagedModelDescription modelDesc = this.appDetailsService.getCurrentModelId();

        return String.format("commits-%s-%s%s-%s.par",
                modelDesc != null ? modelDesc.getSchema() : "DB",
                type,
                id != null ? "-" + id.toString() : "",
                FormatUtils.formatForUri(LocalDateTime.now())
        ).replaceAll(" ", "-");
    }
}
