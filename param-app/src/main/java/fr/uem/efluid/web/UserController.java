package fr.uem.efluid.web;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

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
public class UserController {

	@Autowired
	private SecurityService secu;

	@RequestMapping("/user")
	public String currentUser(Model model) {

		// Current user details
		model.addAttribute("user", this.secu.getCurrentUserDetails());

		return "pages/user";
	}
}
