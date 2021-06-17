package fr.uem.efluid.web;

import fr.uem.efluid.services.*;
import fr.uem.efluid.services.types.PreparationState;
import fr.uem.efluid.services.types.ProjectData;
import fr.uem.efluid.utils.WebUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import java.util.UUID;

import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.POST;

/**
 * <p>
 * For all wizard features : allows to specify minimal config + prepare the 1st mandatory
 * initial commit
 * </p>
 *
 * @author elecomte
 * @version 1
 * @since v0.0.1
 */
@Controller
@RequestMapping("/wizard")
public class WizardController {

    @Autowired
    private ApplicationDetailsService applicationDetailsService;

    @Autowired
    private DictionaryManagementService dictionaryManagementService;

    @Autowired
    private PilotableCommitPreparationService pilotableCommitService;

    @Autowired
    private SecurityService security;

    @Autowired
    private ApplicationDetailsService detailsService;

    @Autowired
    private ProjectManagementService projectService;

    /**
     * @return
     */
    @RequestMapping(path = "/", method = GET)
    public String welcome(Model model) {

        model.addAttribute("info", this.applicationDetailsService.getInfo());

        return "wizard/welcome";
    }

    /**
     * @return
     */
    @RequestMapping(path = "/1", method = GET)
    public String userPage(Model model) {

        model.addAttribute("externalAuth", this.security.canPreloadUserOnly());

        return "wizard/user";
    }

    /**
     * @param login
     * @param password
     * @param email
     * @return
     */
    @RequestMapping(path = "/1", method = POST)
    public String userSave(
            Model model,
            @RequestParam("login") String login,
            @RequestParam("password") String password,
            @RequestParam(name = "email", required = false) String email) {

        // Cannot create - auth error
        if (this.security.addSimpleUser(login, email, password, true) == null) {

            model.addAttribute("error", new Object());

            return userPage(model);
        }

        return "wizard/projects";
    }

    /**
     * @param selected
     * @return
     */
    @RequestMapping(path = "/2", method = POST)
    public String projectSave(Model model, @RequestParam("selected") String selected) {

        UUID uuid = UUID.fromString(selected);

        // User already has all created projects as prefered

        // Select is automatically marked as selected
        this.projectService.selectProject(uuid);

        model.addAttribute(CommonController.PROJECT_ATTR, this.projectService.getCurrentSelectedProject());

        return "wizard/initial_dictionary";
    }

    /**
     * Rest Method for AJAX push
     *
     * @param name
     * @return
     */
    @RequestMapping(value = "/2/add/{name}/{color}", method = POST)
    @ResponseBody
    public ProjectData addProjectData(@PathVariable("name") String name, @PathVariable("color") int color) {
        return this.projectService.createNewProject(name, color);
    }

    /**
     * @param model
     * @return
     */
    @RequestMapping(value = "/3", method = GET)
    public String initialCommitPage(Model model) {

        model.addAttribute(CommonController.PROJECT_ATTR, this.projectService.getCurrentSelectedProject());
        model.addAttribute("availableProjects", this.projectService.getAllProjects());
        model.addAttribute("dictionaryExists", this.dictionaryManagementService.isDictionnaryExists());

        return "wizard/initial_commit";
    }

    /**
     * @param model
     * @param domainName
     * @return
     */
    @RequestMapping(value = "/3/create", method = POST)
    public String addFunctionalDomainData(Model model, @RequestParam("domainName") String domainName) {

        this.dictionaryManagementService.createNewFunctionalDomain(domainName);

        return initialCommitPage(model);
    }

    /**
     * @param model
     * @param request
     * @return
     */
    @RequestMapping(value = "/3/upload", method = POST)
    public String uploadDictionary(Model model, MultipartHttpServletRequest request) {

        this.dictionaryManagementService.importAll(WebUtils.inputExportImportFile(request));

        return initialCommitPage(model);
    }

    /**
     * @param model
     * @return
     */
    @RequestMapping(value = "/3/init", method = GET)
    public String startInitialCommit(Model model) {

        model.addAttribute("preResult", this.pilotableCommitService.startWizzardLocalCommitPreparation());

        return initialCommitPage(model);
    }

    /**
     * @return status as a value
     */
    @RequestMapping(path = {"/3/progress"}, method = GET)
    @ResponseBody
    public PreparationState preparationGetState() {
        return this.pilotableCommitService.getAllCommitPreparationStates();
    }

    /**
     * @param model
     * @param commitName
     * @return
     */
    @RequestMapping(value = "/3/commit", method = GET)
    public String completedInitialCommit(Model model, @RequestParam("commitName") String commitName) {

        // Finalize dedicated for wizard initial commit (auto select all)
        this.pilotableCommitService.finalizeWizzardCommitPreparation(commitName);

        // And auto-select all content
        model.addAttribute("preResult", this.pilotableCommitService.saveWizzardCommitPreparations());

        return initialCommitPage(model);
    }

    /**
     * @param model
     * @param request
     * @return
     */
    @RequestMapping(value = "/3/pull", method = POST)
    public String uploadDiffImport(Model model, MultipartHttpServletRequest request, @RequestParam("selectedProject") UUID selectedProject) {

        this.projectService.selectProject(selectedProject);

        model.addAttribute("preResult",
                this.pilotableCommitService.startWizzardImportCommitPreparation(WebUtils.inputExportImportFile(request), selectedProject));

        return initialCommitPage(model);
    }

    /**
     * @return
     */
    @RequestMapping(value = "/4", method = GET)
    public String completedWizzard(Model model) {

        model.addAttribute(CommonController.PROJECT_ATTR, this.projectService.getCurrentSelectedProject());

        // Finalize temp wizard data
        this.security.completeWizzardUserMode();
        this.detailsService.completeWizard();

        return "wizard/completed";
    }
}
