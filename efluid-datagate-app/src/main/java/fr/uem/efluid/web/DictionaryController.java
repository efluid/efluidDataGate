package fr.uem.efluid.web;

import fr.uem.efluid.model.repositories.FeatureManager;
import fr.uem.efluid.services.ApplicationDetailsService;
import fr.uem.efluid.services.DictionaryManagementService;
import fr.uem.efluid.services.Feature;
import fr.uem.efluid.services.types.*;
import fr.uem.efluid.utils.WebUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import javax.validation.Valid;
import java.util.UUID;

import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.POST;

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
 * @version 1
 * @since v0.0.1
 */
@Controller
@RequestMapping("/ui")
public class DictionaryController extends CommonController {

    @Autowired
    private DictionaryManagementService dictionaryManagementService;

    @Autowired
    private ApplicationDetailsService applicationDetailsService;

    @Autowired
    private FeatureManager features;

    @RequestMapping("/versions/refresh")
    public String versionsPageRefreshModel(Model model) {


        // Use versions page
        return versionsPage(model);
    }

    @RequestMapping("/versions")
    public String versionsPage(Model model) {

        if (!controlSelectedProject(model)) {
            return REDIRECT_SELECT;
        }

        // For formatting
        WebUtils.addTools(model);

        model.addAttribute("currentLocationTitle", "Gestion des versions");
        model.addAttribute("modelDesc", this.applicationDetailsService.getCurrentModelId());
        model.addAttribute("versions", this.dictionaryManagementService.getAvailableVersions());
        model.addAttribute("dictionaryManagementService", this.dictionaryManagementService);
        model.addAttribute("checkVersion", this.dictionaryManagementService.isDictionaryUpdatedAfterLastVersion());
        model.addAttribute("canCreateVersion", this.dictionaryManagementService.isVersionCanCreate());
        model.addAttribute("projectName", this.projectManagementService.getCurrentSelectedProjectShortName());


        // If we use the model ID as version name, the name can be ignored / hidden
        model.addAttribute("noVersionName", this.features.isEnabled(Feature.USE_MODEL_ID_AS_VERSION_NAME));

        return "pages/versions";
    }

    /**
     * Rest Method for AJAX push
     *
     * @param
     * @return
     */

    @RequestMapping(value = "/versions/remove/{uuid}", method = POST)
    @ResponseBody
    public void deleteVersion(@PathVariable("uuid") UUID uuid) {
        this.dictionaryManagementService.deleteVersionById(uuid);
    }


    @RequestMapping("/domains")
    public String domainsPage(Model model) {

        if (!controlSelectedProject(model)) {
            return REDIRECT_SELECT;
        }

        model.addAttribute("domains", this.dictionaryManagementService.getAvailableFunctionalDomains());
        model.addAttribute("projectName", this.projectManagementService.getCurrentSelectedProjectShortName());

        return "pages/domains";
    }

    @RequestMapping("/dictionary")
    public String dictionaryPage(Model model) {

        if (!controlSelectedProject(model)) {
            return REDIRECT_SELECT;
        }

        model.addAttribute("dictionary", this.dictionaryManagementService.getDictionnaryEntrySummaries());
        model.addAttribute("version", this.dictionaryManagementService.getLastVersion());
        model.addAttribute("projectName", this.projectManagementService.getCurrentSelectedProjectShortName());

        return "pages/dictionary";
    }

    /**
     * @param model
     * @return
     */
    @RequestMapping("/dictionary/new")
    public String dictionaryAddNew(Model model) {

        if (!controlSelectedProject(model)) {
            return REDIRECT_SELECT;
        }

        model.addAttribute("tables", this.dictionaryManagementService.getSelectableTables());
        model.addAttribute("currentLocationTitle", "Modifier le dictionnaire");

        return "pages/table_init";
    }

    /**
     * @param model
     * @return
     */
    @RequestMapping("/dictionary/refresh")
    public String dictionaryAddNewWithRefresh(Model model) {

        if (!controlSelectedProject(model)) {
            return REDIRECT_SELECT;
        }

        this.dictionaryManagementService.refreshCachedMetadata();

        model.addAttribute("tables", this.dictionaryManagementService.getSelectableTables());

        return "pages/table_init";
    }

    /**
     * @param model
     * @param uuid
     * @return
     */
    @RequestMapping("/dictionary/edit/{uuid}")
    public String dictionaryEdit(Model model, @PathVariable("uuid") UUID uuid) {

        if (!controlSelectedProject(model)) {
            return REDIRECT_SELECT;
        }

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

        if (!controlSelectedProject(model)) {
            return REDIRECT_SELECT;
        }

        model.addAttribute("tables", this.dictionaryManagementService.getSelectableTables());
        model.addAttribute("domains", this.dictionaryManagementService.getAvailableFunctionalDomains());
        model.addAttribute("entry", this.dictionaryManagementService.prepareNewEditableDictionaryEntry(name));

        return "pages/table_edit";
    }

    @RequestMapping(path = "/dictionary/clause", method = POST)
    public String dictionaryApplyFilterClause(Model model, @RequestParam String clause) {

        if (!controlSelectedProject(model)) {
            return REDIRECT_SELECT;
        }

        model.addAttribute("notCompatibleTables", this.dictionaryManagementService.checkAndApplySelectClauseForAllDictionary(clause));

        // For success apply message
        model.addAttribute("from", "success_clause");

        return dictionaryPage(model);
    }

    /**
     * @param model
     * @return
     */
    @RequestMapping("/dictionary/refresh/all")
    public String dictionaryRefreshAllTableMetadata(Model model) {

        if (!controlSelectedProject(model)) {
            return REDIRECT_SELECT;
        }

        // Refresh
        this.dictionaryManagementService.refreshCachedMetadata();

        model.addAttribute("tables", this.dictionaryManagementService.getSelectableTables());
        model.addAttribute("domains", this.dictionaryManagementService.getAvailableFunctionalDomains());

        return "pages/table_init";
    }

    /**
     * @param model
     * @param name
     * @return
     */
    @RequestMapping("/dictionary/refresh/one/{name}/{uuid}")
    public String dictionaryRefreshTableMetadata(Model model, @PathVariable("uuid") String uuid, @PathVariable("name") String name) {

        if (!controlSelectedProject(model)) {
            return REDIRECT_SELECT;
        }

        // Refresh
        this.dictionaryManagementService.refreshCachedMetadataForOneTable(name);

        model.addAttribute("tables", this.dictionaryManagementService.getSelectableTables());
        model.addAttribute("domains", this.dictionaryManagementService.getAvailableFunctionalDomains());

        if (StringUtils.hasText(uuid) && !"null".equals(uuid)) {
            // Existing
            model.addAttribute("entry", this.dictionaryManagementService.editEditableDictionaryEntry(UUID.fromString(uuid)));
        } else {
            // New
            model.addAttribute("entry", this.dictionaryManagementService.prepareNewEditableDictionaryEntry(name));
        }

        return "pages/table_edit";
    }

    /**
     * REST post process from a processing table edit, to generate a query server-side
     *
     * @param editData
     * @return
     */
    @RequestMapping(path = "/dictionary/querygen", method = POST, consumes = {"application/x-www-form-urlencoded"})
    @ResponseBody
    public String dictionaryGenerateQuery(DictionaryEntryEditData editData) {

        return this.dictionaryManagementService.generateQuery(editData);
    }

    /**
     * @param model
     * @param editData
     * @return
     */
    @RequestMapping(path = "/dictionary/save", method = POST)
    public String dictionarySave(Model model, @ModelAttribute @Valid DictionaryEntryEditData editData) {

        if (!controlSelectedProject(model)) {
            return REDIRECT_SELECT;
        }

        this.dictionaryManagementService.saveDictionaryEntry(editData);

        // For success save message
        model.addAttribute("from", "success_edit");

        return dictionaryPage(model);
    }

    /**
     * <p>
     * Test features of a complete "ingoing" dictionary entry, to validate the
     * corresponding result. Help the user to update the where clause to include the
     * expected results
     * </p>
     *
     * @param editData stale DictionaryEntry details.
     * @return
     */
    @RequestMapping(path = "/dictionary/test", method = POST)
    @ResponseBody
    public TestQueryData dictionaryTestQuery(@Valid DictionaryEntryEditData editData) {
        return this.dictionaryManagementService.testDictionaryEntryExtract(editData);
    }

    /**
     * @param model
     * @return
     */
    @RequestMapping(path = "/share", method = GET)
    public String exportPage(Model model) {

        if (!controlSelectedProject(model)) {
            return REDIRECT_SELECT;
        }

        WebUtils.addTools(model);

        model.addAttribute("domains", this.dictionaryManagementService.getAvailableFunctionalDomains());
        model.addAttribute("modelDesc", this.applicationDetailsService.getCurrentModelId());
        model.addAttribute("version", this.dictionaryManagementService.getLastVersion());
        model.addAttribute("currentLocationTitle", "Exporter/Importer un dictionnaire");
        model.addAttribute("projectName", this.projectManagementService.getCurrentSelectedProjectShortName());

        return "pages/share";
    }

    /**
     * @param uuid
     * @return
     */
    @RequestMapping(value = "/share/{uuid}/{name}.par", method = GET)
    @ResponseBody
    public ResponseEntity<InputStreamResource> downloadExportOneDomain(@PathVariable("uuid") UUID uuid, @PathVariable("name") String name) {

        return WebUtils.outputExportImportFile(name, this.dictionaryManagementService.exportFonctionalDomains(uuid).getResult());
    }

    /**
     * @return
     */
    @RequestMapping(value = "/share/project/{name}.par", method = GET)
    @ResponseBody
    public ResponseEntity<InputStreamResource> downloadExportAllDomainsInCurrentProject(@PathVariable("name") String name) {

        return WebUtils.outputExportImportFile(name, this.dictionaryManagementService.exportCurrentProject().getResult());
    }

    /**
     * @return
     */
    @RequestMapping(value = "/share/all/{name}.par", method = GET)
    @ResponseBody
    public ResponseEntity<InputStreamResource> downloadExportAllDomainsAllProject(@PathVariable("name") String name) {

        return WebUtils.outputExportImportFile(name, this.dictionaryManagementService.exportAll().getResult());
    }

    /**
     * @param model
     * @param request
     * @return
     */
    @RequestMapping(value = "/share/upload", method = POST)
    public String uploadImport(Model model, MultipartHttpServletRequest request, @RequestParam(name = "project",required = false) String project) {

        if (!controlSelectedProject(model)) {
            return REDIRECT_SELECT;
        }

        if (project != null && "current".equals(project)) {
            model.addAttribute("result", this.dictionaryManagementService.importAllInCurrentProject(WebUtils.inputExportImportFile(request)));
        } else {
            model.addAttribute("result", this.dictionaryManagementService.importAll(WebUtils.inputExportImportFile(request)));
        }

        return exportPage(model);
    }


    /**
     * Rest Method for AJAX push
     *
     * @param name
     * @return
     */
    @RequestMapping(value = "/versions/{name}", method = POST)
    @ResponseBody
    public VersionData setVersion(@PathVariable("name") String name) {
        this.dictionaryManagementService.setCurrentVersion(name);
        return this.dictionaryManagementService.getLastVersion();
    }

    /**
     * Update of existing version
     *
     * @param name
     * @return
     */
    @RequestMapping(value = "/versions", method = POST)
    public String updateVersion(Model model, @RequestParam("name") String name) {
        this.dictionaryManagementService.setCurrentVersion(name);
        model.addAttribute("updateDone", Boolean.TRUE);
        return versionsPage(model);
    }

    /**
     * @param model
     * @param name
     * @return
     */
    @GetMapping("/versions/compare/{name}")
    public String compareVersionWithLast(Model model, @PathVariable("name") String name) {

        if (!controlSelectedProject(model)) {
            return REDIRECT_SELECT;
        }

        VersionCompare compare = this.dictionaryManagementService.compareVersionWithLast(name);

        model.addAttribute("compare", compare);

        return "pages/compare";
    }

    /**
     * Rest Method for AJAX push
     *
     * @param name
     * @return
     */
    @RequestMapping(value = "/domains/add/{name}", method = POST)
    @ResponseBody
    public FunctionalDomainData addFunctionalDomainData(@PathVariable("name") String name, Model model) {
        model.addAttribute("currentLocationTitle", "Domaines fonctionnels");
        return this.dictionaryManagementService.createNewFunctionalDomain(name);
    }

    /**
     * Rest Method for AJAX push
     *
     * @param uuid
     */
    @RequestMapping(value = "/domains/remove/{uuid}", method = POST)
    @ResponseBody
    public void removeFunctionalDomainData(@PathVariable("uuid") UUID uuid) {
        this.dictionaryManagementService.deleteFunctionalDomain(uuid);
    }

    /**
     * Rest Method for AJAX push
     *
     * @param uuid
     */
    @RequestMapping(value = "/dictionary/remove/{uuid}", method = POST)
    @ResponseBody
    public void removeDictionaryEntry(@PathVariable("uuid") UUID uuid) {
        this.dictionaryManagementService.deleteDictionaryEntry(uuid);
    }
}
