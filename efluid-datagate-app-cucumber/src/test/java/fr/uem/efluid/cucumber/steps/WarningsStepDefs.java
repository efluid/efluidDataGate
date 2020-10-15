package fr.uem.efluid.cucumber.steps;

import com.fasterxml.jackson.core.type.TypeReference;
import fr.uem.efluid.ColumnType;
import fr.uem.efluid.cucumber.common.CucumberStepDefs;
import fr.uem.efluid.model.AnomalyContextType;
import fr.uem.efluid.model.entities.*;
import fr.uem.efluid.model.metas.ManagedModelDescription;
import fr.uem.efluid.rest.v1.model.AnomalyView;
import fr.uem.efluid.services.AnomalyAndWarningService;
import fr.uem.efluid.services.types.VersionData;
import fr.uem.efluid.utils.DataGenerationUtils;
import io.cucumber.datatable.DataTable;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

//@Ignore // Means it will be ignored by junit start, but will be used by cucumber
public class WarningsStepDefs extends CucumberStepDefs {

    @Autowired
    private AnomalyAndWarningService anomalyAndWarningService;

    @When("^the user asks for the merge anomalies rest service for the exported commit \"(.*)\"$")
    public void user_access_to_anomalies(String commit) throws Throwable {

        // Then go to page
        String link = getCorrespondingLinkForPageName("merge anomalies rest service");

        get(link + "?name=" + currentExports.get(commit).getResult().getFilename());
    }

    @Then("^these warnings are recorded for the merge of export of commit \"(.*)\":$")
    public void then_warnings(String commit, DataTable dataTable) {

        List<Anomaly> anomalies = this.anomalyAndWarningService.getAnomaliesForContext(AnomalyContextType.MERGE, getNamedExportOrSingleCurrentOne(commit).getResult().getFilename());

        var data = dataTable.asMaps();
        assertThat(anomalies).hasSize(data.size());

        data.forEach(l -> {
            String pattern = "On [" + l.get("Table") + "." + l.get("Key") + "] : ";
            var found = anomalies.stream().filter(a -> a.getMessage().startsWith(pattern)).collect(Collectors.toList());
            assertThat(found).describedAs("Anomaly message for " + l.get("Table") + "." + l.get("Key") + " should be unique").hasSize(1);
            assertThat(found.get(0).getMessage().substring(pattern.length())).describedAs("Anomaly message for " + l.get("Table") + "." + l.get("Key")).isEqualTo(l.get("Message"));
            assertThat(found.get(0).getCode()).describedAs("Anomaly message for " + l.get("Table") + "." + l.get("Key")).isEqualTo(l.get("Code"));
        });

    }


    @Then("^there are the commit with listed merge anomalies:$")
    public void there_are_the_listed_merge_anomaly_types(DataTable data) throws Throwable {

        assertRequestWasOk();

        String json = currentAction.andReturn().getResponse().getContentAsString();

        List<String> result = mapper.readValue(json, new TypeReference<List<String>>() {
        });
        List<Map<String, String>> expected = data.asMaps(String.class, String.class);

        assertThat(result).hasSize(expected.size());
        assertThat(result).contains(expected.stream()
                .map(v -> v.get("exported commit"))
                .map(currentExports::get)
                .map(e -> e.getResult().getFilename())
                .toArray(String[]::new));
    }

    @Then("^there are the listed merge anomalies:$")
    public void there_are_the_listed_merge_anomalies(DataTable data) throws Throwable {

        assertRequestWasOk();

        String json = currentAction.andReturn().getResponse().getContentAsString();

        List<AnomalyView> result = mapper.readValue(json, new TypeReference<List<AnomalyView>>() {
        });
        List<Map<String, String>> expected = data.asMaps(String.class, String.class);


        expected.forEach(l -> {
            String pattern = "On [" + l.get("Table") + "." + l.get("Key") + "] : ";
            var found = result.stream().filter(a -> a.getMessage().startsWith(pattern)).collect(Collectors.toList());
            assertThat(found).describedAs("Anomaly message for " + l.get("Table") + "." + l.get("Key") + " should be unique").hasSize(1);
            assertThat(found.get(0).getMessage().substring(pattern.length())).describedAs("Anomaly message for " + l.get("Table") + "." + l.get("Key")).isEqualTo(l.get("Message"));
            assertThat(found.get(0).getCode()).describedAs("Anomaly message for " + l.get("Table") + "." + l.get("Key")).isEqualTo(l.get("Code"));
        });
    }
}
