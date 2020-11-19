package fr.uem.efluid.web;

import fr.uem.efluid.model.entities.Project;
import fr.uem.efluid.services.types.ProjectData;
import fr.uem.efluid.utils.WebUtils;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

import static org.springframework.web.bind.annotation.RequestMethod.POST;

/**
 * <p>
 * Routing and model init for everything related to project management and selection
 * </p>
 * <p>
 * Projects can be seen as GIT repositories
 * </p>
 *
 * @author elecomte
 * @version 1
 * @since v0.2.0
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
        model.addAttribute("currentLocationTitle", "Gestion des projets");
        model.addAttribute("projectName", this.projectManagementService.getCurrentSelectedProjectShortName());
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
     * Rest Method for AJAX push
     *
     * @param oldNameProject
     * @param newNameProject
     * @param uuidProject
     * @return
     */
    @RequestMapping(value = "/projects/modif/{oldNameProject}/{uuidProject}/{newNameProject}", method = POST)
    @ResponseBody
    public Project modifProjectData(@PathVariable("oldNameProject") String oldNameProject, @PathVariable("newNameProject") String newNameProject,  @PathVariable("uuidProject") UUID uuidProject) {
        return this.projectManagementService.updateNameProject(oldNameProject, newNameProject, uuidProject);
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
        model.addAttribute("currentLocationTitle", "Mes projets préférés");
        model.addAttribute("projectName", this.projectManagementService.getCurrentSelectedProjectShortName());

        return "pages/projects_pref";
    }

    /**
     * @param model
     * @param prefered
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
        model.addAttribute("projectName", this.projectManagementService.getCurrentSelectedProjectShortName());

        return "pages/projects_select";
    }

    /**
     * @param model
     * @param uuid
     * @return
     */
    @RequestMapping("/projects/select/{uuid}")
    public String selectProject(Model model, @PathVariable("uuid") String uuid) {

        if (this.projectManagementService.selectProject(UUID.fromString(uuid))) {
            return "redirect:/ui";
        }

        return selectCurrentProjectPage(model);
    }
}
