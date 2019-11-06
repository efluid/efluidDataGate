package fr.uem.efluid.web;

import fr.uem.efluid.services.ApplicationDetailsService;
import fr.uem.efluid.utils.WebUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * <p>
 * Routing and model init home page and associated login / logout features. Templating is
 * managed with Thymeleaf.
 * </p>
 * <p>
 * Can be seen as the "provider of everything not related to core features for the
 * parameter management"
 * </p>
 *
 * @author elecomte
 * @version 2
 * @since v0.0.1
 */
@Controller
public class HomeController extends CommonController {

    @Autowired
    private ApplicationDetailsService applicationDetailsService;

    /**
     * @return
     */
    @RequestMapping("/")
    public String index() {

        // If not configured (no data : forward to wizard)
        if (this.applicationDetailsService.isNeedWizard()) {
            return "forward:/wizard/";
        }

        return "redirect:/ui";
    }

    /**
     * @param model
     * @return
     */
    @RequestMapping("/ui")
    public String index(Model model) {

        // If not configured (no data : forward to wizard)
        if (this.applicationDetailsService.isNeedWizard()) {
            return "forward:/wizard/";
        }

        if (!controlSelectedProject(model)) {
            return REDIRECT_SELECT;
        }

        WebUtils.addTools(model);

        model.addAttribute("details", this.applicationDetailsService.getCurrentDetails());

        return "pages/index";
    }

    /**
     * <p>
     * Authentication error
     * </p>
     *
     * @param username
     * @param error
     * @return
     */
    @RequestMapping("/login")
    public String loginForm(Model model,
                            @RequestParam(name = "username", required = false) String username,
                            @RequestParam(name = "error", required = false) String error) {

        // If not configured (no data : forward to wizard)
        if (this.applicationDetailsService.isNeedWizard()) {
            return "forward:/wizard/";
        }

        if (username != null) {
            model.addAttribute("error", username);
        }

        if (error != null) {
            model.addAttribute("error", error);
        }

        model.addAttribute("info", this.applicationDetailsService.getInfo());

        return "pages/login";
    }

    /**
     * @return
     */
    @RequestMapping("/swagger")
    public String swagger() {
        return "redirect:/webjars/swagger-ui/2.2.10/index.html?url=/v2/api-docs";
    }
}
