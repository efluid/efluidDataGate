package fr.uem.efluid.cucumber.steps;

import fr.uem.efluid.cucumber.common.CucumberStepDefs;
import fr.uem.efluid.services.types.*;
import io.cucumber.datatable.DataTable;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.junit.Assert;

import java.util.*;

import static fr.uem.efluid.model.entities.IndexAction.REMOVE;
import static org.assertj.core.api.Assertions.assertThat;

// @Ignore // Means it will be ignored by junit start, but will be used by cucumber
@SuppressWarnings("unchecked")
public class DependenciesStepDefs extends CucumberStepDefs {

    @When("^the user request a dependency analysis between \"(.*)\" and \"(.*)\"$")
    public void when_dependency_compute_start(String commitAName, String commitBName) throws Exception {

        List<CommitEditData> commits = getCurrentSpecifiedPropertyList("commits", CommitEditData.class);

        Optional<CommitEditData> commitA = commits.stream().filter(c -> c.getComment().equals(commitAName)).findFirst();
        Optional<CommitEditData> commitB = commits.stream().filter(c -> c.getComment().equals(commitBName)).findFirst();

        assertThat(commitA).isPresent();
        assertThat(commitB).isPresent();

        get("/ui/commits/compare/" + commitA.get().getUuid() + "?with=" + commitB.get().getUuid());
    }

    @When("^the dependency analysis has been completed$")
    public void when_dependency_compute_completed() throws Exception {

        // We must wait ...
        while (this.commitService.getCurrentCommitCompareState().getStatus() == CommitCompareStatus.COMPARE_RUNNING) {
            Thread.sleep(10);
        }

        // Must be ok for next step
        assertThat(this.commitService.getCurrentCommitCompareState().getStatus()).isEqualTo(CommitCompareStatus.COMPLETED);

        // Go to result page
        get(getCorrespondingLinkForPageName("dependency page"));
    }

    @When("^the user select the history for dependency on key \"(.*)\" for table \"(.*)\"$")
    public void when_dependency_history(String key, String tableName) throws Exception {

        CommitCompareResult result = getCurrentSpecifiedProperty("compare", CommitCompareResult.class);

        Optional<DictionaryEntrySummary> dict = result.getReferencedTables().values().stream().filter(e -> e.getTableName().equals(tableName)).findFirst();

        assertThat(dict).isPresent();

        get(getCorrespondingLinkForPageName("dependency history") + "?dict=" + dict.get().getUuid() + "&key=" + key);
    }

    @Then("^a dependency analysis is running$")
    public void a_dependency_compute_is_running() {
        Assert.assertEquals(CommitCompareStatus.COMPARE_RUNNING, this.commitService.getCurrentCommitCompareState().getStatus());
    }

    @Then("^these dependencies are identified between the commits :$")
    public void then_dependency_content(DataTable data) {

        CommitCompareResult result = getCurrentSpecifiedProperty("compare", CommitCompareResult.class);

        assertThat(result.getStatus()).isEqualTo(CommitCompareStatus.COMPLETED);

        // Result is compliant to a standard diff
        assertDiffContentIsCompliant(result, data);
    }

    @Then("^this history is provided for the selected dependency :$")
    public void then_dependency_history(DataTable data) {

        //| Commit   | Commit type | Change type | Change payload                                                            |
        List<CommitCompareHistoryEntry> history = getCurrentSpecifiedPropertyList("history", CommitCompareHistoryEntry.class);

        List<Map<String, String>> datas = data.asMaps();
        assertThat(history.size()).isEqualTo(data.asMaps().size());

        for (int i = 0; i < datas.size(); i++) {

            Map<String, String> line = datas.get(i);
            CommitCompareHistoryEntry entry = history.get(i);

            assertThat(entry.getCommitDetails().getComment()).isEqualTo(line.get("Commit"));
            assertThat(entry.getCommitDetails().getState().name()).isEqualTo(line.get("Commit type"));

            assertThat(entry.getAction().name()).isEqualTo(line.get("Change type"));

            if (entry.getAction() != REMOVE) {
                assertThat(entry.getHrPayload()).isEqualTo(line.get("Change payload"));
            }
        }
    }

}
