package fr.uem.efluid.web;

import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.POST;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import fr.uem.efluid.services.CommitService;
import fr.uem.efluid.services.PilotableCommitPreparationService;
import fr.uem.efluid.services.PilotableCommitPreparationService.PilotedCommitPreparation;
import fr.uem.efluid.services.PilotableCommitPreparationService.PilotedCommitStatus;
import fr.uem.efluid.services.types.LocalPreparedDiff;

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
@RequestMapping
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
	@RequestMapping(value = "/prepare/progress", method = GET)
	@ResponseBody
	public PilotedCommitStatus preparationGetStatus() {
		return this.pilotableCommitService.getCurrentCommitPreparation().getStatus();
	}

	/**
	 * @param model
	 * @param name
	 * @return
	 */
	@RequestMapping(path = "/prepare/commit", method = POST)
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
	@RequestMapping(path = "/prepare/save", method = POST)
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
	@RequestMapping(path = "/prepare/cancel", method = GET)
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
	@RequestMapping(path = "/commits", method = GET)
	public String commitListPage(Model model) {

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
			return "pages/diff";
		}

		// Completed / available, basic prepare page
		model.addAttribute("preparation", prepare);

		return "pages/prepare";
	}
}
