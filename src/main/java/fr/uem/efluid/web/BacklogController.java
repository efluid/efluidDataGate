package fr.uem.efluid.web;

import static org.springframework.web.bind.annotation.RequestMethod.GET;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import fr.uem.efluid.services.DictionaryManagementService;
import fr.uem.efluid.services.PilotableCommitPreparationService;
import fr.uem.efluid.services.PilotableCommitPreparationService.PilotedCommitPreparation;
import fr.uem.efluid.services.PilotableCommitPreparationService.PilotedCommitStatus;
import fr.uem.efluid.services.types.PreparedIndexEntry;

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
	private PilotableCommitPreparationService commitService;

	@Autowired
	private DictionaryManagementService dictionaryManagementService;

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
		return this.commitService.getCurrentCommitPreparation().getStatus();
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
		PilotedCommitPreparation<?> prepare = this.commitService.startLocalCommitPreparation(forced);

		// Diff already in progress : dedicated waiting page
		if (prepare.getStatus() == PilotedCommitStatus.DIFF_RUNNING) {
			return "diff";
		}

		// Completed / available, basic prepare page
		model.addAttribute("preparation", prepare);

		// Need dictionary and domains for filtering
		model.addAttribute("dictionary", this.dictionaryManagementService.getDictionnaryEntrySummaries());
		model.addAttribute("domains", this.dictionaryManagementService.getAvailableFunctionalDomains());

		return "prepare";
	}
}
