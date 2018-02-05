package fr.uem.efluid.web;

import static org.springframework.web.bind.annotation.RequestMethod.GET;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import fr.uem.efluid.services.PilotableCommitPreparationService;
import fr.uem.efluid.services.types.PilotedCommitPreparation;
import fr.uem.efluid.services.types.PilotedCommitStatus;

/**
 * @author elecomte
 * @since v0.0.1
 * @version 1
 */
@Controller
@RequestMapping
public class BacklogController {

	@Autowired
	private PilotableCommitPreparationService commitService;

	@RequestMapping("/prepare")
	public String preparePage(Model model) {

		PilotedCommitPreparation prepare = this.commitService.startCommitPreparation();
		
		// Diff already in progress : dedicated waiting page
		if(prepare.getStatus() == PilotedCommitStatus.DIFF_RUNNING){
			return "diff";
		}
		
		// Completed / available, basic prepare page
		model.addAttribute("preparation", prepare);

		return "prepare";
	}

	/**
	 * @return status as a value
	 */
	@RequestMapping(value = "/prepare/progress", method = GET)
	@ResponseBody
	public PilotedCommitStatus checkPreparationStatus() {

		return this.commitService.getCurrentCommitPreparation().getStatus();
	}

}
