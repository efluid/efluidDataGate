package fr.uem.efluid.system.tests.fixtures;

import cucumber.api.DataTable;
import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import fr.uem.efluid.model.entities.Commit;
import fr.uem.efluid.model.entities.CommitState;
import fr.uem.efluid.model.entities.IndexAction;
import fr.uem.efluid.services.PilotableCommitPreparationService;
import fr.uem.efluid.services.types.*;
import fr.uem.efluid.system.common.ImportExportHelper;
import fr.uem.efluid.system.common.SystemTest;
import fr.uem.efluid.utils.FormatUtils;
import org.junit.Assert;
import org.junit.Before;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.*;
import java.util.stream.Collectors;

import static fr.uem.efluid.model.entities.IndexAction.REMOVE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

/**
 * @author elecomte
 * @version 1
 * @since v0.0.8
 */
public class PreparationFixtures extends SystemTest {

    private static UUID runningPrep;

    private static String select = null;

    private static CommitEditData currentCommit;

    @Autowired
    private PilotableCommitPreparationService prep;

    @Before
    public void resetFixture() {
        resetAsyncProcess();
        runningPrep = null;
        currentCommit = null;
    }


    @Given("^a diff analysis can be started and completed$")
    public void a_diff_analysis_can_be_started_and_completed() {

        // Reset select
        select = null;

        // Full dic init
        initCompleteDictionaryWith4Tables();

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

    @Given("^a diff analysis has been started and completed$")
    public void a_diff_analysis_has_been_started_and_completed() throws Throwable {

        // Ready
        a_diff_analysis_can_be_started_and_completed();

        // Started
        a_diff_has_already_been_launched();

        // Completed
        a_diff_is_completed();
    }

    @Given("^a merge diff analysis has been started and completed with the available source package$")
    public void a_merge_diff_analysis_has_been_started_and_completed_from_package() throws Throwable {
        // Import started
        a_merge_diff_has_already_been_launched();

        // Completed
        a_merge_diff_is_completed();


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

    @Given("^a merge diff has already been launched with the available source package$")
    public void a_merge_diff_has_already_been_launched() throws Throwable {

        // Cancel anything running
        no_diff_is_running();

        // Start new merge from available package
        this.prep.startMergeCommitPreparation(PushPullFixtures.currentExport.getResult());

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

    @Given("^the merge diff is completed$")
    public void a_merge_diff_is_completed() throws InterruptedException {

        // We must wait ...
        while (this.prep.getCurrentCommitPreparationState().getStatus() == PilotedCommitStatus.DIFF_RUNNING) {
            Thread.sleep(10);
        }

        // Must be ok for next step
        assertThat(this.prep.getCurrentCommitPreparationState().getStatus()).isEqualTo(PilotedCommitStatus.COMMIT_CAN_PREPARE);
        assertThat(this.prep.getCurrentCommitPreparation().getPreparingState()).isEqualTo(CommitState.MERGED);
    }


    @Given("^the user has selected all content for commit$")
    public void user_has_selected_all_ready_content() throws Throwable {

        // Call select all uri
        post("/prepare/selection/all", p("selected", "true"), p("rollbacked", "false"));

        select = "ALL";

        user_access_preparation_saving_page();
    }
    @Given("^the user has selected all content for merge commit$")
    public void user_has_selected_all_ready_content_for_merge() throws Throwable {

        // Call select all uri
        post("/merge/selection/all", p("selected", "true"), p("rollbacked", "false"));

        select = "ALL";

        user_access_preparation_saving_page();
    }

    @Given("^the user has specified a commit comment \"(.*)\"$")
    public void user_has_defined_commit_comment(String comment) throws Exception {

        currentCommit.setComment(comment);
    }

    @Given("^the user has attached these documents to the commit:$")
    public void user_had_specified_attachments(DataTable table) {

        table.asMaps(String.class, String.class).forEach(m -> {
            ExportFile export = ImportExportHelper.generateFile(Integer.parseInt(m.get("size")), m.get("title"));
            this.prep.addAttachmentOnCurrentCommitPreparation(export);
        });
    }

    @Given("^the commit \"(.*)\" has been saved with all the identified initial diff content$")
    public void commit_has_been_added_with_comment(String comment) throws Throwable {

        // Diff completed
        a_diff_analysis_has_been_started_and_completed();

        // Selected all
        user_has_selected_all_ready_content();

        // Specified comment
        user_has_defined_commit_comment(comment);

        // Saved commit
        user_save_commit();

        // Checked commit
        CommitDetails commit = getSavedCommit();

        assertThat(getSavedCommit()).isNotNull();
        assertThat(commit.getComment()).isEqualTo(comment);
    }

    @Given("^a new commit \"(.*)\" has been saved with all the new identified diff content$")
    public void new_commit_was_added_with_comment(String comment) throws Throwable {

        // No init here

        // Started
        a_diff_has_already_been_launched();

        // Completed
        a_diff_is_completed();

        // Selected all
        user_has_selected_all_ready_content();

        // Specified comment
        user_has_defined_commit_comment(comment);

        // Saved commit
        user_save_commit();

        // Checked commit
        CommitDetails commit = getSavedCommit();

        assertThat(getSavedCommit()).isNotNull();
        assertThat(commit.getComment()).isEqualTo(comment);
    }

    @Given("^a commit \"(.*)\" has been saved with all the new identified diff content in destination environment$")
    public void new_commit_was_added_with_comment_in_dest(String comment) throws Throwable {
        // Switched env use same db
        new_commit_was_added_with_comment(comment);
    }

    @Given("^the commit \"(.*)\" has been saved and exported with all the identified initial diff content$")
    public void new_commit_exported(String comment) throws Throwable {
        commit_has_been_added_with_comment(comment);
        UUID specifiedCommit = backlogDatabase().searchCommitWithName(getCurrentUserProject(), comment);
        PushPullFixtures.currentExport = this.commitService.exportOneCommit(specifiedCommit);
    }

    @When("^the user accesses to preparation commit page$")
    public void user_access_preparation_saving_page() throws Throwable {

        // Get provided data to post them updated
        PilotedCommitPreparation<?> preparation = this.prep.getCurrentCommitPreparation();

        // Various cases
        if (select != null && select.equals("ALL")) {
            preparation.getDomains().forEach(d -> d.getPreparedContent().forEach(c -> c.getDiff().forEach(l -> {
                l.setSelected(true);
                l.setRollbacked(false);
            })));
        }

        // Post prepared data
        postObject(getCorrespondingLinkForPageName("commit saving page"), p("preparation", preparation));

        // Refresh preparation editData
        currentCommit = ((PilotedCommitPreparation<?>) currentAction.andReturn()
                .getModelAndView().getModel().get("preparation")).getCommitData();
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

    @When("^the user select all prepared diff content for merge commit$")
    public void user_select_all_content_for_merge() throws Exception {

        // Call select all uri
        post("/merge/selection/all", p("selected", "true"), p("rollbacked", "false"));

        select = "ALL";
    }

    @When("^the user save the commit$")
    public void user_save_commit() throws Exception {

        // Get provided data to post them updated
        PilotedCommitPreparation<?> preparation = this.prep.getCurrentCommitPreparation();

        // Various cases
        if (currentCommit != null) {
            preparation.setCommitData(currentCommit);
        }

        // Post prepared data
        postObject(getCorrespondingLinkForPageName("commit save"), p("preparation", preparation));
    }

    @When("^the user save the merge commit$")
    public void user_save_merge_commit() throws Exception {

        // It's a normal commit
        user_save_commit();
    }

    @Then("^a diff is running$")
    public void a_diff_is_running() {

        Assert.assertEquals(PilotedCommitStatus.DIFF_RUNNING, this.prep.getCurrentCommitPreparationState().getStatus());
    }

    @Then("^a merge diff is running$")
    public void a_merge_diff_is_running() {

        a_diff_is_running();
        assertThat(this.prep.getCurrentCommitPreparation().getPreparingState()).isEqualTo(CommitState.MERGED);
    }

    @Then("^an alert says that the diff is still running$")
    public void an_alert_says_that_the_diff_is_still_running() throws Throwable {

        // Alert => We are on running diff page
        currentAction = currentAction.andExpect(view().name(getCorrespondingTemplateForName("diff running")));

        // The running preparation is still the previous one
        Assert.assertEquals(runningPrep, this.prep.getCurrentCommitPreparation().getIdentifier());
    }

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

                IndexAction action = IndexAction.valueOf(dataLine.get("Action"));
                assertThat(diffLine.getAction()).isEqualTo(action);
                assertThat(diffLine.getKeyValue()).isEqualTo(dataLine.get("Key"));

                // No need to check payload in delete
                if (action != REMOVE) {
                    assertThat(diffLine.getHrPayload()).isEqualTo(dataLine.get("Payload"));
                }
            }
        });
    }

    @Then("^the merge commit content is rendered with these identified changes :$")
    public void merge_commit_content_ready(DataTable data) {

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

                IndexAction action = IndexAction.valueOf(dataLine.get("Action"));
                assertThat(diffLine.getAction()).isEqualTo(action);
                assertThat(diffLine.getKeyValue()).isEqualTo(dataLine.get("Key"));

                // No need to check payload in delete
                if (action != REMOVE) {
                    assertThat(diffLine.getHrPayload()).isEqualTo(dataLine.get("Payload"));
                }
            }
        });
    }


    @Then("^the commit content has these associated lob data :$")
    public void commit_lob_content(DataTable table) {

        Map<String, String> datas = table.asMaps(String.class, String.class).stream().collect(Collectors.toMap(i -> i.get("hash"), i -> i.get("data")));

        PilotedCommitPreparation<?> preparation = this.prep.getCurrentCommitPreparation();

        assertThat(preparation.getStatus()).isEqualTo(PilotedCommitStatus.COMMIT_CAN_PREPARE);
        assertThat(preparation.getDiffLobs()).hasSize(datas.size());

        preparation.getDiffLobs().forEach((hash, bin) -> {

            // Custom clean message
            if (datas.get(hash) == null) {
                throw new AssertionError("Cannot found corresponding hash for current lob. Current is "
                        + hash + " with data \"" + FormatUtils.toString(bin) + "\"");
            }
            assertThat(FormatUtils.toString(bin)).isEqualTo(datas.get(hash));
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

    @Then("^the commit type is \"(.*)\"$")
    public void commit_type(String type) {
        PilotedCommitPreparation<?> preparation = this.prep.getCurrentCommitPreparation();

        assertThat(preparation).isNotNull();
        assertThat(preparation.getPreparingState()).isEqualTo(CommitState.valueOf(type));
    }

}
