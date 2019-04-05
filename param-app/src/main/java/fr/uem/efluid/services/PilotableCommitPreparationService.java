package fr.uem.efluid.services;

import fr.uem.efluid.model.entities.CommitState;
import fr.uem.efluid.model.entities.DictionaryEntry;
import fr.uem.efluid.model.entities.Project;
import fr.uem.efluid.model.entities.Version;
import fr.uem.efluid.model.repositories.DatabaseDescriptionRepository;
import fr.uem.efluid.model.repositories.DictionaryRepository;
import fr.uem.efluid.model.repositories.VersionRepository;
import fr.uem.efluid.services.types.*;
import fr.uem.efluid.tools.AsyncDriver;
import fr.uem.efluid.tools.AttachmentProcessor;
import fr.uem.efluid.utils.ApplicationException;
import fr.uem.efluid.utils.FormatUtils;
import fr.uem.efluid.utils.SharedOutputInputUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import static fr.uem.efluid.utils.ErrorType.*;

/**
 * <p>
 * Service for Commit preparation, using async execution. <b>Only one execution can be
 * launched (defined as a <tt>PilotedCommitPreparation</tt>)</b>. Commit prepared this way
 * can be of different type (for "local commit" or for "merge commit" after import when a
 * diff exists).
 * </p>
 * <p>
 * Everything associated to commit preparation implies very heavy and very memory
 * consuming processes : that's why this service can only manage ONE preparation at the
 * time (whatever is the kind of the preparation). All preparation are asynchronous : its
 * holder entity <tt>PilotableCommitPreparation</tt> gives details on the status of it.
 * </p>
 *
 * @author elecomte
 * @version 5
 * @since v0.0.1
 */
@Transactional
@Service
public class PilotableCommitPreparationService {

    private static final Logger LOGGER = LoggerFactory.getLogger(PilotableCommitPreparationService.class);

    protected static final String ATTACH_EXT = ".att";

    protected static final String ATTACH_FILE_ID = "attachment";

    @Value("${param-efluid.display.diff-page-size}")
    private int diffDisplayPageSize;

    @Autowired
    private PrepareIndexService diffService;

    @Autowired
    private DictionaryRepository dictionary;

    @Autowired
    private VersionRepository versions;

    @Autowired
    private CommitService commitService;

    @Autowired
    private DatabaseDescriptionRepository managedDesc;

    @Autowired
    private ProjectManagementService projectService;

    @Autowired
    private AttachmentProcessor.Provider attachProcs;

    @Autowired
    private AsyncDriver async;

    // One active only - not a session : JUST 1 FOR ALL APP BY PROJECT
    private Map<UUID, PilotedCommitPreparation<?>> currents = new HashMap<>();

    /**
     * <p>
     * For wizzard, we start a global commit preparation for ALL projects
     * </p>
     *
     * @return
     */
    public WizzardCommitPreparationResult startWizzardLocalCommitPreparation() {

        LOGGER.info("Request for wizzard commit preparation on all projects");

        // Drop all (shouldn't have active preparation)
        this.currents.clear();

        // Prepare on all projects in same time
        return WizzardCommitPreparationResult.fromPreparations(this.projectService.getAllProjects().stream()
                .filter(p -> {
                    // Process only projects with dictionary
                    Collection<DictionaryEntry> dicEntries = this.dictionary.findByDomainProject(new Project(p.getUuid()));
                    return dicEntries != null && dicEntries.size() > 0;
                })
                .map(p -> {
                    // Start preparation for project
                    PilotedCommitPreparation<LocalPreparedDiff> preparation = startLocalPreparation(p.getUuid());

                    LOGGER.info("Request for a new commit preparation in wizzard context - starting preparation"
                            + " {} for project \"{}\"", preparation.getIdentifier(), p.getName());

                    return preparation;
                }).collect(Collectors.toList()));
    }

    /**
     * Start async diff analysis before commit
     */
    public PilotedCommitPreparation<?> startLocalCommitPreparation(boolean force) {

        UUID projectUuid = getActiveProjectUuid();

        // On existing preparation
        if (this.currents.get(projectUuid) != null) {

            // Fail if launched on another type
            if (this.currents.get(projectUuid).getPreparingState() != CommitState.LOCAL) {
                throw new ApplicationException(IMPORT_RUNNING,
                        "An import / Merge process is running. Cannot launch local prepare if merge not completed");
            }

            // Forced restart asked : close current, start a new one
            if (force) {
                LOGGER.info("Request for a new commit preparation - preparation exist already, and "
                        + "force restart asked, so will drop current preparation {}", this.currents.get(projectUuid).getIdentifier());

                // Cancel for any existing reference ...
                this.currents.get(projectUuid).setStatus(PilotedCommitStatus.CANCEL);

                // ... but droping should be enough
                this.currents.remove(projectUuid);
            }

            // Keep current else
            else {

                LOGGER.info("Request for a new commit preparation - preparation already running / "
                        + "available, so use existing {}", this.currents.get(projectUuid).getIdentifier());

                // Default will provides existing if still running
                return getCurrentCommitPreparation();
            }
        }

        // For new commit, check versions
        else {
            assertDictionaryVersionIsOkForProject(projectUuid);
        }

        LOGGER.info("Request for a new commit preparation - start a new one");

        return startLocalPreparation(projectUuid);
    }

    /**
     * <p>
     * For commit preparation, we can add attachment files on last step of commit
     * preparation. This service add ONE attachment file to current commit preparation to
     * store once commit is completed
     * </p>
     * <p>
     * Uses a tmp file and refers it. Data is loaded only at commit completion
     * </p>
     *
     * @param file
     */
    public void addAttachmentOnCurrentCommitPreparation(ExportFile file) {

        PilotedCommitPreparation<?> current = getCurrentCommitPreparation();

        // Must be on existing preparation
        if (current == null || current.getStatus() != PilotedCommitStatus.COMMIT_CAN_PREPARE) {

            // Can process attachment only after diff prepare
            LOGGER.error("Cannot proceed to add attachment if diff is not complete.");
            throw new ApplicationException(PREPARATION_NOT_READY,
                    "Cannot proceed to add attachment if diff is not complete.");
        }

        try {
            // Store attachment into a tmp file
            Path path = SharedOutputInputUtils.initTmpFile(ATTACH_FILE_ID, ATTACH_EXT, true);
            Files.write(path, file.getData());

            // Init on demand attachment list only
            if (current.getCommitData().getAttachments() == null) {
                current.getCommitData().setAttachments(new ArrayList<>());
            }

            // Add attachment tmp info, using tmp file
            current.getCommitData().getAttachments().add(AttachmentLine.fromUpload(file, path));

        } catch (IOException e) {
            throw new ApplicationException(ATTACHMENT_ERROR, "Cannot process attachment file " + file.getFilename(), e);
        }
    }

    /**
     * @param name
     * @return
     */
    public byte[] getAttachmentContentFromCurrentCommitPreparation(String name) {

        PilotedCommitPreparation<?> current = getCurrentCommitPreparation();

        // Must be on existing preparation
        if (current == null || current.getStatus() != PilotedCommitStatus.COMMIT_CAN_PREPARE) {

            // Can process attachment only after diff prepare
            LOGGER.error("Cannot proceed to add attachment if diff is not complete.");
            throw new ApplicationException(PREPARATION_NOT_READY,
                    "Cannot proceed to add attachment if diff is not complete.");
        }

        LOGGER.debug("Request for binary content from attachment \"{}\"", name);

        AttachmentLine line = current.getCommitData().getAttachments().stream()
                .filter(a -> a.getName().equals(name))
                .findFirst().orElseThrow(() -> new ApplicationException(ATTACHMENT_ERROR, "Cannot process attachment file " + name));

        return this.attachProcs.display(line);
    }

    /**
     * @param name
     */
    public void removeAttachmentOnCurrentCommitPreparation(String name) {

        PilotedCommitPreparation<?> current = getCurrentCommitPreparation();

        // Must be on existing preparation
        if (current == null || current.getStatus() != PilotedCommitStatus.COMMIT_CAN_PREPARE) {

            // Can process attachment only after diff prepare
            LOGGER.error("Cannot proceed to add attachment if diff is not complete.");
            throw new ApplicationException(PREPARATION_NOT_READY,
                    "Cannot proceed to add attachment if diff is not complete.");
        }

        current.getCommitData().getAttachments().removeIf(line -> line.getName().equals(name));
    }

    /**
     * Start async diff analysis before commit
     */
    public ExportImportResult<PilotedCommitPreparation<MergePreparedDiff>> startMergeCommitPreparation(ExportFile file) {

        UUID projectUuid = getActiveProjectUuid();

        // On existing preparation
        if (this.currents.get(projectUuid) != null) {

            // Impossible situation
            LOGGER.error("Cannot proced to import entry point for processing merge commit will a preparation is still running.");
            throw new ApplicationException(PREPARATION_CANNOT_START,
                    "Cannot proced to import entry point for processing merge commit will a "
                            + "preparation is still running.");
        }

        LOGGER.info("Request for a new merge commit preparation from an import - start a new one");

        // For CommitState MERGE => Use PreparedMergeIndexEntry (completed in != steps)
        PilotedCommitPreparation<MergePreparedDiff> preparation = new PilotedCommitPreparation<>(CommitState.MERGED);
        preparation.setProjectUuid(projectUuid);

        // Init domain data
        preparation.setDomains(prepareDomainDiffDisplays(preparation.getProjectUuid()));

        // Init feature support for attachments
        setAttachmentFeatureSupports(preparation);

        // First step is NOT async : load the package and identify the appliable index
        ExportImportResult<PilotedCommitPreparation<MergePreparedDiff>> importResult = this.commitService.importCommits(file,
                preparation);

        // Specify as active one
        this.currents.put(projectUuid, preparation);

        this.async.start(preparation, this::processAllMergeDiff);

        return importResult;
    }

    /**
     * @return
     */
    public PilotedCommitPreparation<?> getCurrentCommitPreparation() {
        return this.currents.get(getActiveProjectUuid());
    }

    /**
     * <p>
     * Content of diffDisplay is rendered by paginated page. This method provides 1 page
     * for one diffDisplay content list. A search on key values can be specified (default
     * is null)
     * </p>
     *
     * @param dictUUID
     * @param pageIndex
     * @param search
     * @return
     */
    public DiffDisplayPage getPaginatedDiffDisplay(UUID dictUUID, int pageIndex, String search) {

        PilotedCommitPreparation<?> current = getCurrentCommitPreparation();

        if (current != null) {

            Optional<? extends DiffDisplay<?>> searchDiffDisplay = current.getDomains().stream()
                    .flatMap(d -> d.getPreparedContent().stream())
                    .filter(c -> c.getDictionaryEntryUuid().equals(dictUUID))
                    .findFirst();

            if (searchDiffDisplay.isPresent()) {

                DiffDisplay<?> diffDisplay = searchDiffDisplay.get();

                // If required, filter content
                List<? extends PreparedIndexEntry> diffDisplayContent = search == null
                        ? diffDisplay.getDiff()
                        : diffDisplay.getDiff().stream().filter(i -> i.getKeyValue().contains(search)).collect(Collectors.toList());

                int pageCount = (int) Math.round(Math.ceil((double) diffDisplayContent.size() / this.diffDisplayPageSize));

                List<? extends PreparedIndexEntry> pageContent;

                // No pagination needed
                if (pageCount == 1) {
                    pageContent = diffDisplayContent;
                }

                // Paginated
                else if (pageIndex < pageCount - 1) {
                    pageContent = diffDisplayContent.subList(pageIndex * this.diffDisplayPageSize,
                            (pageIndex + 1) * this.diffDisplayPageSize);
                }

                // Special paginated case : last page
                else {
                    pageContent = diffDisplayContent.subList(pageIndex * this.diffDisplayPageSize, diffDisplayContent.size());
                }

                return new DiffDisplayPage(pageIndex, pageCount, diffDisplayContent.size(), pageContent, search);
            }
        }

        throw new ApplicationException(PREPARATION_NOT_READY, "Cannot get content of current preparation with dict " + dictUUID);
    }

    /**
     * <p>
     * Update one line in preparation, for paginated navigation
     * </p>
     *
     * @param selected
     * @param rollbacked
     * @param indexForDiff
     */
    public void updateDiffLinePreparationSelections(boolean selected, boolean rollbacked, long indexForDiff) {

        getPreparationContentForSelection().streamDiffDisplay()
                .flatMap(d -> d.getDiff().stream())
                .filter(i -> i.getIndexForDiff() == indexForDiff).findFirst()
                .ifPresent(i -> {
                    i.setRollbacked(rollbacked);
                    i.setSelected(selected);
                });
    }

    /**
     * <p>
     * Update all preparation
     * </p>
     *
     * @param selected
     * @param rollbacked
     */
    public void updateAllPreparationSelections(boolean selected, boolean rollbacked) {
        getPreparationContentForSelection().streamDiffDisplay()
                .flatMap(d -> d.getDiff().stream())
                .forEach(i -> {
                    i.setRollbacked(rollbacked);
                    i.setSelected(selected);
                });
    }

    /**
     * <p>
     * Update preparation content for one domain
     * </p>
     *
     * @param selected
     * @param rollbacked
     * @param domainUUID
     */
    public void updateDomainPreparationSelections(boolean selected, boolean rollbacked, UUID domainUUID) {
        getPreparationContentForSelection().getDomains().stream()
                .filter(d -> d.getDomainUuid().equals(domainUUID))
                .flatMap(d -> d.getPreparedContent().stream())
                .flatMap(t -> t.getDiff().stream())
                .forEach(i -> {
                    i.setRollbacked(rollbacked);
                    i.setSelected(selected);
                });
    }

    /**
     * <p>
     * Update preparation content for one diffDisplay (dict page)
     * </p>
     *
     * @param selected
     * @param rollbacked
     * @param dictUUID
     */
    public void updateDiffDisplayPreparationSelections(boolean selected, boolean rollbacked, UUID dictUUID) {
        getPreparationContentForSelection().streamDiffDisplay()
                .filter(d -> d.getDictionaryEntryUuid().equals(dictUUID))
                .flatMap(d -> d.getDiff().stream())
                .forEach(i -> {
                    i.setRollbacked(rollbacked);
                    i.setSelected(selected);
                });
    }

    /**
     * <p>
     * Combines results of all "isNeedAction" for a prepared merge, to check if the merge
     * commit can be applied immediately
     * </p>
     *
     * @return
     */
    @SuppressWarnings("unchecked")
    public boolean isCurrentMergeCommitNeedsAction() {

        PilotedCommitPreparation<?> current = getCurrentCommitPreparation();

        // Works only on ready merge commits
        if (current != null && current.getStatus() == PilotedCommitStatus.COMMIT_CAN_PREPARE
                && current.getPreparingState() == CommitState.MERGED) {

            // Check if at least one content needs action
            boolean result = ((PilotedCommitPreparation<MergePreparedDiff>) current)
                    .isAnyDiffValidate(MergePreparedDiff::isDiffNeedAction);

            LOGGER.debug("Checking if current merge commit needs action. Found {}", Boolean.valueOf(result));

            return result;
        }

        // Other kinds are ignored
        return false;
    }

    /**
     * <p>
     * For checked status
     * </p>
     *
     * @return
     */
    public PilotedCommitStatus getCurrentCommitPreparationStatus() {

        PilotedCommitPreparation<?> preparation = getCurrentCommitPreparation();
        return preparation != null ? preparation.getStatus() : PilotedCommitStatus.NOT_LAUNCHED;
    }

    /**
     * <p>
     * Merged status for all the commit preparation
     * </p>
     *
     * @return
     */
    public PilotedCommitStatus getAllCommitPreparationStatus() {

        // None => Not launched
        if (this.currents.size() == 0) {
            return PilotedCommitStatus.NOT_LAUNCHED;
        }

        // Completion needs all completed
        return this.currents.values().stream().allMatch(p -> p.getStatus() == PilotedCommitStatus.COMMIT_CAN_PREPARE)
                ? PilotedCommitStatus.COMMIT_CAN_PREPARE
                : PilotedCommitStatus.DIFF_RUNNING;
    }

    /**
     * @param encodedLobHash
     * @return
     */
    public byte[] getCurrentOrExistingLobData(String encodedLobHash) {

        PilotedCommitPreparation<?> current = getCurrentCommitPreparation();

        LOGGER.debug("Request for binary content with hash {}", encodedLobHash);

        // 1st : search in current
        if (current != null) {

            String decHash = FormatUtils.decodeAsString(encodedLobHash);
            byte[] data = current.getDiffLobs().get(decHash);

            if (data != null) {
                return data;
            }
        }

        // 2nd : try on existing
        return this.commitService.getExistingLobData(encodedLobHash);
    }

    /**
     * To abort preparation : drops it, then makes service ready for a new one
     */
    public void cancelCommitPreparation() {

        UUID projectUuid = getActiveProjectUuid();

        // For any ref holder : mark canceled and dropped
        PilotedCommitPreparation<?> prep = this.currents.get(projectUuid);

        if (prep != null) {
            prep.setStatus(PilotedCommitStatus.CANCEL);
        }

        // Null = COMPLETED / CANCEL from local service pov
        this.currents.remove(projectUuid);
    }

    /**
     * To complete (validated) preparation : closes and drops it, then makes service ready
     * for a new one
     */
    public void completeCommitPreparation() {

        UUID projectUuid = getActiveProjectUuid();

        // For any ref holder : mark completed
        this.currents.get(projectUuid).setStatus(PilotedCommitStatus.COMPLETED);

        // Null = COMPLETED from local service pov
        this.currents.remove(projectUuid);
    }

    /**
     * <p>
     * The preparation is edited from an external form, but only few values can be edited,
     * and all of its content is already managed localy. So will simply copy "values than
     * can change" into current preparation.
     * </p>
     * <p>
     * Process iteration and copy by domain / table (<tt>DomainDiffDisplay</tt> /
     * <tt>DiffDisplay</tt>)
     * </p>
     *
     * @param changedPreparation
     */
    public <B extends PreparedIndexEntry, A extends DiffDisplay<B>> void copyCommitPreparationSelections(
            PilotedCommitPreparation<A> changedPreparation) {

        // Can be empty if no content was selected (ex : import a commit when everything
        // is already managed locally)
        if (changedPreparation.isHasDiffDisplay()) {

            int endDomain = changedPreparation.getTotalDomainCount();

            LOGGER.debug("Process copy of preparation selection for {} domains", endDomain);

            // Iterate domains
            for (int i = 0; i < endDomain; i++) {

                DomainDiffDisplay<A> changedDomain = changedPreparation.getDomains().get(i);
                DomainDiffDisplay<?> preparedDomain = getCurrentCommitPreparation().getDomains().get(i);

                // Process copy on current domain
                if (changedDomain.getPreparedContent() != null && changedDomain.getPreparedContent().size() > 0) {

                    int endContent = changedDomain.getPreparedContent().size();
                    LOGGER.debug("Processing selection for {} contents in current domain", endContent);

                    // Use basic global iterate on both current and changed, for content
                    for (int j = 0; j < endContent; j++) {

                        DiffDisplay<? extends PreparedIndexEntry> currentDiff = (DiffDisplay<? extends PreparedIndexEntry>) preparedDomain.getPreparedContent().get(j);
                        DiffDisplay<? extends PreparedIndexEntry> changedDiff = changedDomain.getPreparedContent().get(j);

                        if (changedDiff != null && changedDiff.getDiff() != null) {

                            int endDiff = changedDiff.getDiff().size();

                            for (int k = 0; k < endDiff; k++) {

                                PreparedIndexEntry currentEntry = currentDiff.getDiff().get(k);
                                PreparedIndexEntry changedEntry = changedDiff.getDiff().get(k);

                                // Editable values are "selected" and "rollbacked"
                                currentEntry.setRollbacked(changedEntry.isRollbacked());
                                currentEntry.setSelected(changedEntry.isSelected());
                            }
                        }
                    }
                }
            }
        }

        // Case : no identified selection
        else {
            LOGGER.debug("Cannot apply preparation selection on commit as not content was provided : empty commit ?");
        }
    }

    /**
     * <p>
     * The preparation is edited from an external form, but only few values can be edited,
     * and all of its content is already managed localy. So will simply copy "values than
     * can change" into current preparation.
     * </p>
     *
     * @param changedPreparation
     */
    public void copyCommitPreparationCommitData(
            PilotedCommitPreparation<? extends DiffDisplay<? extends PreparedIndexEntry>> changedPreparation) {

        setCommitPreparationCommitData(changedPreparation.getCommitData());
    }

    /**
     * <p>
     * Dedicated process for the wizzard piloted "initial commit" : apply the commit
     * comment, and mark all diff as selected
     * </p>
     *
     * @param comment
     */
    public void finalizeWizzardCommitPreparation(String comment) {

        // Shared process on all preparations
        this.currents.values().stream().forEach(current -> {

            // If not done yet, init for comment apply
            if (current.getCommitData() == null) {
                current.setCommitData(new CommitEditData());
            }

            // Other editable is the commit comment
            current.getCommitData().setComment(comment);

            // Mark all items as selecteds
            current.getDomains().stream()
                    .map(DomainDiffDisplay::getPreparedContent)
                    .flatMap(Collection::stream)
                    .flatMap(d -> d.getDiff().stream())
                    .forEach(i -> i.setSelected(true));
        });
    }

    /**
     * <p>
     * Generic saving process. Declined with fixed type for clean frontend form push
     * </p>
     * <p>
     * Works on current preparation, completed with input "selected / rollbacked items"
     * and commit edit data.
     * </p>
     *
     * @return the created commit UUID
     */
    public WizzardCommitPreparationResult saveWizzardCommitPreparations() {

        WizzardCommitPreparationResult result = WizzardCommitPreparationResult.fromPreparations(this.currents.values().stream()
                .map(current -> {

                    LOGGER.info("Starting saving for preparation in wizzard {}", current.getIdentifier());

                    // Check mandatory comment (checked front side also)
                    if (current.getCommitData().getComment() == null) {
                        throw new ApplicationException(COMMIT_MISS_COMMENT,
                                "Commit preparation cannot be saved without a fixed comment");
                    }

                    // Save update
                    UUID commitUUID = this.commitService.saveAndApplyPreparedCommit(current);

                    // Reset cached diff values, if any, for further uses
                    this.diffService.resetDiffCaches();

                    // For any ref holder : mark completed
                    current.setStatus(PilotedCommitStatus.COMPLETED);

                    LOGGER.info("Saving completed for commit preparation. New commit is {}", commitUUID);

                    return current;
                })
                .collect(Collectors.toList()));

        // Drop preparations
        this.currents.clear();

        return result;
    }

    /**
     * <p>
     * Generic saving process. Declined with fixed type for clean frontend form push
     * </p>
     * <p>
     * Works on current preparation, completed with input "selected / rollbacked items"
     * and commit edit data.
     * </p>
     *
     * @return the created commit UUID
     */
    public UUID saveCommitPreparation() {

        PilotedCommitPreparation<?> current = getCurrentCommitPreparation();

        LOGGER.info("Starting saving for current preparation {}", current.getIdentifier());

        // Check mandatory comment (checked front side also)
        if (current.getCommitData().getComment() == null) {
            throw new ApplicationException(COMMIT_MISS_COMMENT, "Commit preparation cannot be saved without a fixed comment");
        }

        current.setStatus(PilotedCommitStatus.COMMIT_PREPARED);

        // Apply rollbacks on local commits only
        if (current.getPreparingState() == CommitState.LOCAL) {
            this.commitService.applyExclusionsFromLocalCommit(current);
        }

        current.setStatus(PilotedCommitStatus.ROLLBACK_APPLIED);

        // Save update
        UUID commitUUID = this.commitService.saveAndApplyPreparedCommit(current);

        // Reset cached diff values, if any, for further uses
        this.diffService.resetDiffCaches();

        // Drop preparation
        completeCommitPreparation();

        LOGGER.info("Saving completed for commit preparation. New commit is {}", commitUUID);

        return commitUUID;
    }

    /**
     * @return
     */
    private PilotedCommitPreparation<?> getPreparationContentForSelection() {

        PilotedCommitPreparation<?> current = getCurrentCommitPreparation();

        if (current == null) {
            throw new ApplicationException(PREPARATION_NOT_READY, "Cannot get content of current preparation");
        }

        return current;
    }

    /**
     * <p>
     * Apply comment only for completed Commit Data
     * </p>
     *
     * @param data
     */
    private void setCommitPreparationCommitData(CommitEditData data) {

        PilotedCommitPreparation<?> current = getCurrentCommitPreparation();

        // If not done yet, init for comment apply
        if (current.getCommitData() == null) {
            current.setCommitData(new CommitEditData());
        }

        // Other editable is the commit comment
        current.getCommitData().setComment(data.getComment());

        // Works only on edit
        if (current.getCommitData().getAttachments() != null && data.getAttachments() != null
                && current.getCommitData().getAttachments().size() >= data.getAttachments().size()) {

            // Copy only "executed" status which says if the script needs run
            for (int i = 0; i < data.getAttachments().size(); i++) {
                current.getCommitData().getAttachments().get(i).setExecuted(data.getAttachments().get(i).isExecuted());
            }
        }
    }

    /**
     * For CommitState LOCAL => Use PreparedIndexEntry
     *
     * @param projectUuid
     * @return
     */
    private PilotedCommitPreparation<LocalPreparedDiff> startLocalPreparation(UUID projectUuid) {

        // For CommitState LOCAL => Use PreparedIndexEntry
        PilotedCommitPreparation<LocalPreparedDiff> preparation = new PilotedCommitPreparation<>(CommitState.LOCAL);
        preparation.setProjectUuid(projectUuid);

        // Init domain data
        preparation.setDomains(prepareDomainDiffDisplays(preparation.getProjectUuid()));

        // Init feature support for attachments
        setAttachmentFeatureSupports(preparation);

        // Specify as active one
        this.currents.put(projectUuid, preparation);

        this.async.start(preparation, this::processAllDiff);

        return preparation;
    }

    /**
     * <p>
     * Assert also that a project is specified, so result cannot be null. Will throw an
     * exception if user has not selected a project
     * </p>
     *
     * @return
     */
    private UUID getActiveProjectUuid() {
        this.projectService.assertCurrentUserHasSelectedProject();
        return this.projectService.getCurrentSelectedProject().getUuid();
    }

    /**
     * <p>
     * Asynchronous task which is itself a process of asynchronous execution of managed
     * table diffs (one task for each managed table). Similar to a "git status"
     * </p>
     * <p>
     * Use parallele processes, but not asyncronous by itself : can be launched as a
     * CompletableFuture in call processes
     * </p>
     */
    private void processAllDiff(PilotedCommitPreparation<LocalPreparedDiff> preparation) {

        LOGGER.info("Begin diff process on commit preparation {}", preparation.getIdentifier());

        try {
            long startTimeout = System.currentTimeMillis();

            List<DictionaryEntry> dictEntries = this.dictionary.findByDomainProject(new Project(preparation.getProjectUuid()));

            // Process details
            List<Callable<LocalPreparedDiff>> callables = dictEntries
                    .stream()
                    .map(d -> callDiff(d, preparation))
                    .collect(Collectors.toList());

            preparation.setProcessStarted(callables.size());
            preparation.setProcessRemaining(new AtomicInteger(callables.size()));

            // Init lobs holder (concurrent)
            preparation.setDiffLobs(new ConcurrentHashMap<>());

            LOGGER.info("Diff LOCAL process starting with {} diff to run", callables.size());

            List<LocalPreparedDiff> fullDiff = this.async.processSteps(callables, preparation);

            // Keep in preparation for commit build
            preparation.applyDiffDisplayContent(fullDiff);

            // Mark preparation as completed
            preparation.setEnd(LocalDateTime.now());
            preparation.setStatus(PilotedCommitStatus.COMMIT_CAN_PREPARE);

            LOGGER.info("Diff process completed on commit preparation {}. Found {} index entries. Total process duration was {} ms",
                    preparation.getIdentifier(),
                    fullDiff.size(),
                    System.currentTimeMillis() - startTimeout);

        } catch (ApplicationException a) {
            LOGGER.error("Identified Local process error. Sharing", a);
            throw a;
        } catch (Throwable e) {
            LOGGER.error("Error will processing diff", e);
            preparation.fail(new ApplicationException(PREPARATION_INTERRUPTED, "Interrupted process", e));
        }
    }

    /**
     * <p>
     * Asynchronous task which is itself a process of asynchronous execution of managed
     * table diffs (one task for each managed table). Similar to a "git status"
     * </p>
     * <p>
     * Use parallele processes, but not asyncronous by itself : can be launched as a
     * CompletableFuture in call processes
     * </p>
     */
    private void processAllMergeDiff(PilotedCommitPreparation<MergePreparedDiff> preparation) {

        LOGGER.info("Begin diff process on merge-commit preparation {}", preparation.getIdentifier());

        try {
            long startTimeout = System.currentTimeMillis();

            if (preparation.isHasDiffDisplay()) {

                Map<UUID, DictionaryEntry> dictByUuid = this.dictionary.findAllMappedByUuid(new Project(preparation.getProjectUuid()));

                long searchTimestamp = preparation.getCommitData().getRangeStartTime().atZone(ZoneId.systemDefault()).toEpochSecond();

                // Process details
                List<Callable<MergePreparedDiff>> callables = preparation.streamDiffDisplay()
                        .filter(d -> d != null)
                        .map(p -> callMergeDiff(dictByUuid.get(p.getDictionaryEntryUuid()), searchTimestamp, p, preparation))
                        .collect(Collectors.toList());
                preparation.setProcessStarted(callables.size());
                preparation.setProcessRemaining(new AtomicInteger(callables.size()));

                LOGGER.info("Diff MERGE process starting with {} diff to run", Integer.valueOf(callables.size()));

                // Run all callables in parallel exec
                List<MergePreparedDiff> fullDiff = this.async.processSteps(callables, preparation);

                // Reset content in preparation for commit build, once completed
                preparation.resetDiffDisplayContent();
                preparation.applyDiffDisplayContent(fullDiff);

                // Mark preparation as completed
                preparation.setEnd(LocalDateTime.now());
                preparation.setStatus(PilotedCommitStatus.COMMIT_CAN_PREPARE);

                LOGGER.info(
                        "Diff process completed on merge commit preparation {}. Found {} index entries. Total process duration was {} ms",
                        preparation.getIdentifier(),
                        Integer.valueOf(fullDiff.size()),
                        Long.valueOf(System.currentTimeMillis() - startTimeout));
            } else {
                LOGGER.info("Import found no differences. No merge to run");
                preparation.setEnd(LocalDateTime.now());
                preparation.setStatus(PilotedCommitStatus.COMPLETED);
            }

        } catch (ApplicationException a) {
            LOGGER.error("Identified Merge process error. Sharing", a);
            throw a;
        } catch (Throwable e) {
            LOGGER.error("Error will processing diff", e);
            preparation.fail(new ApplicationException(PREPARATION_INTERRUPTED, "Interrupted process", e));
        }
    }

    /**
     * <p>
     * Add details on features related to attachment management : enable / disable access
     * to feature regarding the spec of current AttachmentProcessor.Provider
     * </p>
     *
     * @param prep
     */
    private void setAttachmentFeatureSupports(PilotedCommitPreparation<?> prep) {

        // Support display if enabled in support items
        prep.setAttachmentDisplaySupport(this.attachProcs.isDisplaySupport());

        // Can execute only on merge
        if (prep.getPreparingState() == CommitState.MERGED) {

            // And if configured for support
            prep.setAttachmentExecuteSupport(this.attachProcs.isExecuteSupport());
        }
    }

    /**
     * <p>
     * Execution for one table, as a <tt>Callable</tt>, for a basic local diff.
     * </p>
     *
     * @param dict
     * @return
     */
    private Callable<LocalPreparedDiff> callDiff(DictionaryEntry dict, final PilotedCommitPreparation<?> current) {

        return () -> {
            // Controle if table not yet specified
            assertDictionaryEntryIsRealTable(dict, current);

            // Init table diff
            LocalPreparedDiff tableDiff = LocalPreparedDiff.initFromDictionaryEntry(dict);

            // Complete dictionary entry
            tableDiff.completeFromEntity(dict);

            // Search diff content
            this.diffService.completeCurrentContentDiff(tableDiff, dict, current.getDiffLobs(), new Project(current.getProjectUuid()));

            int rem = current.getProcessRemaining().decrementAndGet();
            LOGGER.info("Completed 1 local Diff. Remaining : {} / {}", Integer.valueOf(rem),
                    Integer.valueOf(current.getProcessStarted()));

            return tableDiff;
        };
    }

    /**
     * <p>
     * Execution for one table, as a <tt>Callable</tt>, for a merge process diff. The list
     * of diff in <tt>MergePreparedDiff</tt> is regenerated, and DictionaryEntry data are
     * completed.
     *
     * @param dict
     * @param lastLocalCommitTimestamp
     * @param correspondingDiff
     * @return
     */
    private final Callable<MergePreparedDiff> callMergeDiff(
            DictionaryEntry dict,
            long lastLocalCommitTimestamp,
            MergePreparedDiff correspondingDiff,
            final PilotedCommitPreparation<?> current) {

        return () -> {
            // Controle if table not yet specified
            assertDictionaryEntryIsRealTable(dict, current);

            // Complete dictionary entry
            correspondingDiff.completeFromEntity(dict);

            // Then run one merge action for dictionaryEntry
            this.diffService.completeMergeIndexDiff(correspondingDiff, dict, current.getDiffLobs(), lastLocalCommitTimestamp,
                    correspondingDiff.getDiff(), new Project(current.getProjectUuid()));

            int rem = current.getProcessRemaining().decrementAndGet();
            LOGGER.info("Completed 1 merge Diff. Remaining : {} / {}", Integer.valueOf(rem),
                    Integer.valueOf(current.getProcessStarted()));

            // For chained process
            return correspondingDiff;
        };
    }

    /**
     * @param entry
     */
    private void assertDictionaryEntryIsRealTable(DictionaryEntry entry, final PilotedCommitPreparation<?> current) {

        if (entry == null) {
            current.fail(new ApplicationException(TABLE_WRONG_REF, "Specified table entry is missing in managed DB"));
        } else if (!this.managedDesc.isTableExists(entry.getTableName())) {
            current.fail(new ApplicationException(TABLE_NAME_INVALID, "For dict entry " + entry.getUuid() + " the table name \""
                    + entry.getTableName() + "\" is not a valid one in managed DB", entry.getTableName()));
        }
    }

    /**
     * <p>
     * Check that required version details for project dictionary are specified and valid.
     * Version is associated to commit definition, so a valid and up-to-date version is
     * required *
     * </p>
     *
     * @param projectUuid
     */
    private void assertDictionaryVersionIsOkForProject(UUID projectUuid) {

        Project project = new Project(projectUuid);

        Version last = this.versions.getLastVersionForProject(project);

        // At least one version
        if (last == null) {
            throw new ApplicationException(VERSION_NOT_EXIST,
                    "Project " + projectUuid + " has no specified version. Commit cannot be created");
        }

        if (this.versions.hasDictionaryUpdatesAfterLastVersionForProject(project)) {
            throw new ApplicationException(VERSION_NOT_UP_TO_DATE, "Project " + projectUuid + " has a version (" + last.getName()
                    + ") not up-to-date with last updates. Commit cannot be created", last.getName());
        }
    }

    /**
     * <p>
     * Prepare the Domain organized diffsplay for diff result content.
     * </p>
     * <p>
     * Now the diffDisplays are managed in DomainDiffDisplay (one for each functional
     * Domain), but the diffDisplay process is not processed domain after domain but table
     * after table, so we have a flat list of table associated DiffDisplay, and we need to
     * apply them in their corresponding Domain diff display
     * </p>
     *
     * @param projectUuid
     * @return
     */
    private <T extends DiffDisplay<?>> List<DomainDiffDisplay<T>> prepareDomainDiffDisplays(UUID projectUuid) {

        return this.dictionary.findByDomainProject(new Project(projectUuid)).stream()
                .map(DictionaryEntry::getDomain)
                .distinct()
                .map(d -> {
                    DomainDiffDisplay<T> domainDiffDisplay = new DomainDiffDisplay<>();
                    domainDiffDisplay.completeFromEntity(d);
                    return domainDiffDisplay;
                }).collect(Collectors.toList());
    }

}
