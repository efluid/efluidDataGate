package fr.uem.efluid.services;

import fr.uem.efluid.model.DiffLine;
import fr.uem.efluid.model.entities.*;
import fr.uem.efluid.model.repositories.*;
import fr.uem.efluid.services.types.PilotedCommitPreparation;
import fr.uem.efluid.services.types.RollbackLine;
import fr.uem.efluid.services.types.SearchHistoryPage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * <p>
 * Where diff content can be applied / rollbacked on a database
 * </p>
 * <p>
 * See {@link ManagedUpdateRepository} for remarks on specific transactional process : use
 * these methods with care.
 * </p>
 *
 * @author elecomte
 * @version 1
 * @since v0.0.1
 */
@Service
@Transactional // Core DB Transaction, NOT managed DB Transaction
public class ApplyDiffService extends AbstractApplicationService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ApplyDiffService.class);

    private static final String DEFAULT_HISTORY_SEARCH = "%";

    @Value("${datagate-efluid.display.history-page-size}")
    private int historyPageSize;

    @Autowired
    private ManagedUpdateRepository updates;

    @Autowired
    private ApplyHistoryEntryRepository history;

    @Autowired
    private ProjectManagementService projectService;

    @Autowired
    private CommitRepository commits;

    @Autowired
    private IndexRepository index;

    @Autowired
    private VersionRepository versions;

    @Autowired
    private ProjectRepository project;

    @Autowired
    private PilotableCommitPreparationService pilotableCommitService;

    /**
     * <p>
     * Due to specific transactional process required on managed DB updated by this
     * method, always call it with ALL COMBINED DiffLines.
     * </p>
     *
     * @param diffLines lines
     * @param lobs      associated lob for content extract
     */
    @Transactional(rollbackFor = Throwable.class)
    void applyDiff(List<? extends DiffLine> diffLines, Map<String, byte[]> lobs) {

        this.projectService.assertCurrentUserHasSelectedProject();
        Project project = this.projectService.getCurrentSelectedProjectEntity();

        LOGGER.info("Will apply a diff of {} items for project {}", diffLines.size(), project.getName());
        keepHistory(this.updates.runAllChangesAndCommit(diffLines, lobs, project), false);
    }

    /**
     * <p>
     * Due to specific transactional process required on managed DB updated by this
     * method, always call it with ALL COMBINED DiffLines.
     * </p>
     *
     * @param rollBackLines lines to rollback
     * @param lobs          associated lob for content extract
     */
    void rollbackDiff(List<RollbackLine> rollBackLines, Map<String, byte[]> lobs) {

        this.projectService.assertCurrentUserHasSelectedProject();
        Project project = this.projectService.getCurrentSelectedProjectEntity();

        LOGGER.info("Will apply a rollback of {} items for project {}", rollBackLines.size(), project.getName());
        keepHistory(this.updates
                        .runAllChangesAndCommit(rollBackLines.stream().map(RollbackLine::toCombinedDiff).collect(Collectors.toList()), lobs,
                                project),
                true);
    }

    /**
     * <p>
     * This method is used to revert lot
     * </p>
     *
     * @param uuid id of the commit to revert
     */

    public void revertLot(String uuid){

        List<IndexEntry> toUpdate = new ArrayList<>();
        List<IndexEntry> previous = this.index.getIndexEntriesByCommitUuid(uuid); //get current idx entries for lot

        PilotedCommitPreparation<?> prepare = this.pilotableCommitService.startLocalCommitPreparation(false); //started preparing a commit

        //created a new commit
        Commit commit = new Commit();
        commit.setCreatedTime(LocalDateTime.now());
        commit.setUser(new User(this.holder.getCurrentUser().getLogin()));
        commit.setOriginalUserEmail(this.holder.getCurrentUser().getEmail());
        commit.setState(prepare.getPreparingState());
        commit.setProject(this.project.findSelectedProjectForUserLogin(this.holder.getCurrentUser().getLogin()));
        commit.setVersion(this.versions.getLastVersionForProject(this.project.findSelectedProjectForUserLogin(this.holder.getCurrentUser().getLogin())));
        commit.setComment("Revert");

        // Prepared commit uuid
        UUID commitUUID = UUID.randomUUID();

        // UUID generate (not done by HBM / DB)
        commit.setUuid(commitUUID);

        //revert each entries of idx
        previous.forEach(
                i -> {
                    String tmp = i.getPayload();
                    i.setPayload(i.getPrevious());
                    i.setPrevious(tmp);

                    if(i.getAction().equals(IndexAction.ADD)) {
                        i.setAction(IndexAction.REMOVE);
                    } else if (i.getAction().equals(IndexAction.REMOVE)){
                        i.setAction(IndexAction.ADD);
                    } else {
                        i.setAction(i.getAction());
                    }

                    toUpdate.add(i);

                });

        commit.setIndex(toUpdate);

        this.index.saveAll(toUpdate);
        this.commits.save(commit);
    }

    /**
     * <p>
     * For requested page, search for given content in hisotyr queries
     * </p>
     *
     * @param pageIndex index in search result
     * @param search    current search query
     * @return SearchHistoryPage
     */
    public SearchHistoryPage getHistory(int pageIndex, String search) {

        String validSearch = search == null || "".equals(search.trim()) || "*".equals(search.trim()) ? DEFAULT_HISTORY_SEARCH : DEFAULT_HISTORY_SEARCH + search + DEFAULT_HISTORY_SEARCH;
        UUID projectId = this.projectService.getCurrentSelectedProject().getUuid();

        return SearchHistoryPage.fromPage(pageIndex, search,
                this.history.findByQueryLikeAndProjectUuidOrderByTimestampDesc(validSearch, projectId, PageRequest.of(pageIndex, this.historyPageSize)));
    }

    /**
     * <p>
     * Track every applied modifs in an history
     * </p>
     */
    private void keepHistory(String[] queries, boolean isRollback) {

        Long timestamp = System.currentTimeMillis();
        User currentUser = new User(getCurrentUser().getLogin());
        UUID projectId = this.projectService.getCurrentSelectedProject().getUuid();

        this.history.saveAll(Stream.of(queries).map(ApplyHistoryEntry::new).peek(h -> {
            h.setRollback(isRollback);
            h.setTimestamp(timestamp);
            h.setUser(currentUser);
            h.setProjectUuid(projectId);
        }).collect(Collectors.toList()));
    }
}
