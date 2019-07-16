package fr.uem.efluid.web;

import fr.uem.efluid.services.ProjectManagementService;
import fr.uem.efluid.services.types.ProjectData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ui.Model;

/**
 * @author elecomte
 * @version 1
 * @since v0.0.8
 */
public abstract class CommonController {

    static final String PROJECT_ATTR = "project";

    static final String REDIRECT_SELECT = "redirect:/ui/projects/select";

    @Autowired
    protected ProjectManagementService projectManagementService;

    /**
     * @param model
     * @return
     */
    boolean controlSelectedProject(Model model) {

        ProjectData current = this.projectManagementService.getCurrentSelectedProject();

        if (current != null) {
            model.addAttribute(PROJECT_ATTR, current);
            return true;
        }

        return false;
    }
}
