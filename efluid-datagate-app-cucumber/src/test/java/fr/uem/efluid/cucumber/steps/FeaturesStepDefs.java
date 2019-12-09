package fr.uem.efluid.cucumber.steps;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import fr.uem.efluid.cucumber.common.CucumberStepDefs;
import io.cucumber.datatable.DataTable;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author elecomte
 * @version 1
 * @since v0.0.8
 */
//@Ignore // Means it will be ignored by junit start, but will be used by cucumber
public class FeaturesStepDefs extends CucumberStepDefs {

    @Autowired
    private ObjectMapper mapper;

    @Given("^the feature \"(.*)\" is (.*)$")
    public void then_feature_state(String feature, String action) throws Exception {
        post("/rest/v1/features/" + action.substring(0, action.length() - 1) + "/" + feature);
        assertRequestWasOk();
    }

    @Then("^there are the listed features:$")
    public void there_are_the_listed_features(DataTable data) throws Throwable {

        assertRequestWasOk();

        String json = currentAction.andReturn().getResponse().getContentAsString();

        Map<String, String> result = mapper.readValue(json, new TypeReference<Map<String, String>>() {
        });
        List<Map<String, String>> expected = data.asMaps(String.class, String.class);

        assertThat(result).hasSize(expected.size());
        assertThat(result.keySet()).contains(expected.stream().map(v -> v.get("feature")).toArray(String[]::new));

        result.forEach((k, v) -> {
            Map<String, String> expectedLine = expected.stream().filter(e -> e.get("feature").equals(k)).findFirst().orElseThrow();
            assertThat(v).isEqualTo(String.valueOf(expectedLine.get("state").equals("enabled")));
        });

    }
}