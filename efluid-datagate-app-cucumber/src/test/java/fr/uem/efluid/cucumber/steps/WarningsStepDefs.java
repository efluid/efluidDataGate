package fr.uem.efluid.cucumber.steps;

import fr.uem.efluid.ColumnType;
import fr.uem.efluid.cucumber.common.CucumberStepDefs;
import fr.uem.efluid.model.AnomalyContextType;
import fr.uem.efluid.model.entities.*;
import fr.uem.efluid.model.metas.ManagedModelDescription;
import fr.uem.efluid.services.AnomalyAndWarningService;
import fr.uem.efluid.services.types.VersionData;
import fr.uem.efluid.utils.DataGenerationUtils;
import io.cucumber.datatable.DataTable;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
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
}
