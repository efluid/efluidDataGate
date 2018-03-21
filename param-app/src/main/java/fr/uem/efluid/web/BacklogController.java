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
import fr.uem.efluid.services.CommitService;
import fr.uem.efluid.services.PilotableCommitPreparationService;
import fr.uem.efluid.services.types.LocalPreparedDiff;
import fr.uem.efluid.services.types.PilotedCommitPreparation;
import fr.uem.efluid.services.types.PilotedCommitStatus;
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
 * @version 1
 */
@Controller
@RequestMapping("/ui")
public class BacklogController {

	@Autowired
	private CommitService commitService;

	@Autowired
	private PilotableCommitPreparationService pilotableCommitService;

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
		return this.pilotableCommitService.getCurrentCommitPreparation().getStatus();
	}

	/**
	 * @param model
	 * @param name
	 * @return
	 */
	@RequestMapping(path = { "/prepare/commit", "/merge/commit" }, method = POST)
	public String preparationLocalCommitPage(Model model, @ModelAttribute PilotedCommitPreparation<LocalPreparedDiff> preparation) {

		// Update current preparation with selected attributes
		this.pilotableCommitService.copyCommitPreparationSelections(preparation);

		// Get updated preparation
		model.addAttribute("preparation", this.pilotableCommitService.getCurrentCommitPreparation());

		return "pages/commit";
	}

	/**
	 * @param model
	 * @param name
	 * @return
	 */
	@RequestMapping(path = { "/prepare/save", "/merge/save" }, method = POST)
	public String preparationLocalSave(Model model, @ModelAttribute PilotedCommitPreparation<LocalPreparedDiff> preparation) {

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

		// For formatting
		WebUtils.addTools(model);

		// Get updated commits
		model.addAttribute("commits", this.commitService.getAvailableCommits());

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

		model.addAttribute("result", this.pilotableCommitService.startMergeCommitPreparation(WebUtils.inputExportImportFile(request)));

		return "pages/merging";
	}

	/**
	 * @param model
	 * @param request
	 * @return
	 */
	@RequestMapping(value = "/pull/upload", method = GET)
	public String processImport(Model model) {

		model.addAttribute("preparation", this.pilotableCommitService.getCurrentCommitPreparation());

		return "pages/merging";
	}

	// /**
	// * <p>
	// * For default merge page
	// * </p>
	// *
	// * @param model
	// * @return
	// */
	// @RequestMapping("/merge")
	// public String mergePage(Model model) {
	//
	// model.addAttribute("preparation",
	// this.pilotableCommitService.getCurrentCommitPreparation());
	//
	// // Default : don't show all
	// model.addAttribute("showAll", Boolean.FALSE);
	//
	// return "pages/merge";
	// }

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

		model.addAttribute("preparation", this.pilotableCommitService.getCurrentCommitPreparation());

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
