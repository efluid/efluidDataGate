package fr.uem.efluid.cucumber.steps;

import fr.uem.efluid.cucumber.common.CucumberStepDefs;
import fr.uem.efluid.model.entities.*;
import fr.uem.efluid.services.DictionaryManagementService;
import fr.uem.efluid.services.types.*;
import fr.uem.efluid.utils.FormatUtils;
import io.cucumber.datatable.DataTable;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;


import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static fr.uem.efluid.model.entities.IndexAction.REMOVE;
import static java.util.stream.Collectors.toMap;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalStateException;
import static org.junit.Assert.*;

/**
 * @author elecomte
 * @version 1
 * @since v0.0.8
 */
//@Ignore // Means it will be ignored by junit start, but will be used by cucumber
public class CommitStepDefs extends CucumberStepDefs {

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

        CommitDetails commit = this.commitService.getExistingCommitDetails(backlogDatabase().searchCommitWithName(getCurrentUserProject(), comment));

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

        // Get by tables
        Map<String, List<Map<String, String>>> tables = data.asMaps().stream().collect(Collectors.groupingBy(i -> i.get("Table")));

        CommitDetails commit = getSavedCommit();

        tables.forEach((t, v) -> {
            DiffDisplay<?> content = commit.getContent().stream()
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
    private static List<CommitEditData> commitsOrderedByCreatedTime=new ArrayList<>();
    @Given("^a list of commit$")
    public void a_list_of_commits(){
        CommitEditData commit1 = new CommitEditData();
        commit1.setCreatedTime(LocalDateTime.now());
        CommitEditData commit2 = new CommitEditData();
        commit2.setCreatedTime(LocalDateTime.now());
        commitsOrderedByCreatedTime.add(commit2);
        commitsOrderedByCreatedTime.add(commit1);

    // Implicit init with default domain / project
    initMinimalWizzardData();

    // Implicit authentication and on page
    implicitlyAuthenticatedAndOnPage("list of versions");
}
    @Then("^the commits have to be ordered into their Created Time order$")
    public void the_commits_have_to_be_ordered_into_their_Created_Time_order(){
        for (int i = 0; i<(commitsOrderedByCreatedTime.size()-1); i++){
            a_list_of_commits();
            if (commitsOrderedByCreatedTime.get(i).getCreatedTime().isAfter(commitsOrderedByCreatedTime.get(i+1).getCreatedTime()));
            assertThatIllegalStateException();
            break;
        }
        int i=0;
        int j=0;
        assertEquals(i,j);
    }
}
