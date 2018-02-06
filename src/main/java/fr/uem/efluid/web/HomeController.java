package fr.uem.efluid.web;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import fr.uem.efluid.services.ApplicationDetailsService;

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
 * @since v0.0.1
 * @version 1
 */
@Controller
public class HomeController {

	@Autowired
	private ApplicationDetailsService applicationDetailsService;

	@RequestMapping("/")
	public String index(Model model) {

		model.addAttribute("details", this.applicationDetailsService.getCurrentDetails());

		return "index";
	}

}
