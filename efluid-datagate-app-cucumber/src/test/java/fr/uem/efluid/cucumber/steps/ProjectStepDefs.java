package fr.uem.efluid.cucumber.steps;

import fr.uem.efluid.cucumber.common.CucumberStepDefs;
import fr.uem.efluid.model.entities.Project;
import fr.uem.efluid.model.repositories.ProjectRepository;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.springframework.beans.factory.annotation.Autowired;

import static org.junit.Assert.*;

import java.net.URLEncoder;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ProjectStepDefs extends CucumberStepDefs {
    private static List<String> specifiedProjects;
    @Autowired
    private ProjectRepository projects;

    @Given("^the existing projects \"(.*)\"$")
    public void the_existing_projects(String projectRaw) throws Throwable {
        List<String> projects = Stream.of(projectRaw.split(", ")).collect(Collectors.toList());

        // Implicit init with default domain / project
        initMinimalWizzardData();

        // Implicit authentication and on page
        implicitlyAuthenticatedAndOnPage("projects page");

        // Init with specified versions
        modelDatabase().getAllProjects();
        // Keep version for update check
        specifiedProjects = projects;

    }

    @When("^the user change the name of project \"(.*)\" to \"(.*)\"")
    public void the_user_change_project_name(String oldName, String newName) throws Throwable {
        Project project = modelDatabase().findProjectByName(oldName);

        project.setName(newName);

        project = this.projects.save(project);

        assertEquals(newName, project.getName());
    }

    @Then("^the name of project \"(.*)\" is updated$")
    public void the_update_date_of_version_is_updated(String name) {
        assertEquals(name, this.projects.findByName(name).getName());
    }



}
