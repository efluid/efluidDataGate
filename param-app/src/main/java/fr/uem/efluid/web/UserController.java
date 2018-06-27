package fr.uem.efluid.web;

import static org.springframework.web.bind.annotation.RequestMethod.POST;
import static org.springframework.web.bind.annotation.RequestMethod.PUT;

import java.util.List;
import java.util.UUID;

import javax.websocket.server.PathParam;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
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
	public String selectedUser(Model model, @PathParam("login") String login) {

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
	@RequestMapping(value = "/users", method = PUT)
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
	 * @param model
	 * @param login
	 * @param email
	 * @param password
	 * @param prefered
	 * @return
	 */
	@RequestMapping(value = "/users", method = POST)
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

		return "pages/users";
	}
}
