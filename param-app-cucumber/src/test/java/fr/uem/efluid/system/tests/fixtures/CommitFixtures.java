package fr.uem.efluid.system.tests.fixtures;

import cucumber.api.DataTable;
import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import fr.uem.efluid.services.PilotableCommitPreparationService;
import fr.uem.efluid.services.types.DiffDisplay;
import fr.uem.efluid.services.types.PilotedCommitPreparation;
import fr.uem.efluid.services.types.PilotedCommitStatus;
import fr.uem.efluid.services.types.PreparedIndexEntry;
import fr.uem.efluid.system.common.SystemTest;
import org.junit.Assert;
import org.junit.Before;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.*;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

/**
 * @author elecomte
 * @version 1
 * @since v0.0.8
 */
public class CommitFixtures extends SystemTest {

    private static UUID runningPrep;

    private static String select = null;
    @Autowired
    private PilotableCommitPreparationService prep;

    @Before
    public void resetFixture() {
        resetAsyncProcess();
        runningPrep = null;
    }

    @Given("^a diff analysis can be started and completed$")
    public void a_diff_analysis_can_be_started_and_completed() {

        // Reset select
        select = null;

        // Full dic init
        initCompleteDictionaryWith3Tables();

        // Authenticated
        implicitlyAuthenticatedAndOnPage("home page");

        // Valid version ready
        modelDatabase().initVersions(getCurrentUserProject(), Collections.singletonList("vCurrent"), 0);
    }


    @Given("^a diff analysis can be started$")
    public void a_diff_analysis_can_be_started() {

        // Standard diff position start
        a_diff_analysis_can_be_started_and_completed();

        // Mark async to "eternal" process
        mockEternalAsyncProcess();
    }

    @Given("^a diff analysis have been started and completed$")
    public void a_diff_analysis_have_been_started_and_completed() throws Throwable {

        // Ready
        a_diff_analysis_can_be_started_and_completed();

        // Started
        a_diff_has_already_been_launched();

        // Completed
        a_diff_is_completed();
    }

    @Given("^no diff is running$")
    public void no_diff_is_running() {
        this.prep.cancelCommitPreparation();
    }

    @Given("^a diff has already been launched$")
    public void a_diff_has_already_been_launched() throws Throwable {

        // Cancel anything running
        no_diff_is_running();

        // Start new
        get(getCorrespondingLinkForPageName("diff launch page"));

        // And get diff uuid for testing
        runningPrep = this.prep.getCurrentCommitPreparation().getIdentifier();
    }

    @Given("^the diff is completed$")
    public void a_diff_is_completed() throws InterruptedException {

        // We must wait ...
        while (this.prep.getCurrentCommitPreparationState().getStatus() == PilotedCommitStatus.DIFF_RUNNING) {
            Thread.sleep(10);
        }

        // Must be ok for next step
        assertThat(this.prep.getCurrentCommitPreparationState().getStatus()).isEqualTo(PilotedCommitStatus.COMMIT_CAN_PREPARE);
    }

    @When("^the user accesses to preparation commit page$")
    public void the_preparation_is_saved_by_user() throws Throwable {

        // Get provided data to post them updated
        PilotedCommitPreparation<?> preparation = this.prep.getCurrentCommitPreparation();

        // Various cases
        if(select != null && select.equals("ALL")){
            preparation.getDomains().forEach(d -> d.getPreparedContent().forEach(c -> c.getDiff().forEach(l -> {
               l.setSelected(true);
               l.setRollbacked(false);
            })));
        }

        // Post prepared data
        postObject(getCorrespondingLinkForPageName("commit saving page"), "preparation", preparation);
    }

    @When("^the user do not select any prepared diff content for commit$")
    public void user_select_no_content() {
        // Default nothing
    }

    @When("^the user select all prepared diff content for commit$")
    public void user_select_all_content() throws Exception {

        // Call select all uri
        post("/prepare/selection/all", p("selected", "true"), p("rollbacked", "false"));

        select = "ALL";
    }

    @Then("^a diff is running$")
    public void a_diff_is_running() {

        Assert.assertEquals(PilotedCommitStatus.DIFF_RUNNING, this.prep.getCurrentCommitPreparationState().getStatus());
    }

    @Then("^an alert says that the diff is still running$")
    public void an_alert_says_that_the_diff_is_still_running() throws Throwable {

        // Alert => We are on running diff page
        currentAction = currentAction.andExpect(view().name(getCorrespondingTemplateForName("diff running")));

        // The running preparation is still the previous one
        Assert.assertEquals(runningPrep, this.prep.getCurrentCommitPreparation().getIdentifier());
    }

    /*
            | Table    | Key | Payload |
            | TTAB_ONE | 1   | eee     |
            | TTAB_ONE | 2   | eee     |
            | TTAB_ONE | 3   | eee     |
    */
    @Then("^the commit content is rendered with these identified changes :$")
    public void commit_content_ready(DataTable data) {

        // Get by tables
        Map<String, List<Map<String, String>>> tables = data.asMaps(String.class, String.class).stream().collect(Collectors.groupingBy(i -> i.get("Table")));

        PilotedCommitPreparation<?> preparation = this.prep.getCurrentCommitPreparation();

        assertThat(preparation.getStatus()).isEqualTo(PilotedCommitStatus.COMMIT_CAN_PREPARE);
        assertThat(preparation.getTotalCount()).isEqualTo(data.asMaps(String.class, String.class).size());
        assertThat(preparation.getDomains().size()).isEqualTo(1);

        tables.forEach((t, v) -> {
            DiffDisplay<?> content = preparation.getDomains().get(0).getPreparedContent().stream()
                    .filter(p -> p.getDictionaryEntryTableName().equals(t))
                    .findFirst().orElseThrow(() -> new AssertionError("Cannot find corresponding diff for table " + t));

            assertThat(content.getDiff().size()).isEqualTo(v.size());

            content.getDiff().sort(Comparator.comparing(PreparedIndexEntry::getKeyValue));
            v.sort(Comparator.comparing(m -> m.get("Key")));

            // Keep order
            for (int i = 0; i < content.getDiff().size(); i++) {
                PreparedIndexEntry diffLine = content.getDiff().get(i);
                Map<String, String> dataLine = v.get(i);

                assertThat(diffLine.getKeyValue()).isEqualTo(dataLine.get("Key"));
                assertThat(diffLine.getHrPayload()).isEqualTo(dataLine.get("Payload"));
            }
        });
    }

    @Then("^the commit comment is empty$")
    public void commit_data_comment_empty() {
        PilotedCommitPreparation<?> preparation = this.prep.getCurrentCommitPreparation();

        assertThat(preparation).isNotNull();
        assertThat(preparation.getCommitData().getComment()).isNull();
    }

    @Then("^all the diff preparation content is ignored by default$")
    public void commit_preparation_not_selected() {
        PilotedCommitPreparation<?> preparation = this.prep.getCurrentCommitPreparation();

        assertThat(preparation).isNotNull();
        preparation.getDomains().forEach(d -> d.getPreparedContent().forEach(c -> c.getDiff().forEach(l -> {
            assertThat(l.isSelected()).isFalse();
            assertThat(l.isRollbacked()).isFalse();
        })));
    }

    @Then("^all the diff preparation content is selected for commit$")
    public void commit_preparation_all_selected() {
        PilotedCommitPreparation<?> preparation = this.prep.getCurrentCommitPreparation();

        assertThat(preparation).isNotNull();
        preparation.getDomains().forEach(d -> d.getPreparedContent().forEach(c -> c.getDiff().forEach(l -> {
            assertThat(l.isSelected()).isTrue();
            assertThat(l.isRollbacked()).isFalse();
        })));
    }

}
