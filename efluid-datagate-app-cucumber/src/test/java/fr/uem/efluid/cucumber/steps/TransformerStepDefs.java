package fr.uem.efluid.cucumber.steps;

import fr.uem.efluid.cucumber.common.CucumberStepDefs;
import fr.uem.efluid.model.entities.Project;
import fr.uem.efluid.services.TransformerService;
import fr.uem.efluid.services.types.TransformerDefDisplay;
import fr.uem.efluid.tools.Transformer;
import io.cucumber.datatable.DataTable;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import org.junit.Before;
import org.junit.Ignore;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author elecomte
 * @version 1
 * @since v1.2.0
 */
@Ignore // Means it will be ignored by junit start, but will be used by cucumber
public class TransformerStepDefs extends CucumberStepDefs {

    private static Map<String, List<TransformerDefDisplay>> specifiedTransformers;

    @Autowired
    private TransformerService transformerService;

    @Before
    public void resetFixture() {
        specifiedTransformers = null;
    }

    @Given("^the configured transformers for project \"(.*)\" :$")
    public void the_existing_versions(String projectName, DataTable table) {

        Project project = modelDatabase().findProjectByName(projectName);

        specifiedTransformers = new HashMap<>();

        // Implicit init with default domain / project
        initMinimalWizzardData();

        // Implicit authentication and on page
        implicitlyAuthenticatedAndOnPage("the home page");

        table.asMaps()
                .forEach(s -> {
                    Transformer<?, ?> transformer = getTransformerByName(s.get("type"));
                    TransformerDefDisplay def = modelDatabase().initTransformer(
                            project,
                            s.get("name"),
                            transformer,
                            s.get("configuration"),
                            Integer.parseInt(s.get("priority")));

                    specifiedTransformers.computeIfAbsent(projectName, (k) -> new ArrayList<>()).add(def);
                });
    }

    @Then("the (\\d*) configured transformers from project \"(.*)\" are displayed")
    public void the_x_versions_are_displayed(int nbr, String projectName) {

        assertModelIsSpecifiedListWithProperties(
                "transformerDefs",
                nbr,
                p -> (TransformerDefDisplay) p,
                specifiedTransformers.get(projectName));
    }

    private Transformer<?, ?> getTransformerByName(String name) {
        String type = this.transformerService.getAllTransformerTypes().stream()
                .filter(t -> t.getName().equals(name)).findFirst()
                .orElseThrow(() -> new AssertionError("Invalid transformer name " + name))
                .getType();

        return this.transformerService.loadTransformerByType(type);
    }
}
