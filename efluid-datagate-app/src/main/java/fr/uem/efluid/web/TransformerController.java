package fr.uem.efluid.web;

import fr.uem.efluid.services.TransformerService;
import fr.uem.efluid.services.types.TransformerDefEditData;
import fr.uem.efluid.utils.WebUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.UUID;

import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.POST;

/**
 * For all TransformerDef features. Includes a basic configuration validation entry point
 *
 * @author elecomte
 * @version 1
 * @since v1.2.0
 */
@Controller
@RequestMapping("/ui")
public class TransformerController extends CommonController {

    @Autowired
    private TransformerService transformerService;

    /**
     * @param model spring mvc ctx
     * @return template for all transformers listing
     */
    @RequestMapping("/transformers")
    public String allTransformersPage(Model model) {

        if (!controlSelectedProject(model)) {
            return REDIRECT_SELECT;
        }

        // For formatting
        WebUtils.addTools(model);

        model.addAttribute("transformerTypes", this.transformerService.getAllTransformerTypes());
        model.addAttribute("transformerDefs", this.transformerService.getAllTransformerDefs(false));
        model.addAttribute("currentLocationTitle", "Transformateurs du projet");
        model.addAttribute("projectName", this.projectManagementService.getCurrentSelectedProjectShortName());

        return "pages/transformers_all";
    }

    /**
     * @param model           spring mvc ctx
     * @param transformerType selected type
     * @return edit template
     */
    @RequestMapping(path = "/transformers/new", method = POST)
    public String transformerAddNewForType(Model model, @RequestParam String transformerType) {

        if (!controlSelectedProject(model)) {
            return REDIRECT_SELECT;
        }

        model.addAttribute("def", this.transformerService.prepareNewTransformerDef(transformerType));

        return "pages/transformers_edit";
    }

    /**
     * @param model spring mvc ctx
     * @param uuid  selected transformer def id
     * @return edit template
     */
    @RequestMapping(path = "/transformers/edit/{uuid}")
    public String transformerEdit(Model model, @PathVariable("uuid") UUID uuid) {

        if (!controlSelectedProject(model)) {
            return REDIRECT_SELECT;
        }

        model.addAttribute("def", this.transformerService.editTransformerDef(uuid));
        model.addAttribute("projectName", this.projectManagementService.getCurrentSelectedProjectShortName());

        return "pages/transformers_edit";
    }

    /**
     * @param model spring mvc ctx
     * @param uuid  selected transformer def id
     * @return edit template
     */
    @RequestMapping(path = "/transformers/delete/{uuid}")
    public String transformerDelete(Model model, @PathVariable("uuid") UUID uuid) {

        this.transformerService.deleteTransformerDef(uuid);

        return allTransformersPage(model);
    }


    /**
     * @param type   the transformer type for specified config
     * @param config configuration content to validate
     * @return validation result. If null = no error. Details on errors else
     */
    @RequestMapping(path = "/transformers/validate/{type}", method = POST)
    @ResponseBody
    public String transformerValidateConfig(@PathVariable("type") String type, @RequestBody String config) {
        return this.transformerService.validateConfiguration(type, decode(config));
    }

    /**
     * @param type the transformer type for specified config
     * @return default config as a formated raw json
     */
    @RequestMapping(path = "/transformers/reset/{type}", method = GET)
    @ResponseBody
    public String transformerResetConfig(@PathVariable("type") String type) {
        return this.transformerService.getDefaultConfigRawJson(type);
    }

    /**
     * @param model    spring mvc ctx
     * @param editData transformer content spec to save
     * @return template for transformer listing
     */
    @RequestMapping(path = "/transformers/save", method = POST)
    public String transformerSave(Model model, @ModelAttribute @Valid TransformerDefEditData editData) {

        this.transformerService.saveTransformerDef(editData);

        return allTransformersPage(model);
    }
}
