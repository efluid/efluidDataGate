package fr.uem.efluid.web;

import static org.springframework.web.bind.annotation.RequestMethod.POST;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import fr.uem.efluid.services.DictionaryManagementService;
import fr.uem.efluid.services.types.DictionaryEntryEditData;
import fr.uem.efluid.services.types.FunctionalDomainData;

/**
 * <p>
 * Routing and model init for everything related to dictionary management : Functional
 * domain management, dictionary listing / edit, processing of associated import / export.
 * Templating is managed with Thymeleaf.
 * </p>
 * <p>
 * Can be seen as the "provider of all 'DICTIONARY' features for the parameter management"
 * </p>
 * 
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

		return "dictionary";
	}

	/**
	 * @param model
	 * @return
	 */
	@RequestMapping("/dictionary/new")
	public String dictionaryAddNew(Model model) {

		model.addAttribute("tables", this.dictionaryManagementService.getSelectableTables());

		return "table_init";
	}

	/**
	 * @param model
	 * @return
	 */
	@RequestMapping("/dictionary/refresh")
	public String dictionaryAddNewWithRefresh(Model model) {

		this.dictionaryManagementService.refreshCachedMetadata();

		model.addAttribute("tables", this.dictionaryManagementService.getSelectableTables());

		return "table_init";
	}

	/**
	 * @param model
	 * @param name
	 * @return
	 */
	@RequestMapping("/dictionary/edit/{uuid}")
	public String dictionaryEdit(Model model, @PathVariable("uuid") UUID uuid) {

		model.addAttribute("tables", this.dictionaryManagementService.getSelectableTables());
		model.addAttribute("domains", this.dictionaryManagementService.getAvailableFunctionalDomains());
		model.addAttribute("entry", this.dictionaryManagementService.editEditableDictionaryEntry(uuid));

		return "table_edit";
	}

	/**
	 * @param model
	 * @param name
	 * @return
	 */
	@RequestMapping("/dictionary/new/{name}")
	public String dictionaryAddNewForTable(Model model, @PathVariable("name") String name) {

		model.addAttribute("tables", this.dictionaryManagementService.getSelectableTables());
		model.addAttribute("domains", this.dictionaryManagementService.getAvailableFunctionalDomains());
		model.addAttribute("entry", this.dictionaryManagementService.prepareNewEditableDictionaryEntry(name));

		return "table_edit";
	}

	/**
	 * @param model
	 * @param name
	 * @return
	 */
	@RequestMapping(path = "/dictionary/save", method = POST)
	public String dictionarySave(Model model, @ModelAttribute DictionaryEntryEditData editData) {

		this.dictionaryManagementService.saveDictionaryEntry(editData);

		// For success save message
		model.addAttribute("from", "success_edit");

		return dictionaryPage(model);
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
	public void removeFunctionalDomainData(@PathVariable("uuid") UUID uuid) {
		this.dictionaryManagementService.deleteFunctionalDomain(uuid);
	}
}
