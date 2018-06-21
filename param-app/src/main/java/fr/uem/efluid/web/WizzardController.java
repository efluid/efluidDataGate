package fr.uem.efluid.web;

import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.POST;

import java.util.Arrays;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import fr.uem.efluid.services.ApplicationDetailsService;
import fr.uem.efluid.services.DictionaryManagementService;
import fr.uem.efluid.services.PilotableCommitPreparationService;
import fr.uem.efluid.services.ProjectManagementService;
import fr.uem.efluid.services.SecurityService;
import fr.uem.efluid.services.types.PilotedCommitStatus;
import fr.uem.efluid.services.types.ProjectData;
import fr.uem.efluid.utils.WebUtils;

/**
 * <p>
 * For all wizzard features : allows to specify minimal config + prepare the 1st mandatory
 * initial commit
 * </p>
 * 
 * @author elecomte
 * @since v0.0.1
 * @version 1
 */
@Controller
@RequestMapping("/wizzard")
public class WizzardController {

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
	public String welcome() {

		return "wizzard/welcome";
	}

	/**
	 * @return
	 */
	@RequestMapping(path = "/1", method = GET)
	public String userPage() {

		return "wizzard/user";
	}

	/**
	 * @param login
	 * @param password
	 * @param email
	 * @return
	 */
	@RequestMapping(path = "/1", method = POST)
	public String userSave(
			@RequestParam("login") String login,
			@RequestParam("password") String password,
			@RequestParam("email") String email) {

		this.security.addSimpleUser(login, email, password, true);

		return "wizzard/projects";
	}

	/**
	 * @param login
	 * @param password
	 * @param email
	 * @return
	 */
	@RequestMapping(path = "/2", method = POST)
	public String projectSave(@RequestParam("selected") String selected) {

		UUID uuid = UUID.fromString(selected);

		// Select is automatically set as prefered for current user
		this.projectService.setPreferedProjectsForCurrentUser(Arrays.asList(uuid));

		// And as selected
		this.projectService.selectProject(uuid);

		return "wizzard/initial_dictionary";
	}

	/**
	 * Rest Method for AJAX push
	 * 
	 * @param name
	 * @return
	 */
	@RequestMapping(value = "/2/add/{name}", method = POST)
	@ResponseBody
	public ProjectData addProjectData(@PathVariable("name") String name) {
		return this.projectService.createNewProject(name);
	}

	/**
	 * @param model
	 * @param domainName
	 * @return
	 */
	@RequestMapping(value = "/3", method = GET)
	public String initialCommitPage(Model model) {

		model.addAttribute("dictionary", this.dictionaryManagementService.getDictionnaryEntrySummaries());

		return "wizzard/initial_commit";
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

		model.addAttribute("preparation", this.pilotableCommitService.startLocalCommitPreparation(true));

		return initialCommitPage(model);
	}

	/**
	 * @return status as a value
	 */
	@RequestMapping(path = { "/3/progress" }, method = GET)
	@ResponseBody
	public PilotedCommitStatus preparationGetStatus() {
		return this.pilotableCommitService.getCurrentCommitPreparationStatus();
	}

	/**
	 * @param model
	 * @param commitName
	 * @return
	 */
	@RequestMapping(value = "/3/commit", method = GET)
	public String completedInitialCommit(Model model, @RequestParam("commitName") String commitName) {

		// Finalize dedicated for wizzard initial commit (auto select all)
		this.pilotableCommitService.finalizeInitialCommitPreparation(commitName);

		// And auto-select all content
		model.addAttribute("preparation", this.pilotableCommitService.getCurrentCommitPreparation());
		this.pilotableCommitService.saveCommitPreparation();

		return initialCommitPage(model);
	}

	/**
	 * @return
	 */
	@RequestMapping(value = "/4", method = GET)
	public String completedWizzard() {

		// Finalize temp wizzard data
		this.security.completeWizzardUserMode();
		this.detailsService.completeWizzard();

		return "wizzard/completed";
	}
}
