package fr.uem.efluid.web;

import static org.springframework.web.bind.annotation.RequestMethod.POST;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import fr.uem.efluid.services.DictionaryManagementService;
import fr.uem.efluid.services.types.FunctionalDomainData;

/**
 * @author elecomte
 * @since v0.0.1
 * @version 1
 */
@Controller
@RequestMapping
public class DictionaryController {

	@Autowired
	private DictionaryManagementService dictionaryManagementService;

	@RequestMapping("/domains")
	public String domainsPage(Model model) {

		model.addAttribute("domains", this.dictionaryManagementService.getAvailableFunctionalDomains());

		return "domains";
	}
	

	@RequestMapping("/dictionary")
	public String dictionaryPage(Model model) {

		model.addAttribute("dictionary", this.dictionaryManagementService.getDictionnaryEntrySummaries());
		model.addAttribute("domains", this.dictionaryManagementService.getAvailableFunctionalDomains());

		return "dictionary";
	}

	/**
	 * Rest Method for AJAX push
	 * 
	 * @param name
	 * @return
	 */
	@RequestMapping(value = "/domains/add/{name}", method = POST)
	@ResponseBody
	public FunctionalDomainData addFunctionalDomainData(@PathVariable("name") String name) {
		return this.dictionaryManagementService.createNewFunctionalDomain(name);
	}

	/**
	 * Rest Method for AJAX push
	 * 
	 * @param name
	 * @return
	 */
	@RequestMapping(value = "/domains/remove/{uuid}", method = POST)
	@ResponseBody
	public void addFunctionalDomainData(@PathVariable("uuid") UUID uuid) {
		this.dictionaryManagementService.deleteFunctionalDomain(uuid);
	}
}
