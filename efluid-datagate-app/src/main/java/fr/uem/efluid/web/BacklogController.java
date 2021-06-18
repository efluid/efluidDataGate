package fr.uem.efluid.web;

import fr.uem.efluid.model.entities.CommitState;
import fr.uem.efluid.services.*;
import fr.uem.efluid.services.types.*;
import fr.uem.efluid.utils.WebUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import javax.validation.Valid;
import java.util.UUID;

import static org.springframework.web.bind.annotation.RequestMethod.*;

/**
 * <p>
 * Routing and model init for everything related to backlog management : diff preparation,
 * commit listing, commit validation, and associated import / export features. Templating
 * is managed with Thymeleaf.
 * </p>
 * <p>
 * Can be seen as the "provider of all 'GIT' features for the parameter management"
 * </p>
 *
 * @author elecomte
 * @version 4
 * @since v0.0.1
 */
@Controller
@RequestMapping("/ui")
public class BacklogController extends CommonController {

    @Autowired
    private CommitService commitService;

    @Autowired
    private PilotableCommitPreparationService pilotableCommitService;

    @Autowired
    private DictionaryManagementService dictService;

    @Autowired
    private ApplicationDetailsService applicationDetailsService;

    @Autowired
    private TransformerService transformerService;

    @Autowired
    private ApplyDiffService diffService;

    @Autowired
    PrepareIndexService prepIndex;


    /**
     * <p>
     * For history navigate default rendering
     * </p>
     *
     * @param model
     * @return
     */
    @RequestMapping("/history")
    public String historyPage(Model model) {
        model.addAttribute("currentLocationTitle", "Historique des mises à jour");
        model.addAttribute("projectName", this.projectManagementService.getCurrentSelectedProjectShortName());

        return historySearchPageNav(model, 0, null);
    }

    /**
     * <p>
     * For new search
     * </p>
     *
     * @param model
     * @param search
     * @return
     */
    @RequestMapping(path = "/history", method = POST)
    public String historySearchPage(Model model, @RequestParam("search") String search) {

        return historySearchPageNav(model, 0, search);
    }

    /**
     * @param model
     * @param pageNbr
     * @return
     */
    @RequestMapping(path = "/history/{page}", method = GET)
    public String historySearchPageNav(Model model, @PathVariable("page") int pageNbr) {

        return historySearchPageNav(model, pageNbr, null);
    }

    /**
     * @param model
     * @param pageNbr
     * @param search
     * @return
     */
    @RequestMapping(path = "/history/{page}/{search}", method = GET)
    public String historySearchPageNav(Model model, @PathVariable("page") int pageNbr, @PathVariable("search") String search) {

        if (!controlSelectedProject(model)) {
            return REDIRECT_SELECT;
        }

        SearchHistoryPage page = this.diffService.getHistory(pageNbr, search);

        // For formatting
        WebUtils.addTools(model);

        // For search nav
        WebUtils.addPageNavBarItems(model, "/ui/history", search, pageNbr, page.getPageCount());

        // Get updated preparation
        model.addAttribute("history", page);

        return "pages/history";
    }

    /**
     * <p>
     * For default prepare page
     * </p>
     *
     * @param model
     * @return
     */
    @RequestMapping("/prepare")
    public String preparationPage(Model model) {
        model.addAttribute("currentLocationTitle", "Préparer un lot");
        model.addAttribute("projectName", this.projectManagementService.getCurrentSelectedProjectShortName());

        return startPreparationAndRouteRegardingStatus(model, false);
    }

    /**
     * <p>
     * For default prepare page
     * </p>
     *
     * @param model
     * @return
     */
    @RequestMapping("/revert/restart")
    public String revertRestart(Model model) {
        PilotedCommitPreparation<?> prepare = this.pilotableCommitService.getCurrentCommitPreparation();

        return revert(model, prepare.getCommitData().getRevertSourceCommitUuid());
    }

    /**
     * <p>
     * Force restart in a new preparation
     * </p>
     *
     * @param model
     * @return
     */
    @RequestMapping("/prepare/restart")
    public String preparationRestart(Model model) {
        model.addAttribute("projectName", this.projectManagementService.getCurrentSelectedProjectShortName());

        return startPreparationAndRouteRegardingStatus(model, true);
    }

    /**
     * @return status as a value
     */
    @RequestMapping(path = {"/prepare/progress", "/merge/progress", "/revert/progress"}, method = GET)
    @ResponseBody
    public PreparationState preparationGetState() {
        return this.pilotableCommitService.getCurrentCommitPreparationState();
    }

    /**
     * @return content for paginated diff content rendering
     */
    @RequestMapping(path = {"/prepare/page/{page}", "/merge/page/{page}", "/revert/page/{page}"}, method = {GET, POST})
    @ResponseBody
    public DiffContentPage preparationGetDiffContentPage(
            @PathVariable("page") int page,
            @RequestBody(required = false) DiffContentSearch search) {

        return this.pilotableCommitService.getPaginatedDiffContent(page, search);
    }

    /**
     * @return content for paginated commit index rendering
     */
    @RequestMapping(path = "/details/{uuid}/page/{page}", method = {GET, POST})
    @ResponseBody
    public DiffContentPage commitDetailsContentPage(
            @PathVariable("uuid") UUID uuid,
            @PathVariable("page") int page,
            @RequestBody(required = false) DiffContentSearch search) {

        return this.commitService.getPaginatedExistingCommitContent(uuid, page, search);
    }

    /**
     * @return content for paginated commit index rendering
     */
    @RequestMapping(path = "/details/{uuid}/rename", method = PUT)
    @ResponseBody
    public DiffContentPage commitDetailsContentPage(
            @PathVariable("uuid") UUID uuid,
            @RequestParam("name") String name) {

        // TODO : call rename
        return this.commitService.getPaginatedExistingCommitContent(uuid, page, search);
    }

    /**
     * <p>
     * Update selection for the whole diff
     * </p>
     *
     * @param selected   state "selected", arg param
     * @param rollbacked state "rollbacked", arg param
     */
    @RequestMapping(path = {"/prepare/selection/all", "/merge/selection/all", "/revert/selection/all"}, method = POST)
    @ResponseBody
    public void preparationSelectionUpdateAll(@RequestParam boolean selected, @RequestParam boolean rollbacked) {

        this.pilotableCommitService.updateAllPreparationSelections(selected, rollbacked);
    }

    /**
     * <p>
     * Update selection for a filtered diff
     * </p>
     *
     * @param selected   state "selected", arg param
     * @param rollbacked state "rollbacked", arg param
     * @param search     body content search
     */
    @RequestMapping(path = {"/prepare/selection/filtered", "/merge/selection/filtered", "/revert/selection/filtered"}, method = POST)
    @ResponseBody
    public void preparationSelectionUpdateFiltered(
            @RequestParam boolean selected,
            @RequestParam boolean rollbacked,
            @RequestBody(required = false) DiffContentSearch search) {

        this.pilotableCommitService.updateFilteredPreparationSelections(search, selected, rollbacked);
    }

    /**
     * <p>
     * Update selection for one item
     * </p>
     *
     * @param itemIndex  selected item temp identifier, as path param
     * @param selected   state "selected", arg param
     * @param rollbacked state "rollbacked", arg param
     */
    @RequestMapping(path = {"/prepare/selection/line/{index}", "/merge/selection/line/{index}", "/revert/selection/line/{index}"}, method = POST)
    @ResponseBody
    public void preparationSelectionUpdateItem(@PathVariable("index") String itemIndex, @RequestParam boolean selected,
                                               @RequestParam boolean rollbacked) {

        this.pilotableCommitService.updateDiffLinePreparationSelections(itemIndex, selected, rollbacked);
    }

    /**
     * @param model
     * @param preparation
     * @return
     */
    @RequestMapping(path = {"/prepare/commit", "/merge/commit", "/revert/commit"}, method = POST)
    public String preparationCommitPage(Model model, @ModelAttribute PilotedCommitPreparation<?> preparation, @RequestAttribute(required = false) PilotedCommitPreparation<?> preparationPush) {

        if (!controlSelectedProject(model)) {
            return REDIRECT_SELECT;
        }

        // Update current preparation with selected attributes
        this.pilotableCommitService.copyCommitPreparationSelections(preparationPush != null ? preparationPush : preparation);

        // Get updated preparation
        model.addAttribute("preparation", this.pilotableCommitService.getCurrentCommitPreparation());

        // Get current version
        model.addAttribute("version", this.dictService.getLastVersion());

        return "pages/commit";
    }

    /**
     * @param model
     * @param preparation
     * @return
     */
    @RequestMapping(path = {"/prepare/save", "/merge/save", "/revert/save"}, method = POST)
    public String preparationSave(Model model, @ModelAttribute PilotedCommitPreparation<?> preparation, @RequestAttribute(required = false) PilotedCommitPreparation<?> preparationPush) {

        if (!controlSelectedProject(model)) {
            return REDIRECT_SELECT;
        }

        // Update current preparation with selected attributes
        this.pilotableCommitService.copyCommitPreparationCommitData(preparationPush != null ? preparationPush : preparation);
        UUID created = this.pilotableCommitService.saveCommitPreparation();

        // Get updated preparation
        model.addAttribute("preparation", this.pilotableCommitService.getCurrentCommitPreparation());

        // For success save message
        model.addAttribute("from", "success_edit");

        // For details
        model.addAttribute("createdUUID", created);

        // Forward to commits list
        return commitListPage(model);
    }

    /**
     * @param model
     * @return
     */
    @RequestMapping(path = {"/prepare/cancel", "/merge/cancel", "/revert/cancel"}, method = GET)
    public String preparationCancel(Model model) {

        if (!controlSelectedProject(model)) {
            return REDIRECT_SELECT;
        }

        // Update current preparation as canceled
        this.pilotableCommitService.cancelCommitPreparation();

        // Forward to commits list
        return commitListPage(model);
    }

    /**
     * @param model
     * @return
     */
    @RequestMapping(path = "/push", method = GET)
    public String commitExportPage(Model model) {

        if (!controlSelectedProject(model)) {
            return REDIRECT_SELECT;
        }

        // For formatting
        WebUtils.addTools(model);

        // Get updated commits
        model.addAttribute("commits", this.commitService.getAvailableCommits());
        model.addAttribute("currentLocationTitle", "Exporter un lot");
        model.addAttribute("checkVersion", this.dictService.isDictionaryUpdatedAfterLastVersion());
        model.addAttribute("version", this.dictService.getLastVersion());
        model.addAttribute("modelDesc", this.applicationDetailsService.getCurrentModelId());
        model.addAttribute("projectName", this.projectManagementService.getCurrentSelectedProjectShortName());

        return "pages/push";
    }

    /**
     * Prepare a new export of commit
     *
     * @param model s-mvc context
     * @param uuid  support ALL or a clean UUID
     * @param type  specified export type
     * @return tpl for edit of an export
     */
    @RequestMapping(path = "/push/prepare/{uuid}/{type}", method = GET)
    public String commitExportEditPage(Model model, @PathVariable("uuid") String uuid, @PathVariable("type") CommitExportEditData.CommitSelectType type) {

        if (!controlSelectedProject(model)) {
            return REDIRECT_SELECT;
        }

        // For formatting
        WebUtils.addTools(model);

        // Editing commit - support for "ALL" as commit UUID
        model.addAttribute("exportEdit", this.commitService.initCommitExport(type,
                uuid != null && !uuid.equals("ALL") ? UUID.fromString(uuid) : null));

        // Transformers from project
        model.addAttribute("transformerDefs", this.transformerService.getAllTransformerDefs(true));

        // Other info to display
        model.addAttribute("version", this.dictService.getLastVersion());
        model.addAttribute("modelDesc", this.applicationDetailsService.getCurrentModelId());

        return "pages/push_prepare";
    }

    /**
     * Save a prepared export of commits with customization, ready to be downloaded from push page
     *
     * @param model          s-mvc context
     * @param exportEditData prepared export content with customization
     * @return tpl of push entry point
     */
    @RequestMapping(path = "/push/save", method = POST)
    public String commitExportSave(Model model, @ModelAttribute @Valid CommitExportEditData exportEditData) {

        // Add saved export details as "ready to process"
        model.addAttribute("ready", this.commitService.saveCommitExport(exportEditData));

        // And continue to export listing page where export download will be processed
        return commitExportPage(model);
    }

    /**
     * Check if download completed
     *
     * @param uuid of prepared commit export (Export entity)
     * @return true if content downloaded
     */
    @RequestMapping(value = "/push/state/{uuid}", method = GET)
    @ResponseBody
    public boolean isCommitExportDownloaded(@PathVariable("uuid") UUID uuid) {
        return this.commitService.isCommitExportDownloaded(uuid);
    }

    /**
     * Download one export from uuid of prepared export, using the specified name as file destination download name. Single download endpoint
     *
     * @param uuid of prepared commit export (Export entity)
     * @param name for downloaded file, used to force set the destination file on browser
     * @return download content
     */
    @RequestMapping(value = "/push/{uuid}/{name}.par", method = GET)
    @ResponseBody
    public ResponseEntity<InputStreamResource> downloadCommitExport(@PathVariable("uuid") UUID uuid, @PathVariable("name") String name) {
        return WebUtils.outputExportImportFile(name, this.commitService.processCommitExport(uuid).getResult());
    }

    /**
     * @param hash
     * @return
     */
    @RequestMapping(path = "/lob/{lobHashEnc}", method = GET)
    @ResponseBody
    public ResponseEntity<InputStreamResource> downloadLobContent(@PathVariable("lobHashEnc") String hash) {

        // Search in both "prepared" and existing lobs data
        return WebUtils.outputData(this.pilotableCommitService.getCurrentOrExistingLobData(hash));
    }

    /**
     * @param model
     * @return
     */
    @RequestMapping(value = "/pull", method = GET)
    public String pullPage(Model model) {

        if (!controlSelectedProject(model)) {
            return REDIRECT_SELECT;
        }

        // Forward to merge if active
        if (this.pilotableCommitService.getCurrentCommitPreparation() != null
                && this.pilotableCommitService.getCurrentCommitPreparation().getPreparingState() != CommitState.LOCAL) {
            return processImport(model);
        }

        model.addAttribute("currentLocationTitle", "Importer un lot");
        model.addAttribute("projectName", this.projectManagementService.getCurrentSelectedProjectShortName());

        return "pages/pull";
    }

    /**
     * @param model
     * @param request
     * @return
     */
    @RequestMapping(value = "/pull/upload", method = POST)
    public String uploadImport(Model model, MultipartHttpServletRequest request) {

        if (!controlSelectedProject(model)) {
            return REDIRECT_SELECT;
        }

        model.addAttribute("result", this.pilotableCommitService.startMergeCommitPreparation(WebUtils.inputExportImportFile(request)));

        return "pages/merging";
    }

    /**
     * @param model
     * @param name
     * @return
     */
    @RequestMapping(value = "/attachment/remove", method = GET)
    public String removeAttachment(Model model, @RequestParam("name") String name) {

        if (!controlSelectedProject(model)) {
            return REDIRECT_SELECT;
        }

        // Update current preparation with selected attributes
        this.pilotableCommitService.removeAttachmentOnCurrentCommitPreparation(name);

        // Get updated preparation
        model.addAttribute("preparation", this.pilotableCommitService.getCurrentCommitPreparation());

        // Get current version
        model.addAttribute("version", this.dictService.getLastVersion());

        return "pages/commit";
    }

    /**
     * @param model
     * @param request
     * @return
     */
    @RequestMapping(value = "/attachment/upload", method = POST)
    public String uploadAttachment(Model model, MultipartHttpServletRequest request) {

        if (!controlSelectedProject(model)) {
            return REDIRECT_SELECT;
        }

        // Update current preparation with selected attributes
        this.pilotableCommitService.addAttachmentOnCurrentCommitPreparation(WebUtils.inputExportImportFile(request));

        // Get updated preparation
        model.addAttribute("preparation", this.pilotableCommitService.getCurrentCommitPreparation());

        // Get current version
        model.addAttribute("version", this.dictService.getLastVersion());

        return "pages/commit";
    }

    /**
     * @param name
     * @param uuid
     * @return
     */
    @RequestMapping(path = "/attachment/content", method = GET)
    @ResponseBody
    public ResponseEntity<InputStreamResource> downloadAttachmentContent(
            @RequestParam(name = "name", required = false) String name,
            @RequestParam(name = "uuid", required = false) UUID uuid) {

        // Provides existing content
        if (name == null && uuid != null) {
            return WebUtils.outputData(this.commitService.getExistingAttachmentData(uuid));
        }

        // Search by name from current temp files
        return WebUtils.outputData(this.pilotableCommitService.getAttachmentContentFromCurrentCommitPreparation(name));
    }

    /**
     * @param model
     * @return
     */
    @RequestMapping(value = "/pull/upload", method = GET)
    public String processImport(Model model) {

        if (!controlSelectedProject(model)) {
            return REDIRECT_SELECT;
        }

        model.addAttribute("preparation", this.pilotableCommitService.getCurrentCommitPreparation());

        return "pages/merging";
    }

    /**
     * <p>
     * For default merge page
     * </p>
     *
     * @param model
     * @return
     */
    @RequestMapping(value = "/merge", method = GET)
    public String mergePage(Model model, @RequestParam(required = false, name = "showAll", defaultValue = "false") boolean showAll) {

        if (!controlSelectedProject(model)) {
            return REDIRECT_SELECT;
        }

        this.pilotableCommitService.setCurrentDisplayAllIncludingSimilar(showAll);

        model.addAttribute("preparation", this.pilotableCommitService.getCurrentCommitPreparation());
        model.addAttribute("needsAction", this.pilotableCommitService.isCurrentMergeCommitNeedsAction());

        return "pages/merge";
    }

    /**
     * @param model
     * @return
     */
    @RequestMapping(path = "/commits", method = GET)
    public String commitListPage(Model model) {

        if (!controlSelectedProject(model)) {
            return REDIRECT_SELECT;
        }

        // For formatting
        WebUtils.addTools(model);

        // Get updated preparation
        model.addAttribute("commits", this.commitService.getAvailableCommits());
        model.addAttribute("currentLocationTitle", "Liste des lots");
        model.addAttribute("projectName", this.projectManagementService.getCurrentSelectedProjectShortName());
        model.addAttribute("revertableCommit", this.commitService.getRevertCompliantCommit());

        return "pages/commits";
    }

    /**
     * @param model
     * @param uuid
     * @return
     */
    @RequestMapping(path = "/details/{uuid}", method = GET)
    public String commitDetailsPage(Model model, @PathVariable("uuid") UUID uuid) {

        if (!controlSelectedProject(model)) {
            return REDIRECT_SELECT;
        }

        // For formatting
        WebUtils.addTools(model);

        // Get updated preparation
        model.addAttribute("details", this.commitService.getExistingCommitDetails(uuid, false));
        model.addAttribute("projectName", this.projectManagementService.getCurrentSelectedProjectShortName());

        return "pages/details";
    }


    /**
     * @param uuid
     * @return
     */
    @GetMapping("/revert/{uuid}")
    public String revert(Model model, @PathVariable("uuid") UUID uuid) {

        if (!controlSelectedProject(model)) {
            return REDIRECT_SELECT;
        }

        PilotedCommitPreparation<?> prepare = this.pilotableCommitService.startRevertCommitPreparation(uuid);

        // Diff already in progress - use common diff page
        if (prepare.getStatus() == PilotedCommitStatus.DIFF_RUNNING) {
            return "pages/reverting";
        }

        // Completed / available, basic prepare page
        model.addAttribute("preparation", prepare);

        // For formatting
        WebUtils.addTools(model);

        return "pages/revert";
    }

    /**
     * @return
     */
    @GetMapping("/revert")
    public String revert(Model model) {

        if (!controlSelectedProject(model)) {
            return REDIRECT_SELECT;
        }

        PilotedCommitPreparation<?> prepare = this.pilotableCommitService.getCurrentCommitPreparation();

        // Diff already in progress - use common diff page
        if (prepare.getStatus() == PilotedCommitStatus.DIFF_RUNNING) {
            return "pages/reverting";
        }

        // Completed / available, basic prepare page
        model.addAttribute("preparation", prepare);

        // For formatting
        WebUtils.addTools(model);

        return "pages/revert";
    }


    /**
     * <p>
     * Common behavior on preparation : need to route to diff or prepare page regarding
     * status. Support service-related "forced" start
     * </p>
     *
     * @param model
     * @param forced
     * @return
     */
    private String startPreparationAndRouteRegardingStatus(Model model, boolean forced) {

        if (!controlSelectedProject(model)) {
            return REDIRECT_SELECT;
        }

        PilotedCommitPreparation<?> prepare = this.pilotableCommitService.startLocalCommitPreparation(forced);

        // Diff already in progress : dedicated waiting page
        if (prepare.getStatus() == PilotedCommitStatus.DIFF_RUNNING) {

            if (prepare.getPreparingState() == CommitState.MERGED) {
                return "pages/merging";
            }

            return "pages/diff";
        }

        // Completed / available, basic prepare page
        model.addAttribute("preparation", prepare);

        // For formatting
        WebUtils.addTools(model);

        return "pages/prepare";
    }
}
