package fr.uem.efluid.web;

import static org.springframework.web.bind.annotation.RequestMethod.POST;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import fr.uem.efluid.services.types.ProjectData;
import fr.uem.efluid.utils.WebUtils;

/**
 * <p>
 * Routing and model init for everything related to project management and selection
 * </p>
 * <p>
 * Projects can be seen as GIT repositories
 * </p>
 * 
 * @author elecomte
 * @since v0.2.0
 * @version 1
 */
@Controller
@RequestMapping("/ui")
public class ProjectController extends CommonController {

	@RequestMapping("/projects")
	public String allProjectsPage(Model model) {

		if (!controlSelectedProject(model)) {
			return REDIRECT_SELECT;
		}

		model.addAttribute("projects", this.projectManagementService.getAllProjects());

		return "pages/projects_all";
	}

	/**
	 * Rest Method for AJAX push
	 * 
	 * @param name
	 * @return
	 */
	@RequestMapping(value = "/projects/add/{name}/{color}", method = POST)
	@ResponseBody
	public ProjectData addProjectData(@PathVariable("name") String name, @PathVariable("color") int color) {
		return this.projectManagementService.createNewProject(name, color);
	}

	/**
	 * @param model
	 * @return
	 */
	@RequestMapping("/projects/prefered")
	public String preferedProjectsPage(Model model) {

		if (!controlSelectedProject(model)) {
			return REDIRECT_SELECT;
		}

		// For formatting
		WebUtils.addTools(model);
		model.addAttribute("prefereds", this.projectManagementService.getPreferedProjectsForCurrentUser());
		model.addAttribute("projects", this.projectManagementService.getAllProjects());

		return "pages/projects_pref";
	}

	/**
	 * @param model
	 * @param name
	 * @return
	 */
	@RequestMapping(path = "/projects/prefered/save", method = POST)
	public String preferedProjectsSave(Model model, @RequestParam List<UUID> prefered) {

		if (!controlSelectedProject(model)) {
			return REDIRECT_SELECT;
		}

		this.projectManagementService.setPreferedProjectsForCurrentUser(prefered);

		// For success save message
		model.addAttribute("from", "success_edit");

		return preferedProjectsPage(model);
	}

	/**
	 * @param model
	 * @return
	 */
	@RequestMapping("/projects/select")
	public String selectCurrentProjectPage(Model model) {

		model.addAttribute("prefereds", this.projectManagementService.getPreferedProjectsForCurrentUser());
		model.addAttribute("project", this.projectManagementService.getCurrentSelectedProject());

		return "pages/projects_select";
	}

	/**
	 * @param model
	 * @param name
	 * @return
	 */
	@RequestMapping("/projects/select/{uuid}")
	public String selectProject(Model model, @PathVariable("uuid") String uuid) {

		this.projectManagementService.selectProject(UUID.fromString(uuid));

		return selectCurrentProjectPage(model);
	}
}
