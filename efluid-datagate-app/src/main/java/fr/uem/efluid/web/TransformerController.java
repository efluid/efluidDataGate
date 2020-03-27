package fr.uem.efluid.web;

import fr.uem.efluid.services.TransformerService;
import fr.uem.efluid.utils.WebUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * For all TransformerDef features
 */
@Controller
@RequestMapping("/ui")
public class TransformerController extends CommonController {

    @Autowired
    private TransformerService transformerService;

    @RequestMapping("/transformers")
    public String allTransformersPage(Model model) {

        if (!controlSelectedProject(model)) {
            return REDIRECT_SELECT;
        }

        // For formatting
        WebUtils.addTools(model);

        model.addAttribute("transformerTypes", this.transformerService.getAllTransformerTypes());
        model.addAttribute("transformerDefs", this.transformerService.getAllTransformerDefs());

        return "pages/transformers_all";
    }
}
