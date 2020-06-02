package fr.uem.efluid.web;

import static org.springframework.web.bind.annotation.RequestMethod.POST;

import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import fr.uem.efluid.services.SecurityService;

/**
 * <p>
 * For user management, listing ...
 * </p>
 * 
 * @author elecomte
 * @since v0.0.1
 * @version 1
 */
@Controller
@RequestMapping("/ui")
public class UserController extends CommonController {

	@Autowired
	private SecurityService secu;

	/**
	 * @param model
	 * @return
	 */
	@RequestMapping("/user")
	public String currentUser(Model model) {

		if (!controlSelectedProject(model)) {
			return REDIRECT_SELECT;
		}

		// Current user details
		model.addAttribute("user", this.secu.getCurrentUserDetails());
		model.addAttribute("currentLocationTitle", "Mon profil");
		model.addAttribute("projectName", this.projectManagementService.getProjectNameSubstring(0,10));

		return "pages/user_me";
	}

	/**
	 * @param model
	 * @return
	 */
	@RequestMapping("/user/new")
	public String newUser(Model model) {

		if (!controlSelectedProject(model)) {
			return REDIRECT_SELECT;
		}

		model.addAttribute("projects", this.projectManagementService.getAllProjects());

		return "pages/user_new";
	}

	/**
	 * @param model
	 * @param login
	 * @return
	 */
	@RequestMapping("/user/{login}")
	public String selectedUser(Model model, @PathVariable("login") String login) {

		if (!controlSelectedProject(model)) {
			return REDIRECT_SELECT;
		}

		model.addAttribute("projects", this.projectManagementService.getAllProjects());

		// Current user details
		model.addAttribute("user", this.secu.getUserDetails(login));

		return "pages/user_edit";
	}

	/**
	 * @param model
	 * @param login
	 * @param email
	 * @param password
	 * @param prefered
	 * @return
	 */
	@RequestMapping(value = "/users/add", method = POST)
	public String addUser(
			Model model,
			@RequestParam("login") String login,
			@RequestParam("email") String email,
			@RequestParam("password") String password,
			@RequestParam("prefered") List<UUID> prefered) {

		this.secu.createUser(login, email, password, prefered);

		return allUsers(model);
	}

	/**
	 *
	 * @param model
	 * @param login
	 * @param email
	 * @param prefered
	 * @return
	 */
	@RequestMapping(value = "/users/update", method = POST)
	public String updateUser(
			Model model,
			@RequestParam("login") String login,
			@RequestParam("email") String email,
			@RequestParam("prefered") List<UUID> prefered) {

		this.secu.editUser(login, email, prefered);

		return allUsers(model);
	}

	/**
	 * @param model
	 * @return
	 */
	@RequestMapping("/users")
	public String allUsers(Model model) {

		if (!controlSelectedProject(model)) {
			return REDIRECT_SELECT;
		}

		// Current user details
		model.addAttribute("users", this.secu.getAllUserDetails());

		model.addAttribute("canCreate", !this.secu.canPreloadUserOnly());
		model.addAttribute("currentLocationTitle", "Éditer les utilisateurs");
		model.addAttribute("isNavBold", true);
		model.addAttribute("projectName", this.projectManagementService.getProjectNameSubstring(0,10));

		return "pages/users";
	}
}
