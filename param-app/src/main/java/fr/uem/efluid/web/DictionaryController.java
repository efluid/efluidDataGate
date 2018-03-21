package fr.uem.efluid.web;

import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.POST;

import java.util.UUID;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import fr.uem.efluid.services.DictionaryManagementService;
import fr.uem.efluid.services.types.DictionaryEntryEditData;
import fr.uem.efluid.services.types.FunctionalDomainData;
import fr.uem.efluid.utils.WebUtils;

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
@RequestMapping("/ui")
public class DictionaryController {

	@Autowired
	private DictionaryManagementService dictionaryManagementService;

	@RequestMapping("/domains")
	public String domainsPage(Model model) {

		model.addAttribute("domains", this.dictionaryManagementService.getAvailableFunctionalDomains());

		return "pages/domains";
	}

	@RequestMapping("/dictionary")
	public String dictionaryPage(Model model) {

		model.addAttribute("dictionary", this.dictionaryManagementService.getDictionnaryEntrySummaries());

		return "pages/dictionary";
	}

	/**
	 * @param model
	 * @return
	 */
	@RequestMapping("/dictionary/new")
	public String dictionaryAddNew(Model model) {

		model.addAttribute("tables", this.dictionaryManagementService.getSelectableTables());

		return "pages/table_init";
	}

	/**
	 * @param model
	 * @return
	 */
	@RequestMapping("/dictionary/refresh")
	public String dictionaryAddNewWithRefresh(Model model) {

		this.dictionaryManagementService.refreshCachedMetadata();

		model.addAttribute("tables", this.dictionaryManagementService.getSelectableTables());

		return "pages/table_init";
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

		return "pages/table_edit";
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

		return "pages/table_edit";
	}

	/**
	 * @param model
	 * @param name
	 * @return
	 */
	@RequestMapping(path = "/dictionary/save", method = POST)
	public String dictionarySave(Model model, @ModelAttribute @Valid DictionaryEntryEditData editData) {

		this.dictionaryManagementService.saveDictionaryEntry(editData);

		// For success save message
		model.addAttribute("from", "success_edit");

		return dictionaryPage(model);
	}

	/**
	 * @param model
	 * @return
	 */
	@RequestMapping(path = "/share", method = GET)
	public String exportPage(Model model) {

		model.addAttribute("domains", this.dictionaryManagementService.getAvailableFunctionalDomains());

		return "pages/share";
	}

	/**
	 * @param uuid
	 * @return
	 */
	@RequestMapping(value = "/share/{uuid}-dictionary.par", method = GET)
	@ResponseBody
	public ResponseEntity<InputStreamResource> downloadExportOneDomain(@PathVariable("uuid") UUID uuid) {

		return WebUtils.outputExportImportFile(this.dictionaryManagementService.exportFonctionalDomains(uuid).getResult());
	}

	/**
	 * @return
	 */
	@RequestMapping(value = "/share/all-dictionary.par", method = GET)
	@ResponseBody
	public ResponseEntity<InputStreamResource> downloadExportAllDomains() {

		return WebUtils.outputExportImportFile(this.dictionaryManagementService.exportAll().getResult());
	}

	/**
	 * @param model
	 * @param request
	 * @return
	 */
	@RequestMapping(value = "/share/upload", method = POST)
	public String uploadImport(Model model, MultipartHttpServletRequest request) {

		model.addAttribute("result", this.dictionaryManagementService.importAll(WebUtils.inputExportImportFile(request)));

		return exportPage(model);
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

	/**
	 * Rest Method for AJAX push
	 * 
	 * @param name
	 * @return
	 */
	@RequestMapping(value = "/dictionary/remove/{uuid}", method = POST)
	@ResponseBody
	public void removeDictionaryEntry(@PathVariable("uuid") UUID uuid) {
		this.dictionaryManagementService.deleteDictionaryEntry(uuid);
	}
}
