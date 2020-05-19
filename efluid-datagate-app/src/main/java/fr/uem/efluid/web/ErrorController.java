package fr.uem.efluid.web;

import fr.uem.efluid.services.types.ProjectData;
import fr.uem.efluid.utils.ApplicationException;
import fr.uem.efluid.utils.ErrorType;
import fr.uem.efluid.utils.WebUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * @author elecomte
 * @version 1
 * @since v0.0.1
 */
@ControllerAdvice(basePackageClasses = CommonController.class)
public class ErrorController extends CommonController {

    private static final Logger LOGGER = LoggerFactory.getLogger(ErrorController.class);

    /**
     * Custom error display
     *
     * @param e
     * @param model
     * @return
     */
    @ExceptionHandler(Throwable.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public String exception(final Throwable e, final Model model) {

        LOGGER.error("Unprocessed exception found", e);

        model.addAttribute("error", e);

        // Includes project (required, even if none specified)
        if(!controlSelectedProject(model)){
            model.addAttribute(PROJECT_ATTR, new ProjectData());
        }

        // Code : for message display (can use payload)
        if (e instanceof ApplicationException) {
            ApplicationException ext = (ApplicationException) e;
            model.addAttribute("code", ext.getError());
            model.addAttribute("payload", ext.getPayload());
            model.addAttribute("timestamp", ext.getTimestamp());

            WebUtils.addTools(model);

        } else {
            model.addAttribute("code", ErrorType.OTHER);
            model.addAttribute("payload", null);

            // Code + timestamp : unique ID for error. Allows to find error in logs
            model.addAttribute("timestamp", System.currentTimeMillis());
        }

        return "error";
    }
}
