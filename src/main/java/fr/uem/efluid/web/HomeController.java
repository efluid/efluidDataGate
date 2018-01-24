package fr.uem.efluid.web;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import fr.uem.efluid.services.ApplicationDetailsService;

@Controller
public class HomeController {

    @Autowired
    private ApplicationDetailsService applicationDetailsService;

    @RequestMapping("/")
    String index(Model model){

        model.addAttribute("details", this.applicationDetailsService.getCurrentDetails());

        return "index";
    }

}
