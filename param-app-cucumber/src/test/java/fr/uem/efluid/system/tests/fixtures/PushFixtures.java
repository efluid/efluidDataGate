package fr.uem.efluid.system.tests.fixtures;

import cucumber.api.DataTable;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import fr.uem.efluid.model.entities.Commit;
import fr.uem.efluid.model.entities.IndexAction;
import fr.uem.efluid.model.entities.IndexEntry;
import fr.uem.efluid.model.entities.LobProperty;
import fr.uem.efluid.services.types.*;
import fr.uem.efluid.system.common.SystemTest;
import fr.uem.efluid.utils.FormatUtils;

import java.util.*;
import java.util.stream.Collectors;

import static fr.uem.efluid.model.entities.IndexAction.REMOVE;
import static java.util.stream.Collectors.toMap;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author elecomte
 * @version 1
 * @since v0.0.8
 */
public class PushFixtures extends SystemTest {

    static ExportImportResult<ExportFile> currentExport;

    @When("^the user request an export of all the commits$")
    public void when_export_all_commits() {
        currentExport = this.commitService.exportCommits(null);
    }

    @When("^the user request an export of the commit with name \"(.*)\"$")
    public void when_export_one_commit(String name) {
        Commit specifiedCommit = backlogDatabase().searchCommitWithName(getCurrentUserProject(), name);
        currentExport = this.commitService.exportOneCommit(specifiedCommit.getUuid());
    }

    @Then("^an export package \"(.*)\" is available$")
    public void then_export_file_name(String name) {

        assertThat(currentExport).isNotNull();
        assertThat(currentExport.getResult().getFilename()).isEqualTo(name);
    }

    @Then("^the export package contains (.*) commit contents$")
    public void then_export_content_size(int size) {
        assertThat(currentExport).isNotNull();
        assertThat(readPackageCommits()).hasSize(size);
    }


    @Then("^the export package content has these identified changes for commit with name \"(.*)\" :$")
    public void commit_content_changes(String name, DataTable data) {

        // Get by tables
        Map<String, List<Map<String, String>>> tables = data.asMaps(String.class, String.class).stream().collect(Collectors.groupingBy(i -> i.get("Table")));

        // Get from package
        readPackageCommits().stream().filter(c -> c.getComment().equals(name)).forEach(
                c -> {
                    CommitDetails details = CommitDetails.fromEntity(c);

                    // Complete content with HR playload details for easy test on payload
                    this.commitService.completeCommitDetailsWithIndexForProjectDict(getCurrentUserProject(), details, new ArrayList<>(c.getIndex()));

                    tables.forEach((t, v) -> {
                        DiffDisplay<?> content = details.getContent().stream()
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
                });
    }


    @Then("^the export package content has these associated lob data for commit with name \"(.*)\" :$")
    public void then_commit_contains_lobs(String name, DataTable table) {


        Map<String, String> datas = table.asMaps(String.class, String.class).stream().collect(Collectors.toMap(i -> i.get("hash"), i -> i.get("data")));

        // Get from package
        UUID commitUUID = readPackageCommits().stream()
                .filter(c -> c.getComment().equals(name))
                .map(Commit::getUuid)
                .findFirst()
                .orElseThrow(() -> new AssertionError("Cannot find content for commit with name " + name + " in exported package"));

        // Get corresponding lobs from package
        List<LobProperty> lobs = readPackageLobs().stream().filter(l -> l.getCommit().getUuid().equals(commitUUID)).collect(Collectors.toList());

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

    /**
     * Easy access to exported content (reuse internal export, so will not test export process itself)
     *
     * @return
     */
    private List<SharedPackage<?>> readPackages() {
        return this.exportImportService.importPackages(currentExport.getResult());
    }

    private List<Commit> readPackageCommits() {
        return readPackages().stream()
                .filter(s -> s.getClass() == CommitPackage.class)
                .map(p -> (CommitPackage) p)
                .map(SharedPackage::getContent)
                .flatMap(List::stream)
                .collect(Collectors.toList());
    }

    private List<LobProperty> readPackageLobs() {
        return readPackages().stream()
                .filter(s -> s.getClass() == LobPropertyPackage.class)
                .map(p -> (LobPropertyPackage) p)
                .map(SharedPackage::getContent)
                .flatMap(List::stream)
                .collect(Collectors.toList());
    }
}
