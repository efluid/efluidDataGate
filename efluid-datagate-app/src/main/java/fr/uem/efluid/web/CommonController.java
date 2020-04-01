package fr.uem.efluid.web;

import fr.uem.efluid.services.ProjectManagementService;
import fr.uem.efluid.services.types.ProjectData;
import fr.uem.efluid.utils.ApplicationException;
import fr.uem.efluid.utils.ErrorType;
import fr.uem.efluid.utils.FormatUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ui.Model;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

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

    protected static String decode(String raw) {
        try {
            return URLDecoder.decode(raw, FormatUtils.CONTENT_ENCODING.name());
        } catch (UnsupportedEncodingException e) {
            throw new ApplicationException(ErrorType.OTHER, e);
        }
    }
}
