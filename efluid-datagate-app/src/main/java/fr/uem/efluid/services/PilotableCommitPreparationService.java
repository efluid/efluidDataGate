package fr.uem.efluid.services;

import fr.uem.efluid.model.entities.*;
import fr.uem.efluid.model.repositories.*;
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
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiFunction;
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

    private static final String ATTACH_EXT = ".att";

    private static final String ATTACH_FILE_ID = "attachment";

    @Value("${datagate-efluid.display.diff-page-size}")
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
    private LobPropertyRepository lobs;

    @Autowired
    private DatabaseDescriptionRepository managedDesc;

    @Autowired
    private ProjectManagementService projectService;

    @Autowired
    private AttachmentProcessor.Provider attachProcs;

    @Autowired
    private AsyncDriver async;

    @Autowired(required = false)
    private PreparationUpdater updater;

    // One active only - not a session : JUST 1 FOR ALL APP BY PROJECT
    private final Map<UUID, PilotedCommitPreparation<?>> currents = new HashMap<>();

    /**
     * <p>
     * For wizard, we start a global commit preparation for ALL projects
     * </p>
     *
     * @return
     */
    public WizzardCommitPreparationResult startWizzardLocalCommitPreparation() {

        LOGGER.info("Request for wizard commit preparation on all projects");

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
                    PilotedCommitPreparation<PreparedIndexEntry> preparation = startLocalPreparation(p.getUuid());

                    LOGGER.info("Request for a new commit preparation in wizard context - starting preparation"
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
     * Init a revert commit - Start async diff analysis before commit
     */
    public PilotedCommitPreparation<?> startRevertCommitPreparation(UUID sourceCommit) {

        UUID projectUuid = getActiveProjectUuid();

        // On existing preparation, restart it
        if (this.currents.get(projectUuid) != null) {

            // Cancel for any existing reference ...
            this.currents.get(projectUuid).setStatus(PilotedCommitStatus.CANCEL);

            // ... but droping should be enough
            this.currents.remove(projectUuid);
        }

        LOGGER.info("Request for a new revert commit preparation from source {}", sourceCommit);

        // For CommitState REVERT => Use PreparedRevertIndexEntry (completed in != steps)
        PilotedCommitPreparation<PreparedRevertIndexEntry> preparation = new PilotedCommitPreparation<>(CommitState.REVERT);
        preparation.setProjectUuid(projectUuid);

        // Identify revert source
        preparation.setCommitData(new CommitEditData());
        preparation.getCommitData().setRevertSourceCommitUuid(sourceCommit);

        // Apply all existing lobs in one step
        this.lobs.findByCommit(new Commit(sourceCommit))
                .forEach(l -> preparation.getDiffLobs().put(l.getHash(), l.getData()));

        // Init feature support for attachments
        setAttachmentFeatureSupports(preparation);

        // Support for some post processes
        if (this.updater != null) {
            this.updater.completeForRevert(preparation, projectUuid);
        }

        // Specify as active one
        this.currents.put(projectUuid, preparation);

        this.async.start(preparation, (x) -> this.processOnAll(preparation, this::callRevert));

        return preparation;
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
    public ExportImportResult<PilotedCommitPreparation<PreparedMergeIndexEntry>> startMergeCommitPreparation(ExportFile file) {

        UUID projectUuid = getActiveProjectUuid();

        // On existing preparation
        if (this.currents.get(projectUuid) != null) {

            // Impossible situation
            LOGGER.error("Cannot proceed to import entry point for processing merge commit will a preparation is still running.");
            throw new ApplicationException(PREPARATION_CANNOT_START,
                    "Cannot proced to import entry point for processing merge commit will a "
                            + "preparation is still running.");
        }

        LOGGER.info("Request for a new merge commit preparation from an import - start a new one");

        // For CommitState MERGE => Use PreparedMergeIndexEntry (completed in != steps)
        PilotedCommitPreparation<PreparedMergeIndexEntry> preparation = new PilotedCommitPreparation<>(CommitState.MERGED);
        preparation.setProjectUuid(projectUuid);
        preparation.setSourceFilename(file.getFilename());

        // Default filtered on needAction onlys
        preparation.setDisplayAll(false);

        // Init feature support for attachments
        setAttachmentFeatureSupports(preparation);

        // First step is NOT async : load the package and identify the applicable index
        ExportImportResult<PilotedCommitPreparation<PreparedMergeIndexEntry>> importResult = this.commitService.importCommits(file,
                preparation);

        // Support for some post processes
        if (this.updater != null) {
            this.updater.completeForMerge(preparation, projectUuid);
        }

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
     * For rendering of "ignored" items
     *
     * @param displayAll
     */
    public void setCurrentDisplayAllIncludingSimilar(boolean displayAll) {
        getCurrentCommitPreparation().setDisplayAll(displayAll);
    }

    /**
     * <p>
     * Content of diffDisplay is rendered by paginated page. This method provides 1 page
     * for one diffDisplay content list. A search on key values can be specified (default
     * is null)
     * </p>
     *
     * @param pageIndex     requested paginated page index
     * @param currentSearch all search (filter / sort) criteria in a combined component
     * @return one page of content, filtered, sorted and paginated
     */
    public DiffContentPage getPaginatedDiffContent(int pageIndex, DiffContentSearch currentSearch) {

        // Apply pagination on filtered content directly
        return new DiffContentPage(pageIndex, getFilteredDiffContent(currentSearch), this.diffDisplayPageSize);
    }

    public Collection<PreparedIndexEntry> updateDataRevert(Collection<PreparedIndexEntry> listIndex) {
        listIndex.forEach(
                y -> {
                    String tmp = y.getPayload();
                    y.setPayload(y.getPrevious());
                    y.setPrevious(tmp);

                    y.setRollbacked(false);
                    y.setSelected(true);
                    y.setDomainName(y.getDomainName());
                    y.setTableName(y.getTableName());

                    if (y.getAction() == IndexAction.ADD) {
                        y.setAction(IndexAction.REMOVE);
                    } else if (y.getAction() == IndexAction.REMOVE) {
                        y.setAction(IndexAction.ADD);
                    } else {
                        y.setAction(IndexAction.UPDATE);
                    }

                }
        );

        return listIndex;
    }

    /**
     * <p>
     * Update all preparation with selection states
     * </p>
     *
     * @param selected   true to apply "selected" state
     * @param rollbacked true to apply "roolbacked" state
     */
    public void updateAllPreparationSelections(boolean selected, boolean rollbacked) {
        getPreparationContentForSelection().getDiffContent()
                .forEach(i -> {
                    i.setRollbacked(rollbacked);
                    i.setSelected(selected);
                });
    }

    /**
     * <p>
     * Update the preparation items which match the specified filter
     * </p>
     *
     * @param search     filtering definition
     * @param selected   true to apply "selected" state
     * @param rollbacked true to apply "roolbacked" state
     */
    public void updateFilteredPreparationSelections(DiffContentSearch search, boolean selected, boolean rollbacked) {
        getFilteredDiffContent(search)
                .forEach(i -> {
                    i.setRollbacked(rollbacked);
                    i.setSelected(selected);
                });
    }

    /**
     * <p>
     * Update one preparation item
     * </p>
     *
     * @param itemIndex  temp identified for the item to update in diff
     * @param selected   true to apply "selected" state
     * @param rollbacked true to apply "roolbacked" state
     */
    public void updateDiffLinePreparationSelections(String itemIndex, boolean selected, boolean rollbacked) {
        getPreparationContentForSelection().getDiffContent().stream()
                .filter(d -> d.getIndexForDiff().equals(itemIndex)).findFirst()
                .ifPresent(
                        i -> {
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
            boolean result = ((PilotedCommitPreparation<PreparedMergeIndexEntry>) current)
                    .getDiffContent().stream().anyMatch(PreparedMergeIndexEntry::isNeedAction);

            LOGGER.debug("Checking if current merge commit needs action. Found {}", result);

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
    public PreparationState getCurrentCommitPreparationState() {

        PilotedCommitPreparation<?> preparation = getCurrentCommitPreparation();
        return preparation != null
                ? new PreparationState(preparation.getStatus(), preparation.getPercentDone())
                : new PreparationState(PilotedCommitStatus.NOT_LAUNCHED, 0);
    }

    /**
     * <p>
     * Merged status for all the commit preparation
     * </p>
     *
     * @return
     */
    public PreparationState getAllCommitPreparationStates() {

        // None => Not launched
        if (this.currents.size() == 0) {
            return new PreparationState(PilotedCommitStatus.NOT_LAUNCHED, 0);
        }

        // Completion needs all completed
        return new PreparationState(this.currents.values().stream().allMatch(p -> p.getStatus() == PilotedCommitStatus.COMMIT_CAN_PREPARE)
                ? PilotedCommitStatus.COMMIT_CAN_PREPARE
                : PilotedCommitStatus.DIFF_RUNNING,
                this.currents.values().stream()
                        .mapToInt(PilotedCommitPreparation::getPercentDone).sum() / this.currents.size()
        );
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
     * Force abort everything in app ...
     */
    public void killAllCommitPreparations() {
        this.currents.forEach((k, prep) -> {
            LOGGER.debug("Force Kill for project {} : {} preparation", k, (prep != null) ? prep.getStatus() : "NONE");
            if (prep != null) {
                prep.setStatus(PilotedCommitStatus.CANCEL);
                this.async.kill(prep.getIdentifier());
            }
        });
        this.currents.clear();
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

            // Remove from survey also if not done yet
            this.async.dropFromSurvey(prep);
        }

        // Null = COMPLETED / CANCEL from local service pov
        this.currents.remove(projectUuid);
    }

    /**
     * To complete (validated) preparation : closes and drops it, then makes service ready
     * for a new one
     */
    private void completeCommitPreparation() {

        UUID projectUuid = getActiveProjectUuid();

        PilotedCommitPreparation<?> preparation = this.currents.get(projectUuid);

        if (preparation != null) {

            // For any ref holder : mark completed
            preparation.setStatus(PilotedCommitStatus.COMPLETED);

            this.async.dropFromSurvey(preparation);

            // Null = COMPLETED from local service pov
            this.currents.remove(projectUuid);
        }
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
    public void copyCommitPreparationSelections(PilotedCommitPreparation<?> changedPreparation) {

        // Can be empty if no content was selected (ex : import a commit when everything
        // is already managed locally)
        if (!changedPreparation.getDiffContent().isEmpty()) {

            // Prepare direct access to index entries by there "index"
            Map<String, PreparedIndexEntry> entries = changedPreparation.getDiffContent().stream()
                    .collect(Collectors.toMap(PreparedIndexEntry::getIndexForDiff, p -> p));

            LOGGER.debug("Process copy of preparation selection for {} entries", entries.size());

            // Iterate all local entries and apply selection (if any)
            getCurrentCommitPreparation().getDiffContent().forEach(currentEntry -> {
                PreparedIndexEntry changedEntry = entries.get(currentEntry.getIndexForDiff());
                if (changedEntry != null) {
                    currentEntry.setRollbacked(changedEntry.isRollbacked());
                    currentEntry.setSelected(changedEntry.isSelected());
                }
            });
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
            PilotedCommitPreparation<? extends PreparedIndexEntry> changedPreparation) {
        System.out.println("=========> " + changedPreparation.getCommitData());

        setCommitPreparationCommitData(changedPreparation.getCommitData());
    }

    /**
     * <p>
     * Dedicated process for the wizard piloted "initial commit" : apply the commit
     * comment, and mark all diff as selected
     * </p>
     *
     * @param comment
     */
    public void finalizeWizzardCommitPreparation(String comment) {

        // Shared process on all preparations
        this.currents.values().forEach(current -> {

            // If not done yet, init for comment apply
            if (current.getCommitData() == null) {
                current.setCommitData(new CommitEditData());
            }

            // Other editable is the commit comment
            current.getCommitData().setComment(comment);

            // Mark all items as selecteds
            current.getDiffContent().forEach(i -> i.setSelected(true));
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
                .peek(current -> {

                    LOGGER.info("Starting saving for preparation in wizard {}", current.getIdentifier());

                    // Check mandatory comment (checked front side also)
                    if (current.getCommitData().getComment() == null) {
                        throw new ApplicationException(COMMIT_MISS_COMMENT,
                                "Commit preparation cannot be saved without a fixed comment");
                    }

                    // Save update
                    UUID commitUUID = this.commitService.saveAndApplyPreparedCommit(current);

                    // For any ref holder : mark completed
                    current.setStatus(PilotedCommitStatus.COMPLETED);

                    LOGGER.info("Saving completed for commit preparation. New commit is {}", commitUUID);

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

        // Save update
        UUID commitUUID = this.commitService.saveAndApplyPreparedCommit(current);

        // Apply rollbacks on local commits only
        if (current.getPreparingState() == CommitState.LOCAL) {
            this.commitService.applyExclusionsFromLocalCommit(current, new Commit(commitUUID));
        }

        current.setStatus(PilotedCommitStatus.ROLLBACK_APPLIED);

        // Drop preparation (if not done yet)
        completeCommitPreparation();

        LOGGER.info("Saving completed for commit preparation. New commit is {}", commitUUID);

        return commitUUID;
    }

    /**
     * Apply a filtering on <i>CurrentCommitPreparation</i>
     *
     * @param currentSearch filter to apply
     * @return filtered content.
     * @throws ApplicationException if no content available
     */
    private List<? extends PreparedIndexEntry> getFilteredDiffContent(DiffContentSearch currentSearch) {

        PilotedCommitPreparation<?> current = getCurrentCommitPreparation();

        // Filter and sort content regarding the specified search
        if (current != null) {

            List<? extends PreparedIndexEntry> diffContent = currentSearch.filterAndSortDiffContentInMemory(current);

            // Complete tableName / DomainName to display (only on listed results)
            diffContent.forEach(i -> {
                DictionaryEntrySummary dic = current.getReferencedTables().get(i.getDictionaryEntryUuid());
                if (dic != null) {
                    i.setTableName(dic.getTableName());
                    i.setDomainName(dic.getDomainName());
                }
            });
            return diffContent;
        }
        throw new ApplicationException(PREPARATION_NOT_READY, "Cannot get content of current preparation to filter");
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
    private PilotedCommitPreparation<PreparedIndexEntry> startLocalPreparation(UUID projectUuid) {

        // For CommitState LOCAL => Use PreparedIndexEntry
        PilotedCommitPreparation<PreparedIndexEntry> preparation = new PilotedCommitPreparation<>(CommitState.LOCAL);
        preparation.setProjectUuid(projectUuid);

        // Init feature support for attachments
        setAttachmentFeatureSupports(preparation);

        // Support for some post processes
        if (this.updater != null) {
            this.updater.completeForDiff(preparation, projectUuid);
        }

        // Specify as active one
        this.currents.put(projectUuid, preparation);

        this.async.start(preparation, (x) -> this.processOnAll(preparation, this::callDiff));

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
     * table diffs - or revert - (one task for each managed table). Similar to a "git status"
     * </p>
     * <p>
     * Use parallele processes, but not asyncronous by itself : can be launched as a
     * CompletableFuture in call processes
     * </p>
     */
    private <T extends PreparedIndexEntry> void processOnAll(PilotedCommitPreparation<T> preparation, BiFunction<PilotedCommitPreparation<T>, DictionaryEntry, Callable<Void>> callableBuilder) {

        LOGGER.info("Begin diff process on commit preparation {}", preparation.getIdentifier());

        try {
            long startTimeout = System.currentTimeMillis();

            List<DictionaryEntry> dictEntries = this.dictionary.findByDomainProject(new Project(preparation.getProjectUuid()));

            // Process details
            List<Callable<?>> callables = dictEntries
                    .stream()
                    .map(d -> callableBuilder.apply(preparation, d))
                    .collect(Collectors.toList());

            preparation.setProcessStarted(callables.size());
            preparation.setProcessRemaining(new AtomicInteger(callables.size()));

            LOGGER.info("Diff LOCAL process starting with {} diff to run", callables.size());

            this.async.processSteps(callables, preparation);

            // Mark preparation as completed
            preparation.setStatus(PilotedCommitStatus.COMMIT_CAN_PREPARE);

            // And stop survey (on own thread)
            this.async.dropFromSurvey(preparation);

            LOGGER.info("Diff process completed on commit preparation {}. Found {} index entries. Total process duration was {} ms",
                    preparation.getIdentifier(),
                    preparation.getDiffContent().size(),
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
    private void processAllMergeDiff(PilotedCommitPreparation<PreparedMergeIndexEntry> preparation) {

        LOGGER.info("Begin diff process on merge-commit preparation {}", preparation.getIdentifier());

        try {
            long startTimeout = System.currentTimeMillis();

            if (!preparation.getDiffContent().isEmpty()) {

                Map<UUID, DictionaryEntry> dictByUuid = this.dictionary.findAllByProjectMappedToUuid(new Project(preparation.getProjectUuid()));

                // Process details
                List<Callable<?>> callables = preparation.getDiffContent().stream()
                        // Sort index by date for clean merge build
                        .sorted(Comparator.comparing(PreparedIndexEntry::getTimestamp))
                        .collect(Collectors.groupingBy(PreparedIndexEntry::getDictionaryEntryUuid))
                        .entrySet().stream()
                        .filter(Objects::nonNull)
                        // Reset content in preparation for commit build, once completed
                        .peek(p -> preparation.getDiffContent().removeAll(p.getValue()))
                        .map(p -> callMergeDiff(preparation, p.getValue(), dictByUuid.get(p.getKey())))
                        .collect(Collectors.toList());

                preparation.setProcessStarted(callables.size());
                preparation.setProcessRemaining(new AtomicInteger(callables.size()));

                LOGGER.info("Diff MERGE process starting with {} diff to run", callables.size());

                // Run all callables in parallel exec
                this.async.processSteps(callables, preparation);

                // Mark preparation as completed
                preparation.setStatus(PilotedCommitStatus.COMMIT_CAN_PREPARE);

                LOGGER.info(
                        "Diff process completed on merge commit preparation {}. Found {} index entries. Total process duration was {} ms",
                        preparation.getIdentifier(),
                        preparation.getDiffContent().size(),
                        System.currentTimeMillis() - startTimeout);
            } else {
                LOGGER.info("Import found no differences. No merge to run");
                preparation.setStatus(PilotedCommitStatus.COMPLETED);
            }

            // And stop survey (on own thread)
            this.async.dropFromSurvey(preparation);

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
    private Callable<Void> callDiff(
            final PilotedCommitPreparation<PreparedIndexEntry> current,
            DictionaryEntry dict) {

        return () -> {
            // Controle if table not yet specified
            assertDictionaryEntryIsRealTable(dict, current);

            // Search diff content
            try {
                this.diffService.completeLocalDiff(current, dict, current.getDiffLobs(), new Project(current.getProjectUuid()));
            } catch (Throwable ex) {
                LOGGER.error("Error on local diff process for table {}", dict.getTableName(), ex);
                throw new ApplicationException(PREPARATION_BIZ_FAILURE, "Error on local diff process for dict entry " + dict.getUuid(), ex, dict.getTableName());
            }
            int rem = current.getProcessRemaining().decrementAndGet();
            LOGGER.info("Completed 1 local Diff. Remaining : {} / {}", rem, current.getProcessStarted());

            return null;
        };
    }

    /**
     * <p>
     * Execution for one table, as a <tt>Callable</tt>, for a basic local diff.
     * </p>
     *
     * @param dict
     * @return
     */
    private Callable<Void> callRevert(
            final PilotedCommitPreparation<PreparedRevertIndexEntry> current,
            DictionaryEntry dict) {

        return () -> {
            // Controle if table not yet specified
            assertDictionaryEntryIsRealTable(dict, current);

            // Init directly commit revert content
            try {
                this.diffService.completeRevertDiff(current, dict);
            } catch (Throwable ex) {
                LOGGER.error("Error on local revert process for table {}", dict.getTableName(), ex);
                throw new ApplicationException(PREPARATION_BIZ_FAILURE, "Error on local diff process for dict entry " + dict.getUuid(), ex, dict.getTableName());
            }
            int rem = current.getProcessRemaining().decrementAndGet();
            LOGGER.info("Completed 1 local Diff. Remaining : {} / {}", rem, current.getProcessStarted());

            return null;
        };
    }

    /**
     * <p>
     * Execution for one table, as a <tt>Callable</tt>, for a merge process diff. The list
     * of diff in <tt>MergePreparedDiff</tt> is regenerated, and DictionaryEntry data are
     * completed.
     *
     * @param current           preparing preparation
     * @param dict
     * @param correspondingDiff
     * @return Void (ignore result, content is updated in PilotedCommitPreparation)
     */
    private Callable<Void> callMergeDiff(
            final PilotedCommitPreparation<PreparedMergeIndexEntry> current,
            List<PreparedMergeIndexEntry> correspondingDiff,
            DictionaryEntry dict) {

        return () -> {
            // Control if table not yet specified
            assertDictionaryEntryIsRealTable(dict, current);

            // Then run one merge action for dictionaryEntry
            this.diffService.completeMergeDiff(current, dict, current.getDiffLobs(),
                    correspondingDiff, new Project(current.getProjectUuid()));

            int rem = current.getProcessRemaining().decrementAndGet();
            LOGGER.info("Completed 1 merge Diff. Remaining : {} / {}", rem, current.getProcessStarted());

            return null;
        };
    }

    /**
     * @param entry
     */
    private void assertDictionaryEntryIsRealTable(DictionaryEntry entry, final PilotedCommitPreparation<?> current) {

        if (entry == null) {
            LOGGER.error("No corresponding table found");
            current.fail(new ApplicationException(TABLE_WRONG_REF, "Specified table entry is missing in managed DB"));
        } else if (!this.managedDesc.isTableExists(entry.getTableName())) {
            LOGGER.error("No corresponding table found in DB for {}", entry.getTableName());
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

    public interface PreparationUpdater {

        void completeForDiff(PilotedCommitPreparation<PreparedIndexEntry> preparation, UUID projectUUID);

        void completeForRevert(PilotedCommitPreparation<PreparedRevertIndexEntry> preparation, UUID projectUUID);

        void completeForMerge(PilotedCommitPreparation<PreparedMergeIndexEntry> preparation, UUID projectUUID);
    }
}
