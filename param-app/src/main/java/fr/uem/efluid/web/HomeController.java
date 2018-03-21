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

		// If not configured (no data : forward to wizzard)
		if (this.applicationDetailsService.isNeedWizzard()) {
			return "forward:/wizzard/";
		}

		model.addAttribute("details", this.applicationDetailsService.getCurrentDetails());

		return "pages/index";
	}

	@RequestMapping("/swagger")
	public String swagger() {
		return "redirect:/webjars/swagger-ui/2.2.10/index.html?url=/v2/api-docs";
	}

}
