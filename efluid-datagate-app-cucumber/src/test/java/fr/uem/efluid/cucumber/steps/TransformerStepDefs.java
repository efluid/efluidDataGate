package fr.uem.efluid.cucumber.steps;

import fr.uem.efluid.cucumber.common.CucumberStepDefs;
import fr.uem.efluid.model.entities.Project;
import fr.uem.efluid.model.entities.TransformerDef;
import fr.uem.efluid.services.types.CommitExportEditData;
import fr.uem.efluid.services.types.TransformerDefDisplay;
import fr.uem.efluid.services.types.TransformerDefEditData;
import fr.uem.efluid.services.types.TransformerType;
import fr.uem.efluid.tools.Transformer;
import io.cucumber.datatable.DataTable;
import io.cucumber.docstring.DocString;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.junit.Before;
import org.junit.Ignore;

import java.util.*;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author elecomte
 * @version 1
 * @since v1.2.0
 */
@Ignore // Means it will be ignored by junit start, but will be used by cucumber
public class TransformerStepDefs extends CucumberStepDefs {

    private static Map<String, List<TransformerDefDisplay>> specifiedTransformers;

    private static TransformerDefEditData currentEdit;

    @Before
    public void resetFixture() {
        specifiedTransformers = null;
        currentEdit = null;
    }

    @Given("^the configured transformers for project \"(.*)\" :$")
    public void the_existing_transformers(String projectName, DataTable table) {

        specifiedTransformers = new HashMap<>();

        // Full dic init
        if (CommonStepDefs.efluidCase) {
            initCompleteDictionaryWithEfluidTestTables();
        } else {
            initCompleteDictionaryWith7Tables();
        }

        CommonStepDefs.efluidCase = false;

        // Valid version ready
        modelDatabase().initVersions(getCurrentUserProject(), Collections.singletonList("vCurrent"), 0);

        // Implicit authentication and on page
        implicitlyAuthenticatedAndOnPage("the home page");

        Project project = modelDatabase().findProjectByName(projectName);

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

    @Given("^the user has initialized a new transformer of type \"(.*)\", with name \"(.*)\" and this configuration :$")
    public void init_transformer(String typeName, String name, DocString config) throws Exception {

        // Implicit wizard init
        initMinimalWizzardData();

        // Implicit authentication
        implicitlyAuthenticatedAndOnPage("list of transformers");

        Transformer<?, ?> transformer = getTransformerByName(typeName);
        post(getCorrespondingLinkForPageName("new transformer page") + "?transformerType=" + transformer.getClass().getSimpleName());

        currentEdit = getCurrentSpecifiedProperty("def", TransformerDefEditData.class);
        currentEdit.setName(name);
        currentEdit.setConfiguration(config.getContent());
    }

    @When("^the user select a transformer type \"(.*)\" to add$")
    public void add_transformer(String typeName) throws Exception {
        Transformer<?, ?> transformer = getTransformerByName(typeName);
        post(getCorrespondingLinkForPageName("new transformer page") + "?transformerType=" + transformer.getClass().getSimpleName());
    }

    @When("^the user .*save the transformer$")
    public void save_try_transformer() throws Exception {

        // Attribute push of properties
        post("/ui/transformers/save",
                p("name", currentEdit.getName()),
                p("type", currentEdit.getType()),
                p("uuid", currentEdit.getUuid() != null ? currentEdit.getUuid().toString() : null),
                p("priority", String.valueOf(currentEdit.getPriority())),
                p("configuration", currentEdit.getConfiguration())
        );

        // Drop current (for other saves)
        currentEdit = null;
    }
    @When("^the user customize the transformer \"(.*)\" with this configuration :$")
    public void customize_export_transformer(String name, DocString config) throws Exception {
        CommitExportEditData exportEditData = getCurrentSpecifiedProperty("exportEdit", CommitExportEditData.class);
        List<TransformerDefDisplay> transformers = getCurrentSpecifiedPropertyList("transformerDefs", TransformerDefDisplay.class);

        Optional<TransformerDefDisplay> transformerDefDisplay = transformers.stream().filter(t -> t.getName().equals(name)).findFirst();

        assertThat(transformerDefDisplay).isPresent();

        UUID truid = transformerDefDisplay.get().getUuid();

        if(exportEditData.getSpecificTransformerConfigurations() ==null){
            exportEditData.setSpecificTransformerConfigurations(new HashMap<>());
        }

        exportEditData.getSpecificTransformerConfigurations().put(truid, config.getContent());
    }

    @Then("^the (.*) configured transformers from project \"(.*)\" are displayed$")
    public void the_x_transformers_are_displayed(String nbr, String projectName) {

        assertModelIsSpecifiedListWithProperties(
                "transformerDefs",
                Integer.parseInt(nbr),
                p -> (TransformerDefDisplay) p,
                specifiedTransformers.get(projectName));
    }

    @Then("^the available transformer types are :$")
    public void the_transformer_types_are_listed(DataTable table) {
        List<TransformerType> types = table.asMaps().stream()
                .map(l -> new TransformerType(l.get("type"), l.get("name")))
                .collect(Collectors.toList());

        assertModelIsSpecifiedListWithProperties(
                "transformerTypes",
                types.size(),
                p -> (TransformerType) p,
                types);
    }

    @Then("^the transformer definition configuration is :$")
    public void the_transformer_def_config_is(DocString config) {

        assertModelIsSpecifiedProperty(
                "def",
                TransformerDefEditData.class,
                t -> jsonEquals(t.getConfiguration(), config.getContent()));
    }

    @Then("^the transformer with name \"(.*)\" of type \"(.*)\" exists$")
    public void expected_transformer(String name, String typeName) {
        Optional<TransformerDef> def = modelDatabase()
                .findTransformerDefByProjectAndNameAndType(
                        getCurrentUserProject(),
                        name,
                        getTransformerByName(typeName));

        assertThat(def).isPresent();
        assertThat(def.get().getUuid()).isNotNull();
    }
}
