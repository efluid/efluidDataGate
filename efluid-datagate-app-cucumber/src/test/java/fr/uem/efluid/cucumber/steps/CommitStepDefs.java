package fr.uem.efluid.cucumber.steps;

import fr.uem.efluid.cucumber.common.CucumberStepDefs;
import fr.uem.efluid.model.DiffLine;
import fr.uem.efluid.model.entities.*;
import fr.uem.efluid.services.types.*;
import fr.uem.efluid.utils.FormatUtils;
import io.cucumber.datatable.DataTable;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.junit.Before;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static fr.uem.efluid.model.entities.IndexAction.REMOVE;
import static java.util.stream.Collectors.toMap;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author elecomte
 * @version 1
 * @since v0.0.8
 */
//@Ignore // Means it will be ignored by junit start, but will be used by cucumber
public class CommitStepDefs extends CucumberStepDefs {

    @When("^the user select the details of commit \"(.*)\"$")
    public void select_commit_details(String comment) throws Exception {
        List<CommitEditData> commits = getCurrentSpecifiedPropertyList("commits", CommitEditData.class);
        Optional<CommitEditData> commit = commits.stream().filter(c -> c.getComment().equals(comment)).findFirst();
        assertThat(commit).isPresent();

        // Navigate
        get("/ui/details/" + commit.get().getUuid());
    }

    @When("^apply a content filter criteria \"(.*)\" on \"(.*)\"$")
    public void apply_content_filter(String value, String criteria) {
        if (currentSearch.getFilters() == null) {
            currentSearch.setFilters(new HashMap<>());
        }
        currentSearch.getFilters().put(criteria, value);
    }

    @When("^apply a content sort criteria \"(.*)\" on \"(.*)\"$")
    public void apply_content_sort(String value, String criteria) {
        if (currentSearch.getSorts() == null) {
            currentSearch.setSorts(new HashMap<>());
        }
        currentSearch.getSorts().put(criteria, value);
    }

    @When("^the user ask for a revert of commit \"(.*)\"$")
    public void when_revert(String name) throws Exception {

        UUID revertable = getCurrentSpecifiedProperty("revertableCommit", UUID.class);

        Optional<CommitEditData> found = getCurrentSpecifiedPropertyList("commits", CommitEditData.class).stream()
                .filter(c -> c.getComment().equals(name)).findFirst();

        assertThat(revertable).isNotNull();
        assertThat(found).isPresent();
        assertThat(found.get().getUuid()).isEqualTo(revertable);

        get(getCorrespondingLinkForPageName("revert commit page") + "/{uuid}", revertable);
    }

    @Then("^the commit \"(.*)\" is added to commit list for current project$")
    public void then_commit_is_added_with_comment(String comment) {
        CommitDetails commit = getSavedCommit();

        assertThat(commit).isNotNull();
        assertThat(commit.getComment()).isEqualTo(comment);
    }

    @Then("^the merge commit \"(.*)\" is added to commit list for current project$")
    public void then_merge_commit_is_added_with_comment(String comment) {
        // Merge is a normal commit
        then_commit_is_added_with_comment(comment);
    }

    @Then("^the commit \"(.*)\" from current project is of type \"(.*)\"$")
    public void then_commit_is_added_with_comment_and_type(String comment, String type) {

        CommitDetails commit = this.commitService.getExistingCommitDetails(backlogDatabase().searchCommitWithName(getCurrentUserProject(), comment), false);

        assertThat(commit).isNotNull();
        assertThat(commit.getState()).isEqualTo(CommitState.valueOf(type));
    }

    @Then("^the commit \"(.*)\" is added to commit list for current project in destination environment$")
    public void then_commit_is_added_with_comment_in_dest(String comment) {

        // Switched env is same db
        then_commit_is_added_with_comment(comment);
    }

    @Then("^the saved commit content has these identified changes :$")
    public void commit_content_changes(DataTable data) {
        // Process assert on last saved commit in DB
        CommitDetails details = getSavedCommit();

        //checkCommitDetails(t -> details.getDiffContentForTableName(t).stream(), data);
        assertDiffContentIsCompliant(details, data);
    }

    @Then("^the saved merge commit content has these identified changes :$")
    public void merge_commit_content_changes(DataTable data) {

        // Merge is a normal commit
        commit_content_changes(data);
    }

    @Then("^these attachment documents are associated to the commit in the current project backlog:$")
    public void then_commit_contains_attachments(DataTable table) {

        CommitDetails commit = getSavedCommit();

        Map<String, AttachmentType> data = table.asMaps()
                .stream()
                .collect(toMap(m -> m.get("title"), m -> AttachmentType.valueOf(m.get("type"))));

        // Complies to the specified list of attachments
        assertThat(commit.getAttachments()).hasSize(data.size());
        commit.getAttachments().forEach(a -> {
            assertThat(a.getName()).matches(data::containsKey);
            assertThat(a.getType()).isEqualTo(data.get(a.getName()));
        });
    }

    @Then("^the saved commit content has these associated lob data :$")
    public void then_commit_contains_lobs(DataTable table) {

        CommitDetails commit = getSavedCommit();

        List<LobProperty> lobs = backlogDatabase().loadCommitLobs(commit);

        Map<String, String> datas = table.asMaps().stream().collect(Collectors.toMap(i -> i.get("hash"), i -> i.get("data")));

        assertThat(lobs).hasSize(datas.size());

        lobs.forEach((lobProperty) -> {

            // Custom clean message
            if (datas.get(lobProperty.getHash()) == null) {
                throw new AssertionError("Cannot found corresponding hash for current lob. Current is "
                        + lobProperty.getHash() + " with data \"" + FormatUtils.toString(lobProperty.getData()) + "\"");
            }
            assertThat(FormatUtils.toString(lobProperty.getData())).isEqualTo(datas.get(lobProperty.getHash()));
        });
    }

    @Then("^the saved commit content has (.*) lines$")
    public void then_commit_contains_lobs(long size) {
        assertThat(backlogDatabase().countCommitIndex(getSavedCommitUuid())).isEqualTo(size);
    }

    @Then("^the list of commits is :$")
    public void commit_list(DataTable table) {

        List<Map<String, String>> datas = table.asMaps();

        // Use fixed sorts to avoid ramdom failure
        List<CommitEditData> commits = getCurrentSpecifiedPropertyList("commits", CommitEditData.class).stream()
                .sorted(Comparator.comparing(CommitEditData::getCreatedTime))
                .collect(Collectors.toList());

        assertThat(commits).hasSize(datas.size());
        int idx = 0;
        for (CommitEditData commit : commits) {
            assertThat(commit).isNotNull();
            assertThat(commit.getUuid()).isNotNull();
            assertThat(commit.getComment()).isEqualTo(datas.get(idx).get("comment"));
            assertThat(commit.getOriginalUserEmail()).isEqualTo(datas.get(idx).get("author"));
            idx++;
        }
    }

    @Then("^the commit details are displayed with this content :$")
    public void commit_detail_content(DataTable table) throws Exception {

        CommitDetails details = getCurrentSpecifiedProperty("details", CommitDetails.class);

        DiffContentPage paginatedContent = postContent("/ui/details/" + details.getUuid() + "/page/0", currentSearch, DiffContentPage.class);

        // Get details directly with all content
        assertDiffContentIsCompliant(new DiffContentHolder<PreparedIndexEntry>(
                new ArrayList<>(paginatedContent.getPage()), details.getReferencedTables()) {
        }, table);
    }

    @Then("^the commit index has these managed technical payloads :$")
    public void commit_detail_index(DataTable table) throws Exception {

        CommitDetails details = getCurrentSpecifiedProperty("details", CommitDetails.class);

        var index = backlogDatabase().loadCommitIndex(details);

        // Get details directly with all content
        assertIndexIsTechnicallyCompliant(index, details.getReferencedTables(), table);
    }

    @Then("^the index for commit \"(.*)\" has these managed technical payloads :$")
    public void commit_detail_index(String comment, DataTable table) throws Exception {

        CommitDetails details = this.commitService.getExistingCommitDetails(backlogDatabase().searchCommitWithName(getCurrentUserProject(), comment), false);

        var index = backlogDatabase().loadCommitIndex(details);

        // Get details directly with all content
        assertIndexIsTechnicallyCompliant(index, details.getReferencedTables(), table);
    }

    @Then("^the commit details are displayed with this sorted content :$")
    public void commit_detail_content_sorted(DataTable table) throws Exception {

        CommitDetails details = getCurrentSpecifiedProperty("details", CommitDetails.class);

        DiffContentPage paginatedContent = postContent("/ui/details/" + details.getUuid() + "/page/0", currentSearch, DiffContentPage.class);

        // Get details directly with all content
        assertDiffContentIsCompliantOrdered(paginatedContent.getPage(), table);
    }

    @Then("^the commit details are displayed with (\\d*) payloads$")
    public void commit_detail_size(int size) {
        CommitDetails details = getCurrentSpecifiedProperty("details", CommitDetails.class);
        assertThat(details.getIndexSize()).isEqualTo(size);
    }

    @Then("^a revert commit can be created only for commit \"(.*)\"$")
    public void can_revert(String name) {

        UUID revertable = getCurrentSpecifiedProperty("revertableCommit", UUID.class);

        Optional<CommitEditData> found = getCurrentSpecifiedPropertyList("commits", CommitEditData.class).stream()
                .filter(c -> c.getComment().equals(name)).findFirst();

        assertThat(revertable).isNotNull();
        assertThat(found).isPresent();
        assertThat(found.get().getUuid()).isEqualTo(revertable);
    }
}
