package fr.uem.efluid.web;

import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.POST;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import fr.uem.efluid.services.SecurityService;

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
public class WizzardController {

	@Autowired
	private SecurityService security;

	/**
	 * @return
	 */
	@RequestMapping(path = "/wizzard/", method = GET)
	public String welcome() {

		return "wizzard/welcome";
	}

	/**
	 * @return
	 */
	@RequestMapping(path = "/wizzard/1", method = GET)
	public String userPage() {

		return "wizzard/user";
	}

	/**
	 * @param login
	 * @param password
	 * @param email
	 * @return
	 */
	@RequestMapping(path = "/wizzard/1", method = POST)
	public String userSave(
			@RequestParam("login") String login,
			@RequestParam("password") String password,
			@RequestParam("email") String email) {

		this.security.addSimpleUser(login, email, password);

		return "wizzard/dictionary";
	}

}
