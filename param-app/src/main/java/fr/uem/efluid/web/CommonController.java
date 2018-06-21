package fr.uem.efluid.web;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ui.Model;

import fr.uem.efluid.services.ProjectManagementService;
import fr.uem.efluid.services.types.ProjectData;

/**
 * @author elecomte
 * @since v0.0.8
 * @version 1
 */
public abstract class CommonController {

	protected static final String REDIRECT_SELECT = "redirect:/ui/projects/select";
	
	@Autowired
	protected ProjectManagementService projectManagementService;

	/**
	 * @param model
	 * @return
	 */
	protected boolean controlSelectedProject(Model model) {

		ProjectData current = this.projectManagementService.getCurrentSelectedProject();

		if (current != null) {
			model.addAttribute("project", current);
			return true;
		}

		return false;
	}
}
