package fr.uem.efluid.cucumber.steps;

import fr.uem.efluid.cucumber.common.CucumberStepDefs;
import fr.uem.efluid.cucumber.common.ImportExportHelper;
import fr.uem.efluid.model.entities.CommitState;
import fr.uem.efluid.model.entities.IndexAction;
import fr.uem.efluid.services.types.*;
import fr.uem.efluid.utils.ApplicationException;
import fr.uem.efluid.utils.DatasourceUtils;
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
import java.util.stream.Stream;

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
// @Ignore // Means it will be ignored by junit start, but will be used by cucumber
@SuppressWarnings("unchecked")
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

    /* ########################################### ALL GIVENS ################################################ */

    @Given("^the database doesn't support nullable join keys$")
    public void disable_join_key_support() {
        DatasourceUtils.CustomQueryGenerationRules rules = initUpdatableRules();
        rules.setJoinOnNullableKeys(false);
        this.queryGenerator.update(rules);
    }

    @Given("^the database does support nullable join keys$")
    public void init_join_key_support() {
        DatasourceUtils.CustomQueryGenerationRules rules = initUpdatableRules();
        rules.setJoinOnNullableKeys(true);
        this.queryGenerator.update(rules);
    }

    @Given("^a diff analysis can be started and completed$")
    public void a_diff_analysis_can_be_started_and_completed() {

        // Reset select
        select = null;

        // Full dic init
        if (CommonStepDefs.efluidCase) {
            initCompleteDictionaryWithEfluidTestTables();
        } else {
            initCompleteDictionaryWith8Tables();
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

    @Given("^a new diff analysis has been started and completed$")
    public void a_new_diff_analysis_has_been_started_and_completed() throws Throwable {

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

    @Given("^the exported package for commit \"(.*)\" has been merged in destination environment$")
    public void a_merge_has_been_done(String name) throws Throwable {

        // Cancel anything running
        no_diff_is_running();

        // Start new merge from available package
        this.prep.startMergeCommitPreparation(getNamedExportOrSingleCurrentOne(name).getResult());

        // And get diff uuid for testing
        runningPrep = this.prep.getCurrentCommitPreparation().getIdentifier();

        // Completed
        a_merge_diff_is_completed();

        // Selected all
        user_has_selected_all_ready_content_for_merge();

        // Name commit
        user_has_defined_commit_comment("merged " + name);

        // It's a normal commit
        user_save_commit();
    }

    @Given("^a merge diff analysis has been started and completed with the package of commit \"(.*)\"$")
    public void a_merge_diff_analysis_has_been_started_and_completed_from_package(String name) throws Throwable {
        // Import started

        // Cancel anything running
        no_diff_is_running();

        // Start new merge from available package
        this.prep.startMergeCommitPreparation(getNamedExportOrSingleCurrentOne(name).getResult());

        // And get diff uuid for testing
        runningPrep = this.prep.getCurrentCommitPreparation().getIdentifier();

        // Completed
        a_merge_diff_is_completed();
    }

    @Given("^a merge diff analysis has been started and completed with the package of commit \"(.*)\" created a moment after$")
    public void a_merge_diff_analysis_has_been_started_and_completed_from_package_created_after(String name) throws Throwable {

        // Normal merge, but with postponed package
        postponeImportedPackageTime(getSavedCommit().getCreatedTime().plusMinutes(1));
        a_merge_diff_analysis_has_been_started_and_completed_from_package(name);
        postponeImportedPackageTime(null); // Reset
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
        this.prep.startMergeCommitPreparation(getSingleCurrentExport().getResult());

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
        post("/ui/prepare/selection/all", p("selected", "true"), p("rollbacked", "false"));

        select = "ALL";

        user_access_preparation_saving_page();
    }

    @Given("^the user has asked rollback on all content for commit$")
    public void user_has_rollback_all_ready_content() throws Throwable {

        // Call select all uri
        post("/ui/prepare/selection/all", p("selected", "false"), p("rollbacked", "true"));

        select = "NONE";

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


    @Given("^the proposed diff has been saved as commit \"(.*)\"$")
    public void user_has_validated_diff_as_commit(String name) throws Throwable {

        // Current diff is ready
        a_diff_is_completed();

        // Select all
        user_has_selected_all_ready_content();

        // New commit
        user_has_defined_commit_comment(name);

        // Saved commit
        user_save_commit();
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
        currentExports.put(comment, processCommitExportWithoutTransformerCustomization(CommitExportEditData.CommitSelectType.SINGLE_ONE, specifiedCommit));
    }

    @Given("^the commit \"(.*)\" has been saved and exported with all the new identified diff content$")
    public void new_update_commit_exported(String comment) throws Throwable {
        new_commit_was_added_with_comment(comment);
        UUID specifiedCommit = backlogDatabase().searchCommitWithName(getCurrentUserProject(), comment);
        currentExports.put(comment, processCommitExportWithoutTransformerCustomization(CommitExportEditData.CommitSelectType.SINGLE_ONE, specifiedCommit));
    }

    @Given("^the user has saved the merge commit$")
    public void user_has_saved_merge_commit() throws Exception {

        // It's a normal commit
        user_save_commit();
    }

    @Given("^the configured max before similar diff process is (.*)$")
    public void set_similar_max_config(long value) {
        this.indexDisplayConfig.forceCombineSimilarDiffAfter(value);
    }

    /* ########################################### ALL WHENS ################################################ */

    @When("^the user accesses to preparation commit page$")
    public void user_access_preparation_saving_page() throws Throwable {

        // Get provided data to post them updated
        PilotedCommitPreparation<?> preparation = this.prep.getCurrentCommitPreparation();

        // Various cases
        if (select != null && select.equals("ALL")) {
            preparation.getDiffContent().forEach(l -> {
                l.setSelected(true);
                l.setRollbacked(false);
            });
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
        post("/ui/prepare/selection/all", p("selected", "true"), p("rollbacked", "false"));

        select = "ALL";
    }

    @When("^the user select the filtered diff content for commit$")
    public void user_select_filtered_content() throws Exception {

        // Call select all uri
        postWithBody("/ui/prepare/selection/filtered", CommitStepDefs.currentSearch, p("selected", "true"), p("rollbacked", "false"));

        assertRequestWasOk();

        select = "FILTERED";
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

    /* ########################################### ALL THENS ################################################ */

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

        PilotedCommitPreparation<?> preparation = this.prep.getCurrentCommitPreparation();

        assertThat(preparation.getStatus()).isEqualTo(PilotedCommitStatus.COMMIT_CAN_PREPARE);

        assertDiffContentIsCompliant(preparation, data);
    }

    @Then("^no commit content has been identified$")
    @Then("^no merge content has been identified$")
    public void commit_content_empty() {

        PilotedCommitPreparation<?> preparation = this.prep.getCurrentCommitPreparation();

        assertThat(preparation.getStatus()).isEqualTo(PilotedCommitStatus.COMMIT_CAN_PREPARE);

        assertThat(preparation.getDiffContent().size()).isEqualTo(0);
    }

    @Then("^the commit content is selected as this :$")
    public void commit_content_selected(DataTable data) {

        PilotedCommitPreparation<?> preparation = this.prep.getCurrentCommitPreparation();

        assertThat(preparation.getStatus()).isEqualTo(PilotedCommitStatus.COMMIT_CAN_PREPARE);

        assertDiffContentSelect(preparation, data);
    }


    @Then("^the paginated commit content is rendered with these identified changes :$")
    public void commit_detail_content(DataTable table) throws Exception {

        DiffContentPage paginatedContent = postContent("/ui/prepare/page/0", CommitStepDefs.currentSearch, DiffContentPage.class);

        // Get details directly with all content
        assertDiffContentIsCompliant(new DiffContentHolder<PreparedIndexEntry>(
                                             new ArrayList<>(paginatedContent.getPage()), this.prep.getCurrentCommitPreparation().getReferencedTables()) {
                                     },
                table);
    }

    @Then("^the paginated commit content is rendered with these identified sorted changes :$")
    public void commit_detail_content_sorted(DataTable table) throws Exception {

        DiffContentPage paginatedContent = postContent("/ui/prepare/page/0", CommitStepDefs.currentSearch, DiffContentPage.class);

        // Control with same sort rules
        assertDiffContentIsCompliantOrdered(paginatedContent.getPage(), table);
    }


    @Then("^these remarks on missing linked lines are rendered :$")
    public void commit_remarks_ready(DataTable data) {

        // Table      | Key     | Payload

        // Get by tables
        Map<String, List<Map<String, String>>> tables = data.asMaps().stream().collect(Collectors.groupingBy(i -> i.get("Table")));

        PilotedCommitPreparation<?> preparation = this.prep.getCurrentCommitPreparation();

        assertThat(preparation.getStatus()).isEqualTo(PilotedCommitStatus.COMMIT_CAN_PREPARE);
        assertThat(preparation.isHasSomeDiffRemarks()).isTrue();

        tables.forEach((t, v) -> {

            List<ContentLineDisplay> remarkDisplays = preparation.getDiffRemarks().stream()
                    .filter(r -> r.getLocation().equals("table " + t))
                    .flatMap(c -> ((DiffRemark<List<ContentLineDisplay>>) c).getPayload().stream())
                    .collect(Collectors.toList());

            assertThat(remarkDisplays.size()).withFailMessage("Missing remarks for table %s", t).isEqualTo(v.size());

            v.forEach(line -> {
                Optional<ContentLineDisplay> remark = remarkDisplays.stream().filter(r -> r.getKey().equals(line.get("Key"))).findFirst();
                assertThat(remark).withFailMessage("No remark found for key %s", line.get("Key")).isPresent();
                assertThat(remark.get().getHrPayload()).isEqualTo(line.get("Payload"));
            });
        });
    }

    @Then("^there is no remarks on missing linked lines$")
    public void commit_remarks_ready() {

        PilotedCommitPreparation<?> preparation = this.prep.getCurrentCommitPreparation();

        assertThat(preparation.getStatus()).isEqualTo(PilotedCommitStatus.COMMIT_CAN_PREPARE);
        assertThat(preparation.isHasSomeDiffRemarks()).isFalse();
    }

    @Then("^the commit content has (.*) entries for managed table \"(.*)\"$")
    public void commit_content_size(int size, String table) {
        PilotedCommitPreparation<?> preparation = this.prep.getCurrentCommitPreparation();
        assertThat(preparation.getDiffContentForTableName(table)).hasSize(size);
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
        boolean combinedKeySpecified = data.cells().get(0).contains("Combined Key");

        PilotedCommitPreparation<?> preparation = this.prep.getCurrentCommitPreparation();

        // If "need resolve" is specified, we want all lines, else only the ones "needing action"
        long currentCount = needResolveSpecified && !combinedKeySpecified
                ? preparation.getTotalCount()
                : preparation.getDiffContent().stream().filter(PreparedIndexEntry::isNeedAction).count();

        assertThat(preparation.getStatus()).isEqualTo(PilotedCommitStatus.COMMIT_CAN_PREPARE);
        assertThat(currentCount).isEqualTo(data.asMaps().size());

        tables.forEach((t, v) -> {
            Collection<? extends PreparedIndexEntry> indexEntries = needResolveSpecified
                    ? preparation.getDiffContentForTableName(t)
                    : preparation.getDiffContentForTableName(t).stream().filter(PreparedIndexEntry::isNeedAction).collect(Collectors.toList());

            if (indexEntries.isEmpty()) {
                throw new AssertionError("Cannot find corresponding diff for table " + t);
            }

            assertThat(indexEntries.size()).isEqualTo(v.size());

            List<? extends PreparedIndexEntry> sortedIndexEntries = indexEntries.stream().sorted(Comparator.comparing(PreparedIndexEntry::getKeyValue)).collect(Collectors.toList());
            v.sort(Comparator.comparing(CucumberStepDefs::dataKey));

            // Keep order
            for (int i = 0; i < sortedIndexEntries.size(); i++) {

                PreparedMergeIndexEntry diffLine = (PreparedMergeIndexEntry) sortedIndexEntries.get(i);

                // If "need Resolve" not specified, only check the lines needing action
                if (needResolveSpecified || diffLine.isNeedAction()) {
                    Map<String, String> dataLine = v.get(i);

                    if (dataLine.containsKey("Combined Key")) {
                        assertThat(diffLine.getCombinedKey()).describedAs("combined key for key " + diffLine.getKeyValue()).isEqualTo(dataLine.get("Combined Key"));
                    }

                    IndexAction action = IndexAction.valueOf(dataLine.get("Action"));

                    String desc = " on table \"" + t + "\" on key \""
                            + diffLine.getKeyValue() + "\". Resolution was \"" + diffLine.getResolutionRule() + "\"";

                    assertThat(diffLine.getAction()).as("Action" + desc).isEqualTo(action);

                    // Compare at byte level for OS independence
                    assertRawKeysAreEquals(dataLine, diffLine.getKeyValue(), desc);

                    // No need to check payload in delete
                    if (action != REMOVE) {
                        assertThat(diffLine.getHrPayload()).as("Payload" + desc).isEqualTo(dataLine.get("Payload"));
                    }

                    if (dataLine.get("Need Resolve") != null) {
                        assertThat(diffLine.isNeedAction()).describedAs("Need resolve" + desc).isEqualTo("true".equals(dataLine.get("Need Resolve")));
                    }
                }
            }
        });
    }

    @Then("^the merge commit content is rendered with changes for these lines : \"(.*)\"$")
    public void merge_commit_content_ready_inline(String changes) {

        List<ChangeSpec> preparedChanges = Stream.of(changes.trim().split(", "))
                .map(ChangeSpec::new)
                .collect(Collectors.toList());

        // Get by tables
        Map<String, List<ChangeSpec>> tables = preparedChanges.stream()
                .collect(Collectors.groupingBy(ChangeSpec::getTable));

        PilotedCommitPreparation<?> preparation = this.prep.getCurrentCommitPreparation();

        // If "need resolve" is specified, we want all lines, else only the ones "needing action"
        long currentCount = preparation.getDiffContent().stream().filter(PreparedIndexEntry::isNeedAction).count();

        assertThat(preparation.getStatus()).as("merge preparation status").isEqualTo(PilotedCommitStatus.COMMIT_CAN_PREPARE);
        assertThat(currentCount).as("count for merge commit diff line with action needed").isEqualTo(preparedChanges.size());

        tables.forEach((t, v) -> {
            Collection<? extends PreparedIndexEntry> indexEntries =
                    preparation.getDiffContentForTableName(t).stream().filter(PreparedIndexEntry::isNeedAction).collect(Collectors.toList());

            if (indexEntries.isEmpty()) {
                throw new AssertionError("Cannot find corresponding diff for table " + t);
            }

            assertThat(indexEntries.size()).isEqualTo(v.size());

            List<? extends PreparedIndexEntry> sortedIndexEntries = indexEntries.stream().sorted(Comparator.comparing(PreparedIndexEntry::getKeyValue)).collect(Collectors.toList());
            v.sort(Comparator.comparing(ChangeSpec::getKey));

            // Keep order
            for (int i = 0; i < sortedIndexEntries.size(); i++) {

                PreparedMergeIndexEntry diffLine = (PreparedMergeIndexEntry) sortedIndexEntries.get(i);

                // If "need Resolve" not specified, only check the lines needing action
                if (diffLine.isNeedAction()) {
                    ChangeSpec dataLine = v.get(i);

                    IndexAction action = IndexAction.valueOf(dataLine.getAction());

                    String desc = " on table \"" + t + "\" on key \""
                            + diffLine.getKeyValue() + "\". Resolution was \"" + diffLine.getResolutionRule() + "\"";

                    assertThat(diffLine.getAction()).as("Action" + desc).isEqualTo(action);
                    assertThat(diffLine.getKeyValue()).as("Key" + desc).isEqualTo(dataLine.getKey());
                }
            }
        });
    }

    @Then("^the paginated merge commit content is rendered with these identified sorted changes :$")
    public void merge_detail_content_sorted(DataTable table) throws Exception {

        DiffContentPage paginatedContent = postContent("/ui/merge/page/0", CommitStepDefs.currentSearch, DiffContentPage.class);

        assertDiffContentIsCompliantOrdered(paginatedContent.getPage(), table);
    }

    @Then("^the merge commit content has these resolution details :$")
    public void merge_commit_content_details(DataTable data) {

        PilotedCommitPreparation<PreparedMergeIndexEntry> preparation = (PilotedCommitPreparation<PreparedMergeIndexEntry>) this.prep.getCurrentCommitPreparation();

        assertThat(preparation.getStatus()).isEqualTo(PilotedCommitStatus.COMMIT_CAN_PREPARE);

        data.asMaps().forEach(l -> {

            String table = l.get("Table");
            String key = l.get("Key");
            PreparedMergeIndexEntry entry = preparation.getDiffContentForTableName(table).stream()
                    .filter(d -> d.getKeyValue().equals(key))
                    .findFirst().orElseThrow(() ->
                            new AssertionError("Cannot find corresponding diff entry for table " + table + " on key " + key));

            check_resolution_line(entry, "their", l.get("Their Act"), entry.getTheir().getHrPayload());
            check_resolution_line(entry, "mine", l.get("Mine Act"), entry.getMine().getHrPayload());
            check_resolution_line(entry, "resolution", l.get("Res. Act"), l.get("Res. Payload"));

            assertThat(entry.getResolutionRule()).describedAs("Resolution rule for table " + table + " on key " + key).isEqualTo(l.get("Rule"));
            assertThat(entry.isNeedAction()).describedAs("Need action for table " + table + " on key " + key).isEqualTo(Boolean.valueOf(l.get("Need Act")));
            assertThat(this.valueConverter.convertToHrPayload(entry.getPrevious(), null)).describedAs("Resolved source content for table " + table + " on key " + key).isEqualTo(l.get("Res. Previous"));
        });
    }

    @Then("^the merge commit content has these resolution details for table \"(.*)\" on key \"(.*)\" :$")
    public void merge_commit_content_details(String table, String key, DataTable data) {

        PilotedCommitPreparation<PreparedMergeIndexEntry> preparation = (PilotedCommitPreparation<PreparedMergeIndexEntry>) this.prep.getCurrentCommitPreparation();

        assertThat(preparation.getStatus()).isEqualTo(PilotedCommitStatus.COMMIT_CAN_PREPARE);

        PreparedMergeIndexEntry entry = preparation.getDiffContentForTableName(table).stream()
                .filter(d -> d.getKeyValue().equals(key))
                .findFirst().orElseThrow(() ->
                        new AssertionError("Cannot find corresponding diff entry for table " + table + " on key " + key));

        data.asMaps().forEach(l -> {
            check_resolution_line(entry, l.get("Type"), l.get("Action"), l.get("Payload"));
        });

    }

    private void check_resolution_line(PreparedMergeIndexEntry entry, String type, String actStr, String payload) {

        if (!StringUtils.hasText(payload)) {
            payload = null;
        }

        IndexAction action = StringUtils.hasText(actStr) ? IndexAction.valueOf(actStr) : null;
        String desc = " on table \"" + entry.getTableName() + "\" on key \"" + entry.getKeyValue() + "\" for type "
                + type + ". Resolution was \"" + entry.getResolutionRule() + "\"";

        switch (type) {
            case "mine":
                if (entry.getMine().getAction() != REMOVE) {
                    assertThat(entry.getMine().getHrPayload()).as("Payload" + desc).isEqualTo(payload);
                }
                assertThat(entry.getMine().getAction()).as("Action" + desc).isEqualTo(action);
                break;
            case "their":
                if (entry.getTheir().getAction() != REMOVE) {
                    assertThat(entry.getTheir().getHrPayload()).as("Payload" + desc).isEqualTo(payload);
                }
                assertThat(entry.getTheir().getAction()).as("Action" + desc).isEqualTo(action);
                break;
            case "resolution":
                if (entry.getAction() == REMOVE) {
                    assertThat(entry.getPayload()).as("Payload" + desc).isNull();
                } else {
                    assertThat(entry.getHrPayload()).as("Payload" + desc).isEqualTo(payload);
                }
                assertThat(entry.getAction()).as("Action" + desc).isEqualTo(action);
                break;
            default:
                throw new AssertionError("Unsupported data type \"" + type + "\" for resolution details");
        }
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
        preparation.getDiffContent().forEach(l -> {
            assertThat(l.isSelected()).isFalse();
            assertThat(l.isRollbacked()).isFalse();
        });
    }

    @Then("^all the diff preparation content is selected for commit$")
    public void commit_preparation_all_selected() {
        PilotedCommitPreparation<?> preparation = this.prep.getCurrentCommitPreparation();

        assertThat(preparation).isNotNull();
        preparation.getDiffContent().stream()
                .filter(PreparedIndexEntry::isNeedAction)
                .forEach(l -> {
                    assertThat(l.isSelected()).isTrue();
                    assertThat(l.isRollbacked()).isFalse();
                });
    }

    @Then("^the commit type is \"(.*)\"$")
    public void commit_type(String type) {
        PilotedCommitPreparation<?> preparation = this.prep.getCurrentCommitPreparation();

        assertThat(preparation).isNotNull();
        assertThat(preparation.getPreparingState()).isEqualTo(CommitState.valueOf(type));
    }

    @Then("^the paginated json content rendering on page (.*) is equals to \"(.*)\"$")
    public void then_display_paginated_content(int page, String file) throws Exception {
        File contentFile = new File("src/test/resources/datasets/" + file);
        String content = String.join("\n", Files.readAllLines(contentFile.toPath()));

        get("/ui/prepare/page/" + page);
        assertRequestWasOk();
        currentAction.andDo(MockMvcResultHandlers.print());
        currentAction.andExpect(content().string(containsString(content)));
    }

    @Then("^a summary of the identified changes is : \"(.*)\" adds - \"(.*)\" updates - \"(.*)\" deletes$")
    public void then_diff_summary(int adds, int updates, int deletes) {
        PilotedCommitPreparation<?> preparation = getCurrentSpecifiedProperty("preparation", PilotedCommitPreparation.class);

        assertThat(preparation.getSummary()).isNotNull();
        assertThat(preparation.getSummary().getIdentifiedAdds()).isEqualTo(adds);
        assertThat(preparation.getSummary().getIdentifiedUpdates()).isEqualTo(updates);
        assertThat(preparation.getSummary().getIdentifiedDeletes()).isEqualTo(deletes);
    }

    private static class ChangeSpec {

        private final String action;
        private final String table;
        private final String key;

        ChangeSpec(String src) {
            // ADD TTEST1:$5-regA
            String[] core = src.trim().split(" ");
            String[] details = core[1].trim().split(":");
            this.action = core[0];
            this.table = details[0];
            this.key = details[1];
        }

        public String getAction() {
            return action;
        }

        public String getTable() {
            return table;
        }

        public String getKey() {
            return key;
        }
    }

}
