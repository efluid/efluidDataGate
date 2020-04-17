package fr.uem.efluid.services;

import fr.uem.efluid.model.DiffLine;
import fr.uem.efluid.model.entities.*;
import fr.uem.efluid.model.metas.ManagedModelDescription;
import fr.uem.efluid.model.repositories.*;
import fr.uem.efluid.services.types.*;
import fr.uem.efluid.tools.AttachmentProcessor;
import fr.uem.efluid.utils.ApplicationException;
import fr.uem.efluid.utils.Associate;
import fr.uem.efluid.utils.FormatUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
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
 * @version 2
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

    private static final String PCKG_CHERRY_PICK = "commits-cherry-pick";

    private static final Logger LOGGER = LoggerFactory.getLogger(CommitService.class);

    @Value("${datagate-efluid.display.details-index-max}")
    private long maxDisplayDetails;

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
        preparation.setSpecificTransformerConfigurations(this.transformerService.getAllTransformerDefConfigs());

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
            List<Commit> allCommits = sortedCommits(project);

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
        if (editData.getSpecificTransformerConfigurations() != null) {
            editData.getSpecificTransformerConfigurations().forEach((k, v) -> {
                ExportTransformer et = new ExportTransformer();
                et.setConfiguration(v);
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

        List<Commit> commitsToExport = sortedCommits(project);

        // If start from first, it's an "ALL"
        String pckgName = PCKG_ALL;

        // Do not start with the first = it's a partial export
        if (!preparedExport.getStartCommit().equals(commitsToExport.get(0))) {

            // Single selection while it's not the last one, mark it as a partial
            if (preparedExport.getStartCommit().equals(preparedExport.getEndCommit())) {
                pckgName = PCKG_CHERRY_PICK;
            } else {
                pckgName = PCKG_AFTER;
            }
        }

        LOGGER.info("Prepare partial commit export. Will use ref only for all commits BEFORE {} into project {}",
                preparedExport.getStartCommit(), project.getName());

        LocalDateTime rangeEnd = preparedExport.getEndCommit().getCreatedTime();
        LocalDateTime rangeBegin = preparedExport.getStartCommit().getCreatedTime();

        // Remove the ones after selected
        commitsToExport = commitsToExport.stream()
                .filter(c -> !c.getCreatedTime().isAfter(rangeEnd))
                .collect(Collectors.toList());

        // And mark previous ones as "ref only"
        commitsToExport.stream()
                .filter(c -> c.getCreatedTime().isBefore(rangeBegin))
                .forEach(Commit::setAsRefOnly);

        // Get associated lobs
        List<LobProperty> lobsToExport = loadLobsForCommits(commitsToExport);

        // Then export :
        ExportFile file = this.exportImportService.exportPackages(Arrays.asList(
                new TransformerDefPackage(PCKG_TRANSFORMERS, LocalDateTime.now())
                        .initWithContent(this.transformerService.getCustomizedTransformerDef(project, preparedExport.getTransformers())),
                new CommitPackage(pckgName, LocalDateTime.now()).initWithContent(commitsToExport),
                new LobPropertyPackage(PCKG_LOBS, LocalDateTime.now()).initWithContent(lobsToExport),
                new AttachmentPackage(PCKG_ATTACHS, LocalDateTime.now())
                        .initWithContent(this.attachments.findByCommitIn(commitsToExport))));

        ExportImportResult<ExportFile> result = new ExportImportResult<>(file);

        // Update count is the "ref only" count
        long refOnly = commitsToExport.stream().filter(Commit::isRefOnly).count();
        result.addCount(pckgName, commitsToExport.size() - refOnly, refOnly, 0);

        LOGGER.info("Export package for commit is ready. {} total commits exported for project \"{}\", "
                        + "uncluding {} exported as ref only. File size is {}b",
                commitsToExport.size(), project.getName(), refOnly, file.getSize());

        // Mark as completed
        preparedExport.setDownloadedTime(LocalDateTime.now());
        this.exports.save(preparedExport);

        // Result is for display / File load
        return result;
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
                .sorted(Comparator.comparing(CommitEditData::getCreatedTime))
                .collect(Collectors.toList());
    }

    /**
     * @param commitUUID
     * @return
     */
    public CommitDetails getExistingCommitDetails(UUID commitUUID) {

        this.projectService.assertCurrentUserHasSelectedProject();
        Project project = this.projectService.getCurrentSelectedProjectEntity();

        LOGGER.debug("Request for details on existing commit {}", commitUUID);

        // Must exist
        assertCommitExists(commitUUID);

        // Load details
        CommitDetails details = CommitDetails.fromEntity(this.commits.getOne(commitUUID));

        long size = this.indexes.countByCommitUuid(commitUUID);

        // Check index size for commit
        if (size < this.maxDisplayDetails) {
            completeCommitDetailsWithIndexForProjectDict(project, details, this.indexes.findByCommitUuid(commitUUID));
        }

        // Too much data, get only dictionary item listings
        else {
            details.setTooMuchData(true);
        }

        details.setSize(size);

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
     * Complete commit details for rendering regarding the specified project dictionary, and the given index content
     *
     * @param project
     * @param details
     * @param index
     */
    public void completeCommitDetailsWithIndexForProjectDict(Project project, CommitDetails details, List<IndexEntry> index) {

        Map<UUID, DictionaryEntry> mappedDict = this.dictionary.findAllMappedByUuid(project);

        // Load commit index
        CommitDetails.completeIndex(details, index);

        // Need to complete DictEnty + HRPayload for index entries
        details.getContent().forEach(d -> {
            DictionaryEntry dict = mappedDict.get(d.getDictionaryEntryUuid());
            d.completeFromEntity(dict);
            // Update for rendering
            d.setDiff(this.diffs.prepareDiffForRendering(dict, d.getDiff()));
        });
    }


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
            PilotedCommitPreparation<? extends DiffDisplay<? extends PreparedIndexEntry>> prepared) {

        LOGGER.debug("Process preparation of rollback from prepared commit, if any");

        List<RollbackLine> rollbacked = prepared.streamDiffDisplay().flatMap(this::streamDiffRollbacks)
                .collect(Collectors.toList());

        if (rollbacked.size() > 0) {

            LOGGER.info("In current commit preparation, a total of {} rollback entries were identified and are going to be applied",
                    rollbacked.size());

            this.applyDiffService.rollbackDiff(rollbacked, prepared.getDiffLobs());
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
    <A extends DiffDisplay<? extends PreparedIndexEntry>> UUID saveAndApplyPreparedCommit(
            PilotedCommitPreparation<A> prepared) {

        LOGGER.debug("Process apply and saving of a new commit with state {} into project {}", prepared.getPreparingState(),
                prepared.getProjectUuid());

        // Init commit
        final Commit commit = createCommit(prepared);

        LOGGER.debug("Processing commit {} : commit initialized, preparing index content", commit.getUuid());

        List<IndexEntry> entries = prepared.streamDiffDisplay()
                .flatMap(l -> this.diffs.splitCombinedSimilar(l.getDiff()).stream())
                .filter(PreparedIndexEntry::isSelected)
                .map(PreparedIndexEntry::toEntity)
                .peek(e -> e.setCommit(commit))
                .collect(Collectors.toList());

        LOGGER.info("Prepared index with {} items for new commit {}", entries.size(), commit.getUuid());

        LOGGER.debug("New commit {} of state {} with comment {} prepared with {} index lines",
                commit.getUuid(), prepared.getPreparingState(), commit.getComment(), entries.size());

        // Prepare used lobs
        List<LobProperty> newLobs = this.diffs.prepareUsedLobsForIndex(entries, prepared.getDiffLobs());

        LOGGER.info("Start saving {} index items for new commit {}", entries.size(), commit.getUuid());

        // Save index and set back to commit with bi-directional link
        commit.setIndex(this.indexes.saveAll(entries));

        LOGGER.info("Start saving {} lobs items for new commit {}", newLobs.size(), commit.getUuid());

        // Add commit to lobs and save
        newLobs.forEach(l -> l.setCommit(commit));
        this.lobs.saveAll(newLobs);

        // Updated commit link
        this.commits.save(commit);

        // For merge : apply (will rollback previous steps if error found)
        if (prepared.getPreparingState() == CommitState.MERGED) {
            LOGGER.info("Processing merge commit {} : now apply all {} modifications prepared from imported values",
                    commit.getUuid(), entries.size());
            this.applyDiffService.applyDiff(entries, prepared.getDiffLobs());
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
                        proc.execute(user, a);
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
     * @param importFile
     */
    ExportImportResult<PilotedCommitPreparation<MergePreparedDiff>> importCommits(
            ExportFile importFile,
            PilotedCommitPreparation<MergePreparedDiff> currentPreparation) {

        LOGGER.debug("Asking for an import of commit in piloted preparation context {}", currentPreparation.getIdentifier());

        // #1 Load import
        List<SharedPackage<?>> commitPackages = this.exportImportService.importPackages(importFile);

        // #2 Check package validity
        assertImportPackageIsValid(commitPackages);

        // Get package files - commits
        CommitPackage commitPckg = (CommitPackage) commitPackages.stream().filter(p -> p.getClass() == CommitPackage.class).findFirst()
                .orElseThrow(() -> new ApplicationException(COMMIT_IMPORT_INVALID,
                        "Import of commits doens't contains the expected package types"));

        // Get package files - lobs
        LobPropertyPackage lobsPckg = (LobPropertyPackage) commitPackages.stream().filter(p -> p.getClass() == LobPropertyPackage.class)
                .findFirst().orElseThrow(() -> new ApplicationException(COMMIT_IMPORT_INVALID,
                        "Import of commits doens't contains the expected package types"));

        // Get package files - attachments
        AttachmentPackage attachsPckg = (AttachmentPackage) commitPackages.stream().filter(p -> p.getClass() == AttachmentPackage.class)
                .findFirst().orElseThrow(() -> new ApplicationException(COMMIT_IMPORT_INVALID,
                        "Import of commits doens't contains the expected package types"));

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

        List<Commit> toProcess = new ArrayList<>();
        LocalDateTime timeProcessStart = null;

        // Need to be sorted by create time
        commitPckg.getContent().sort(Comparator.comparing(Commit::getCreatedTime));

        // Version checking is a dynamic feature
        boolean checkVersion = this.features.isEnabled(Feature.VALIDATE_VERSION_FOR_IMPORT);

        // #4 Process commits, one by one
        for (Commit imported : commitPckg.getContent()) {

            // Referenced version must exist locally if feature enable
            if (checkVersion) {
                assertImportedCommitHasExpectedVersion(imported);
            }

            // Check if already stored localy (in "local" or "merged")
            boolean hasItLocaly = localCommits.containsKey(imported.getUuid()) || mergedCommits.containsKey(imported.getUuid());

            // It's a ref : we MUST have it locally (imported as this or merged)
            if (imported.isRefOnly()) {

                // Impossible situation
                if (!hasItLocaly) {
                    throw new ApplicationException(COMMIT_IMPORT_INVALID,
                            "Imported package is not compliant : the requested ref commit " + imported.getUuid()
                                    + " is not imported yet nore merged in local commit base.");
                }

                LOGGER.debug("Imported ref commit {} is already managed in local db. As a valid reference, ignore it", imported.getUuid());

            } else {

                if (hasItLocaly) {
                    LOGGER.debug("Imported commit {} is already managed in local db. Ignore it", imported.getUuid());
                }

                // This one is not yet imported or merged : keep it for processing
                else {
                    LOGGER.debug("Imported commit {} is not yet managed in local db. Will process it", imported.getUuid());

                    toProcess.add(imported);

                    // Start time for local diff search
                    if (timeProcessStart == null) {
                        timeProcessStart = imported.getCreatedTime();
                        LOGGER.debug("As the imported commit {} is the first missing one, will use it's time {} to identify"
                                + " local diff to run", imported.getUuid(), timeProcessStart);
                    }
                }
            }
        }

        // #5 Get all lobs
        currentPreparation.setDiffLobs(
                lobsPckg.getContent().stream()
                        .distinct()
                        .collect(Collectors.toConcurrentMap(LobProperty::getHash, LobProperty::getData)));

        // Create the future merge commit info
        currentPreparation.setCommitData(new CommitEditData());
        currentPreparation.getCommitData().setMergeSources(toProcess.stream().map(Commit::getUuid).collect(Collectors.toList()));
        currentPreparation.getCommitData().setRangeStartTime(timeProcessStart);
        currentPreparation.getCommitData().setImportedTime(LocalDateTime.now());
        currentPreparation.getCommitData().setComment(generateMergeCommitComment(toProcess));

        // Add attachment - managed in temporary version first
        currentPreparation.getCommitData().setAttachments(attachsPckg.toAttachmentLines());

        // Remove the already imported attachments
        removeAlreadyImportedAttachments(currentPreparation);

        // Init prepared merge with imported index
        currentPreparation.applyDiffDisplayContent(importedCommitIndexes(toProcess));

        // Result for direct display (with ref to preparation)
        ExportImportResult<PilotedCommitPreparation<MergePreparedDiff>> result = new ExportImportResult<>(currentPreparation);

        // Can show number of processing commits
        result.addCount(PCKG_ALL, commitPckg.getContent().size(), toProcess.size(), 0);

        LOGGER.info("Import of commits from package {} done  : now the merge data is ready with {} source commits", commitPckg,
                commitPckg.getContent().size());

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
     * @param entry
     * @param diffContent
     * @return
     */
    private List<RollbackLine> getDiffRollbacks(DictionaryEntry entry, Collection<? extends DiffLine> diffContent) {

        // All "previous" for current diff
        Map<String, IndexEntry> previouses = this.indexes.findAllPreviousIndexEntries(entry,
                diffContent.stream().map(DiffLine::getKeyValue).collect(Collectors.toList()));

        // Completed rollback
        return diffContent.stream()
                .map(current -> new RollbackLine(current, previouses.get(current.getKeyValue())))
                .collect(Collectors.toList());
    }

    /**
     * @param commitsToExport
     * @return
     */
    private List<LobProperty> loadLobsForCommits(List<Commit> commitsToExport) {
        return this.lobs
                .findByCommitUuidIn(commitsToExport.stream().filter(c -> !c.isRefOnly()).map(Commit::getUuid).collect(Collectors.toList()));
    }

    /**
     * <p>
     * Get rollback specified in one DiffDisplay
     * </p>
     *
     * @param diff
     * @return
     */
    private Stream<RollbackLine> streamDiffRollbacks(DiffDisplay<? extends PreparedIndexEntry> diff) {

        LOGGER.debug("Process identification of rollback on dictionaryEntry {}, if any", diff.getDictionaryEntryUuid());

        return getDiffRollbacks(new DictionaryEntry(diff.getDictionaryEntryUuid()),
                diff.getDiff().stream().filter(PreparedIndexEntry::isRollbacked).collect(Collectors.toList()))
                .stream();
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
    private void assertImportedCommitHasExpectedVersion(Commit refCommit) {

        Optional<Version> vers = this.versions.findById(refCommit.getVersion().getUuid());

        if (!vers.isPresent()) {
            throw new ApplicationException(VERSION_NOT_IMPORTED,
                    "Referenced version " + refCommit.getVersion().getUuid() + " is not managed locally");
        }
    }

    private List<Commit> sortedCommits(Project project) {
        return this.commits.findByProject(project).stream().sorted(Comparator.comparing(Commit::getCreatedTime)).collect(Collectors.toList());
    }

    /**
     * @param importedSources
     * @return
     */
    private Collection<MergePreparedDiff> importedCommitIndexes(List<Commit> importedSources) {

        Map<UUID, MergePreparedDiff> groupedByDicEntry = new HashMap<>();

        if (importedSources != null) {

            for (Commit commit : importedSources) {

                for (IndexEntry indexEntry : commit.getIndex()) {

                    MergePreparedDiff diff = groupedByDicEntry.get(indexEntry.getDictionaryEntryUuid());

                    if (diff == null) {
                        DictionaryEntry dicEntry = this.dictionary.getOne(indexEntry.getDictionaryEntryUuid());
                        diff = new MergePreparedDiff(dicEntry.getUuid(), dicEntry.getDomain().getUuid(), new ArrayList<>());
                        groupedByDicEntry.put(indexEntry.getDictionaryEntryUuid(), diff);
                    }

                    diff.getDiff().add(PreparedMergeIndexEntry.fromImportedEntity(indexEntry));
                }
            }
        }

        return groupedByDicEntry.values();
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
     * @param sources
     * @return
     */
    private static String generateMergeCommitComment(List<Commit> sources) {

        return ":twisted_rightwards_arrows: Merging Sources :\n * " + sources.stream().filter(c -> c.getComment() != null).map(Commit::getComment).collect(Collectors.joining("\n * "));
    }

    /**
     * Rules for commit package : one commit package + lobs package only
     *
     * @param commitPackages
     */
    private static void assertImportPackageIsValid(List<SharedPackage<?>> commitPackages) {
        if (commitPackages.size() != 4) {
            throw new ApplicationException(COMMIT_IMPORT_INVALID,
                    "Import of commits can contain only commit package file + lobs package + attachment package file + a transformer package");
        }

        if (commitPackages.stream().noneMatch(p -> p instanceof CommitPackage
                || p instanceof LobPropertyPackage
                || p instanceof AttachmentPackage
                || p instanceof TransformerDefPackage)) {
            throw new ApplicationException(COMMIT_IMPORT_INVALID, "Import of commits doens't contains the expected package types");
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
