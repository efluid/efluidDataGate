package fr.uem.efluid.cucumber.steps;

import fr.uem.efluid.cucumber.common.CucumberStepDefs;
import fr.uem.efluid.cucumber.common.ImportExportHelper;
import fr.uem.efluid.model.entities.CommitState;
import fr.uem.efluid.model.entities.IndexAction;
import fr.uem.efluid.services.types.*;
import fr.uem.efluid.utils.ApplicationException;
import fr.uem.efluid.utils.ErrorType;
import fr.uem.efluid.utils.FormatUtils;
import io.cucumber.datatable.DataTable;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.junit.Assert;
import org.junit.Before;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.util.StringUtils;

import java.io.File;
import java.nio.file.Files;
import java.util.*;
import java.util.stream.Collectors;

import static fr.uem.efluid.model.entities.IndexAction.REMOVE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.core.StringContains.containsString;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

/**
 * @author elecomte
 * @version 1
 * @since v0.0.8
 */
//@Ignore // Means it will be ignored by junit start, but will be used by cucumber
public class PreparationStepDefs extends CucumberStepDefs {

    private static UUID runningPrep;

    private static String select = null;

    private static CommitEditData currentCommit;

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
        if (CommonStepDefs.efluidCase) {
            initCompleteDictionaryWithEfluidTestTables();
        } else {
            initCompleteDictionaryWith7Tables();
        }

        CommonStepDefs.efluidCase = false;

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
    public void a_merge_diff_has_already_been_launched() {

        // Cancel anything running
        no_diff_is_running();

        // Start new merge from available package
        this.prep.startMergeCommitPreparation(PushPullStepDefs.currentExport.getResult());

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
    public void user_has_defined_commit_comment(String comment) {

        currentCommit.setComment(comment);
    }

    @Given("^the user has attached these documents to the commit:$")
    public void user_had_specified_attachments(DataTable table) {

        table.asMaps().forEach(m -> {
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
    public void new_init_commit_exported(String comment) throws Throwable {
        commit_has_been_added_with_comment(comment);
        UUID specifiedCommit = backlogDatabase().searchCommitWithName(getCurrentUserProject(), comment);
        PushPullStepDefs.currentExport = this.commitService.exportOneCommit(specifiedCommit);
    }

    @Given("^the commit \"(.*)\" has been saved and exported with all the new identified diff content$")
    public void new_update_commit_exported(String comment) throws Throwable {
        new_commit_was_added_with_comment(comment);
        UUID specifiedCommit = backlogDatabase().searchCommitWithName(getCurrentUserProject(), comment);
        PushPullStepDefs.currentExport = this.commitService.exportOneCommit(specifiedCommit);
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
        postObject(getCorrespondingLinkForPageName("commit saving page"), p("preparationPush", preparation));

        // Refresh preparation editData
        currentCommit = ((PilotedCommitPreparation<?>) Objects.requireNonNull(currentAction.andReturn()
                .getModelAndView()).getModel().get("preparation")).getCommitData();
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
        postObject(getCorrespondingLinkForPageName("commit save"), p("preparationPush", preparation));
    }

    @When("^the user save the merge commit$")
    public void user_save_merge_commit() throws Exception {

        // It's a normal commit
        user_save_commit();
    }

    @Then("^a diff is running$")
    public void a_diff_is_running() {
        assertThat(PushPullStepDefs.currentException).isNull();
        PushPullStepDefs.currentException = null;
        Assert.assertEquals(PilotedCommitStatus.DIFF_RUNNING, this.prep.getCurrentCommitPreparationState().getStatus());
    }

    @Then("^a merge diff is running$")
    public void a_merge_diff_is_running() {
        a_diff_is_running();
        assertThat(this.prep.getCurrentCommitPreparation().getPreparingState()).isEqualTo(CommitState.MERGED);
    }

    @Then("^a merge diff fail with error code (.*)$")
    public void a_merge_diff_fail_with_error(String error) {
        assertThat(PushPullStepDefs.currentException).isNotNull();
        assertThat(PushPullStepDefs.currentException).isInstanceOf(ApplicationException.class);
        assertThat(((ApplicationException) PushPullStepDefs.currentException).getError()).isEqualTo(ErrorType.valueOf(error));
        PushPullStepDefs.currentException = null;
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
        Map<String, List<Map<String, String>>> tables = data.asMaps().stream().collect(Collectors.groupingBy(i -> i.get("Table")));

        PilotedCommitPreparation<?> preparation = this.prep.getCurrentCommitPreparation();

        assertThat(preparation.getStatus()).isEqualTo(PilotedCommitStatus.COMMIT_CAN_PREPARE);
        assertThat(preparation.getTotalCount()).isEqualTo(data.asMaps().size());
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

    @Then("^the commit content has (.*) entries for managed table \"(.*)\"$")
    public void commit_content_size(int size, String table) {

        PilotedCommitPreparation<?> preparation = this.prep.getCurrentCommitPreparation();

        var diff = preparation.getDomains().stream().flatMap(d -> d.getPreparedContent().stream()).filter(c -> ((DiffDisplay) c).getDictionaryEntryTableName().equals(table)).findFirst();

        assertThat(diff).isPresent();
        assertThat(diff.get().getDiff()).hasSize(size);
    }

    @Then("^no diff content has been found$")
    public void commit_empty() {
        PilotedCommitPreparation<?> preparation = this.prep.getCurrentCommitPreparation();
        assertThat(preparation.isEmptyDiff()).isTrue();
    }

    @Then("^the merge commit content is rendered with these identified changes :$")
    public void merge_commit_content_ready(DataTable data) {

        // Get by tables
        Map<String, List<Map<String, String>>> tables = data.asMaps().stream().collect(Collectors.groupingBy(i -> i.get("Table")));

        boolean needResolveSpecified = data.cells().get(0).contains("Need Resolve");

        PilotedCommitPreparation<?> preparation = this.prep.getCurrentCommitPreparation();

        // If "need resolve" is specified, we want all lines, else only the ones "needing action"
        long currentCount = needResolveSpecified
                ? preparation.getTotalCount()
                : preparation.getDomains().stream().mapToLong(DomainDiffDisplay::getNeedActionTotalCount).sum();

        assertThat(preparation.getStatus()).isEqualTo(PilotedCommitStatus.COMMIT_CAN_PREPARE);
        assertThat(currentCount).isEqualTo(data.asMaps().size());
        assertThat(preparation.getDomains().size()).isEqualTo(1);

        tables.forEach((t, v) -> {
            DiffDisplay<?> content = preparation.getDomains().get(0).getPreparedContent().stream()
                    .filter(p -> p.getDictionaryEntryTableName().equals(t))
                    .findFirst().orElseThrow(() -> new AssertionError("Cannot find corresponding diff for table " + t));

            List<? extends PreparedIndexEntry> indexEntries = needResolveSpecified
                    ? content.getDiff()
                    : content.getDiff().stream().filter(PreparedIndexEntry::isNeedAction).collect(Collectors.toList());

            assertThat(indexEntries.size()).isEqualTo(v.size());

            indexEntries.sort(Comparator.comparing(PreparedIndexEntry::getKeyValue));
            v.sort(Comparator.comparing(m -> m.get("Key")));

            // Keep order
            for (int i = 0; i < indexEntries.size(); i++) {

                PreparedMergeIndexEntry diffLine = (PreparedMergeIndexEntry) indexEntries.get(i);

                // If "need Resolve" not specified, only check the lines needing action
                if (needResolveSpecified || diffLine.isNeedAction()) {
                    Map<String, String> dataLine = v.get(i);

                    IndexAction action = IndexAction.valueOf(dataLine.get("Action"));

                    String desc = " on table \"" + content.getDictionaryEntryTableName() + "\" on key \""
                            + diffLine.getKeyValue() + "\". Resolution was \"" + diffLine.getResolutionRule() + "\"";

                    assertThat(diffLine.getAction()).as("Action" + desc).isEqualTo(action);
                    assertThat(diffLine.getKeyValue()).as("Key" + desc).isEqualTo(dataLine.get("Key"));

                    // No need to check payload in delete
                    if (action != REMOVE) {
                        assertThat(diffLine.getHrPayload()).as("Payload" + desc).isEqualTo(dataLine.get("Payload"));
                    }

                    if (dataLine.get("Need Resolve") != null) {
                        assertThat(diffLine.isNeedAction()).isEqualTo("true".equals(dataLine.get("Need Resolve")));
                    }
                }
            }
        });
    }

    @Then("^the merge commit content has these resolution details for table \"(.*)\" on key \"(.*)\" :$")
    public void merge_commit_content_details(String table, String key, DataTable data) {

        PilotedCommitPreparation<?> preparation = this.prep.getCurrentCommitPreparation();

        assertThat(preparation.getStatus()).isEqualTo(PilotedCommitStatus.COMMIT_CAN_PREPARE);

        MergePreparedDiff diff = (MergePreparedDiff) preparation.getDomains().get(0).getPreparedContent().stream()
                .filter(p -> p.getDictionaryEntryTableName().equals(table))
                .findFirst().orElseThrow(() -> new AssertionError("Cannot find corresponding diff for table " + table));

        PreparedMergeIndexEntry entry = diff.getDiff().stream()
                .filter(d -> d.getKeyValue().equals(key))
                .findFirst().orElseThrow(() ->
                        new AssertionError("Cannot find corresponding diff entry for table " + table + " on key " + key));

        data.asMaps().forEach(l -> {
            String payload = l.get("Payload");
            if (StringUtils.isEmpty(payload)) {
                payload = null;
            }
            String type = l.get("Type");
            String actStr = l.get("Action");
            IndexAction action = StringUtils.hasText(actStr) ? IndexAction.valueOf(actStr) : null;
            String desc = " on table \"" + table + "\" on key \"" + key + "\" for type "
                    + type + ". Resolution was \"" + entry.getResolutionRule() + "\"";

            switch (type) {
                case "mine":
                    assertThat(entry.getMine().getHrPayload()).as("Payload" + desc).isEqualTo(payload);
                    assertThat(entry.getMine().getAction()).as("Action" + desc).isEqualTo(action);
                    break;
                case "their":
                    assertThat(entry.getTheir().getHrPayload()).as("Payload" + desc).isEqualTo(payload);
                    assertThat(entry.getTheir().getAction()).as("Action" + desc).isEqualTo(action);
                    break;
                case "resolution":
                    assertThat(entry.getHrPayload()).as("Payload" + desc).isEqualTo(payload);
                    assertThat(entry.getAction()).as("Action" + desc).isEqualTo(action);
                    break;
                default:
                    throw new AssertionError("Unsupported data type \"" + type + "\" for resolution details");
            }
        });

    }

    @Then("^the commit content has these associated lob data :$")
    public void commit_lob_content(DataTable table) {

        Map<String, String> datas = table.asMaps().stream().collect(Collectors.toMap(i -> i.get("hash"), i -> i.get("data")));

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
        preparation.getDomains().forEach(d -> d.getPreparedContent()
                .forEach(c ->
                        c.getDiff().stream()
                                .filter(PreparedIndexEntry::isNeedAction)
                                .forEach(l -> {
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

    @Then("^the paginated json content rendering on page (.*) for table \"(.*)\" is equals to \"(.*)\"$")
    public void then_display_paginated_content(int page, String tablename, String file) throws Exception {
        File contentFile = new File("src/test/resources/datasets/" + file);
        String content = String.join("\n", Files.readAllLines(contentFile.toPath()));

        Optional<UUID> tabUuid = this.prep.getCurrentCommitPreparation().getDomains().stream().flatMap(d -> d.getPreparedContent().stream()).filter(c -> c.getDictionaryEntryTableName().equals(tablename)).map(c -> ((DiffDisplay) c).getDictionaryEntryUuid()).findFirst();

        assertThat(tabUuid).as("Table identifier in current diff for name " + tablename).isPresent();

        if (tabUuid.isPresent()) {
            get("/ui/prepare/page/" + tabUuid.get() + "/" + page);
            assertRequestWasOk();
            currentAction.andDo(MockMvcResultHandlers.print());
            currentAction.andExpect(content().string(containsString(content)));
        }
    }

}