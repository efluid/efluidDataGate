package fr.uem.efluid.web;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import fr.uem.efluid.services.PilotableCommitPreparationService;

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

		model.addAttribute("preparation", this.commitService.getCurrentCommitPreparation());

		return "prepare";
	}

	
}
