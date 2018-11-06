package fr.uem.efluid.web;

import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.POST;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import fr.uem.efluid.model.entities.CommitState;
import fr.uem.efluid.services.ApplyDiffService;
import fr.uem.efluid.services.CommitService;
import fr.uem.efluid.services.DictionaryManagementService;
import fr.uem.efluid.services.PilotableCommitPreparationService;
import fr.uem.efluid.services.types.DiffDisplayPage;
import fr.uem.efluid.services.types.LocalPreparedDiff;
import fr.uem.efluid.services.types.PilotedCommitPreparation;
import fr.uem.efluid.services.types.PilotedCommitStatus;
import fr.uem.efluid.services.types.SearchHistoryPage;
import fr.uem.efluid.utils.WebUtils;

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
 * @since v0.0.1
 * @version 2
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
	private ApplyDiffService diffService;

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
	 * @param page
	 * @param search
	 * @return
	 */
	@RequestMapping(path = "/history/{page}", method = GET)
	public String historySearchPageNav(Model model, @PathVariable("page") int pageNbr) {

		return historySearchPageNav(model, pageNbr, null);
	}

	/**
	 * @param model
	 * @param page
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
		return startPreparationAndRouteRegardingStatus(model, false);
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
		return startPreparationAndRouteRegardingStatus(model, true);
	}

	/**
	 * @return status as a value
	 */
	@RequestMapping(path = { "/prepare/progress", "/merge/progress" }, method = GET)
	@ResponseBody
	public PilotedCommitStatus preparationGetStatus() {
		return this.pilotableCommitService.getCurrentCommitPreparationStatus();
	}

	/**
	 * @return content for paginated diffDisplay rendering
	 */
	@RequestMapping(path = { "/prepare/page/{uuid}/{page}", "/merge/page/{uuid}/{page}" }, method = GET)
	@ResponseBody
	public DiffDisplayPage preparationGetDiffDisplayPage(@PathVariable("uuid") UUID uuid, @PathVariable("page") int page,
			@RequestParam(required = false) String search) {
		
		return this.pilotableCommitService.getPaginatedDiffDisplay(uuid, page, search);
	}

	/**
	 * <p>
	 * Update selection for the whole diff
	 * </p>
	 * 
	 * @param selected
	 * @param rollbacked
	 */
	@RequestMapping(path = { "/prepare/selection/all", "/merge/selection/all" }, method = POST)
	@ResponseBody
	public void preparationSelectionUpdateAll(@RequestParam boolean selected, @RequestParam boolean rollbacked) {

		this.pilotableCommitService.updateAllPreparationSelections(selected, rollbacked);
	}

	/**
	 * <p>
	 * Update selection for a selected domain
	 * </p>
	 * 
	 * @param domainUUID
	 * @param selected
	 * @param rollbacked
	 */
	@RequestMapping(path = { "/prepare/selection/domain/{domain}", "/merge/selection/domain/{domain}" }, method = POST)
	@ResponseBody
	public void preparationSelectionUpdateDomain(@PathVariable("domain") UUID domainUUID, @RequestParam boolean selected,
			@RequestParam boolean rollbacked) {

		this.pilotableCommitService.updateDomainPreparationSelections(selected, rollbacked, domainUUID);
	}

	/**
	 * <p>
	 * Update selection for one diffDisplay
	 * </p>
	 * 
	 * @param dictUUID
	 * @param selected
	 * @param rollbacked
	 */
	@RequestMapping(path = { "/prepare/selection/dict/{dict}", "/merge/selection/dict/{dict}" }, method = POST)
	@ResponseBody
	public void preparationSelectionUpdateDiffDisplay(@PathVariable("dict") UUID dictUUID, @RequestParam boolean selected,
			@RequestParam boolean rollbacked) {

		this.pilotableCommitService.updateDiffDisplayPreparationSelections(selected, rollbacked, dictUUID);
	}

	/**
	 * <p>
	 * Update selection for one item
	 * </p>
	 * 
	 * @param index
	 * @param selected
	 * @param rollbacked
	 */
	@RequestMapping(path = { "/prepare/selection/line/{index}", "/merge/selection/line/{index}" }, method = POST)
	@ResponseBody
	public void preparationSelectionUpdateItem(@PathVariable("index") long itemIndex, @RequestParam boolean selected,
			@RequestParam boolean rollbacked) {

		this.pilotableCommitService.updateDiffLinePreparationSelections(selected, rollbacked, itemIndex);
	}

	/**
	 * @param model
	 * @param name
	 * @return
	 */
	@RequestMapping(path = { "/prepare/commit", "/merge/commit" }, method = POST)
	public String preparationLocalCommitPage(Model model, @ModelAttribute PilotedCommitPreparation<LocalPreparedDiff> preparation) {

		if (!controlSelectedProject(model)) {
			return REDIRECT_SELECT;
		}

		// Update current preparation with selected attributes
		this.pilotableCommitService.copyCommitPreparationSelections(preparation);

		// Get updated preparation
		model.addAttribute("preparation", this.pilotableCommitService.getCurrentCommitPreparation());

		// Get current version
		model.addAttribute("version", this.dictService.getLastVersion());

		return "pages/commit";
	}

	/**
	 * @param model
	 * @param name
	 * @return
	 */
	@RequestMapping(path = { "/prepare/save", "/merge/save" }, method = POST)
	public String preparationLocalSave(Model model, @ModelAttribute PilotedCommitPreparation<LocalPreparedDiff> preparation) {

		if (!controlSelectedProject(model)) {
			return REDIRECT_SELECT;
		}

		// Update current preparation with selected attributes
		this.pilotableCommitService.copyCommitPreparationCommitData(preparation);
		this.pilotableCommitService.saveCommitPreparation();

		// Get updated preparation
		model.addAttribute("preparation", this.pilotableCommitService.getCurrentCommitPreparation());

		// For success save message
		model.addAttribute("from", "success_edit");

		// Forward to commits list
		return commitListPage(model);
	}

	/**
	 * @param model
	 * @param name
	 * @return
	 */
	@RequestMapping(path = { "/prepare/cancel", "/merge/cancel" }, method = GET)
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
	 * @param name
	 * @return
	 */
	@RequestMapping(path = "/push", method = GET)
	public String commitExportPage(Model model) {

		if (!controlSelectedProject(model)) {
			return REDIRECT_SELECT;
		}

		if (!controlSelectedProject(model)) {
			return REDIRECT_SELECT;
		}

		// For formatting
		WebUtils.addTools(model);

		// Get updated commits
		model.addAttribute("commits", this.commitService.getAvailableCommits());
		model.addAttribute("checkVersion", Boolean.valueOf(this.dictService.isDictionaryUpdatedAfterLastVersion()));

		return "pages/push";
	}

	/**
	 * @param uuid
	 * @return
	 */
	@RequestMapping(value = "/push/{uuid}-commit.par", method = GET)
	@ResponseBody
	public ResponseEntity<InputStreamResource> downloadExportOneCommit(@PathVariable("uuid") UUID uuid) {

		return WebUtils.outputExportImportFile(this.commitService.exportOneCommit(uuid).getResult());
	}

	/**
	 * @return
	 */
	@RequestMapping(value = "/push/all-commits.par", method = GET)
	@ResponseBody
	public ResponseEntity<InputStreamResource> downloadExportAllCommits() {

		return WebUtils.outputExportImportFile(this.commitService.exportCommits(null).getResult());
	}

	/**
	 * @param uuid
	 * @return
	 */
	@RequestMapping(value = "/push/until-{uuid}-commits.par", method = GET)
	@ResponseBody
	public ResponseEntity<InputStreamResource> downloadExportUntil(@PathVariable("uuid") UUID uuid) {

		return WebUtils.outputExportImportFile(this.commitService.exportCommits(uuid).getResult());
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
	 * @param hash
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
	 * @param request
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

		model.addAttribute("preparation", this.pilotableCommitService.getCurrentCommitPreparation());
		model.addAttribute("needsAction", Boolean.valueOf(this.pilotableCommitService.isCurrentMergeCommitNeedsAction()));
		model.addAttribute("showAll", Boolean.valueOf(showAll));

		return "pages/merge";
	}

	/**
	 * @param model
	 * @param name
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
		model.addAttribute("details", this.commitService.getExistingCommitDetails(uuid));

		return "pages/details";
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

		return "pages/prepare";
	}
}
