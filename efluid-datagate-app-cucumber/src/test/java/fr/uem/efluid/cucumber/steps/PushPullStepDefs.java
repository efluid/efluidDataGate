package fr.uem.efluid.cucumber.steps;

import fr.uem.efluid.cucumber.common.CucumberStepDefs;
import fr.uem.efluid.model.entities.Commit;
import fr.uem.efluid.model.entities.IndexAction;
import fr.uem.efluid.model.entities.LobProperty;
import fr.uem.efluid.model.entities.TransformerDef;
import fr.uem.efluid.services.types.*;
import fr.uem.efluid.tools.Transformer;
import fr.uem.efluid.utils.FormatUtils;
import io.cucumber.datatable.DataTable;
import io.cucumber.docstring.DocString;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.junit.Ignore;

import java.util.*;
import java.util.stream.Collectors;

import static fr.uem.efluid.model.entities.IndexAction.REMOVE;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author elecomte
 * @version 1
 * @since v0.0.8
 */
@Ignore // Means it will be ignored by junit start, but will be used by cucumber
public class PushPullStepDefs extends CucumberStepDefs {

    static ExportImportResult<ExportFile> currentExport;

    static Exception currentException;

    @When("^the user request an export of all the commits$")
    public void when_export_all_commits() {
        currentExport = processCommitExportWithoutTransformerCustomization(CommitExportEditData.CommitSelectType.RANGE_FROM, null);
    }

    @When("^the user request an export of the commit with name \"(.*)\"$")
    public void when_export_one_commit(String name) {
        UUID specifiedCommit = backlogDatabase().searchCommitWithName(getCurrentUserProject(), name);
        currentExport = processCommitExportWithoutTransformerCustomization(CommitExportEditData.CommitSelectType.SINGLE_ONE, specifiedCommit);
    }

    @Given("^the user has requested an export of the commit with name \"(.*)\"$")
    public void given_export_one_commit(String name) {
        UUID specifiedCommit = backlogDatabase().searchCommitWithName(getCurrentUserProject(), name);
        currentExport = processCommitExportWithoutTransformerCustomization(CommitExportEditData.CommitSelectType.SINGLE_ONE, specifiedCommit);
    }

    @Given("^the user has requested an export of the commit with name \"(.*)\" and no customization on transformers$")
    public void given_export_one_commit_no_transformer_customization(String name) {
        // Use default
        given_export_one_commit(name);
    }

    @Given("^the user has requested an export of the commit with name \"(.*)\" and this customization for transformer \"(.*)\" :$")
    public void given_export_one_commit_with_transformer_customization(String name, String transformer, DocString config) throws Exception {

        // Access customization
        when_prepare_export_single(name);

        // Customize
        CommitExportEditData exportEditData = getCurrentSpecifiedProperty("exportEdit", CommitExportEditData.class);
        List<TransformerDefDisplay> transformers = getCurrentSpecifiedPropertyList("transformerDefs", TransformerDefDisplay.class);

        Optional<TransformerDefDisplay> transformerDefDisplay = transformers.stream().filter(t -> t.getName().equals(transformer)).findFirst();

        assertThat(transformerDefDisplay).isPresent();

        UUID truid = transformerDefDisplay.get().getUuid();

        if (exportEditData.getSpecificTransformerConfigurations() == null) {
            exportEditData.setSpecificTransformerConfigurations(new HashMap<>());
        }

        exportEditData.getSpecificTransformerConfigurations().put(truid, config.getContent());

        // Export
        when_post_prepared_export();

        // Download
        then_export_download();
    }

    @Given("^the user has requested an export starting by the commit with name \"(.*)\"$")
    public void given_export_start_by_commit(String name) {
        UUID specifiedCommit = backlogDatabase().searchCommitWithName(getCurrentUserProject(), name);
        currentExport = processCommitExportWithoutTransformerCustomization(CommitExportEditData.CommitSelectType.RANGE_FROM, specifiedCommit);
    }

    @When("^the user import the available source package$")
    public void when_import_current_package() {
        try {
            this.prep.startMergeCommitPreparation(currentExport.getResult());
        } catch (Exception e) {
            // Do not fail at launch
            currentException = e;
        }
    }

    @When("^the user request to prepare an export of the commit with name \"(.*)\"$")
    public void when_prepare_export_single(String name) throws Exception {
        UUID specifiedCommit = backlogDatabase().searchCommitWithName(getCurrentUserProject(), name);
        get("/ui/push/prepare/" + specifiedCommit.toString() + "/SINGLE_ONE");
    }

    @When("^the user request to prepare an export of all commits$")
    public void when_prepare_export_all() throws Exception {
        get("/ui/push/prepare/ALL/RANGE_FROM");
    }

    @When("^the user request to prepare an export starting by the commit with name \"(.*)\"$")
    public void when_prepare_export_range(String name) throws Exception {
        UUID specifiedCommit = backlogDatabase().searchCommitWithName(getCurrentUserProject(), name);
        get("/ui/push/prepare/" + specifiedCommit.toString() + "/RANGE_FROM");
    }

    @When("^the user validate the prepared export$")
    public void when_post_prepared_export() throws Exception {
        CommitExportEditData data = getCurrentSpecifiedProperty("exportEdit", CommitExportEditData.class);

        PostParamSet params = postParams()
                .with("commitSelectType", data.getCommitSelectType())
                .with("selectedCommitUuid", data.getSelectedCommitUuid());

        // Add customization using a spring-mvc bean "map model"
        data.getSpecificTransformerConfigurations()
                .forEach((k, v) -> params.with("specificTransformerConfigurations[" + k + "]", v));

        post("/ui/push/save", params);
    }

    @Then("^the export download start automatically$")
    public void then_export_download() {

        CommitExportDisplay ready = getCurrentSpecifiedProperty("ready", CommitExportDisplay.class);

        // Check : has the required item for automatic download start
        assertThat(ready).isNotNull();

        // Check : Not downloaded
        assertThat(commitService.isCommitExportDownloaded(ready.getUuid())).isFalse();

        // Simulate frontend download : Process export / download
        currentExport = commitService.processCommitExport(ready.getUuid());

        // Check : Downloaded
        assertThat(commitService.isCommitExportDownloaded(ready.getUuid())).isTrue();
    }

    @Then("^an export package \"(.*)\" is available$")
    public void then_export_file_name(String name) {

        assertThat(currentExport).isNotNull();
        assertThat(currentExport.getResult().getFilename()).isEqualTo(name);
    }

    @Then("^the export package contains (.*) commit contents$")
    public void then_export_content_size(int size) {
        assertThat(currentExport).isNotNull();
        List<Commit> commits = readPackageCommits();

        // Test only commit with index content (not reference commits)
        assertThat(commits.stream().filter(c -> c.getIndex() != null && c.getIndex().size() > 0)).hasSize(size);
    }

    @Then("^the exported commit \"(.*)\" is not present in the destination environment$")
    public void commit_not_imported_in_dest(String name) {

        List<Commit> commits = readPackageCommits();

        // Get from package
        UUID commitUUID = commits.stream()
                .filter(c -> c.getComment().equals(name))
                .map(Commit::getUuid)
                .findFirst()
                .orElseThrow(() -> new AssertionError("Cannot find content for commit with name " + name + " in exported package"));

        assertThat(this.commitService.getAvailableCommits()).noneMatch(c -> c.getUuid().equals(commitUUID) || c.getMergeSources().contains(commitUUID));
    }

    @Then("^the exported commit \"(.*)\" is present and merged in the destination environment$")
    public void commit_imported_in_dest(String name) {

        // Get from package
        UUID commitUUID = readPackageCommits().stream()
                .filter(c -> c.getComment().equals(name))
                .map(Commit::getUuid)
                .findFirst()
                .orElseThrow(() -> new AssertionError("Cannot find content for commit with name " + name + " in exported package"));

        assertThat(this.commitService.getAvailableCommits()).anyMatch(c -> c.getUuid().equals(commitUUID) || c.getMergeSources().contains(commitUUID));
    }

    @Then("^the export package content has these identified changes for commit with name \"(.*)\" :$")
    public void commit_content_changes(String name, DataTable data) {

        // Get by tables
        Map<String, List<Map<String, String>>> tables = data.asMaps().stream().collect(Collectors.groupingBy(i -> i.get("Table")));

        // Commits (ignoring ref commits)
        List<Commit> commits = readPackageCommits().stream().filter(c -> c.getIndex() != null && c.getIndex().size() > 0).collect(Collectors.toList());

        // Check this commit content exists
        assertThat(commits).anyMatch(c -> c.getComment() != null && c.getComment().equals(name));

        // Check commit contents
        commits.stream()
                .filter(c -> c.getComment() != null && c.getComment().equals(name)).forEach(
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


        Map<String, String> datas = table.asMaps().stream().collect(Collectors.toMap(i -> i.get("hash"), i -> i.get("data")));

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

    @Then("^the preparing export is displayed as type \"(.*)\" for commit \"(.*)\"$")
    @SuppressWarnings("unchecked")
    public void then_export_details(String type, String commit) {

        assertModelIsSpecifiedProperty("exportEdit", CommitExportEditData.class,
                a -> a.extracting(e -> e.getCommitSelectType().name()).isEqualTo(type),
                a -> a.extracting(CommitExportEditData::getSelectedCommitComment).isEqualTo(commit)
        );
    }

    @Then("^no transformers are listed for customization$")
    public void then_export_no_transformers() {
        assertThat(getCurrentSpecifiedProperty("exportEdit", CommitExportEditData.class).getSpecificTransformerConfigurations()).hasSize(0);
    }

    @Given("^the export package content has these transformer definitions :$")
    public void export_contains_transformers(DataTable table) {

        // Packkage content
        List<TransformerDef> trans = readPackageTransformers();
        List<Map<String, String>> data = table.asMaps();

        assertThat(trans).hasSize(data.size());

        data.forEach(s -> {
            Optional<TransformerDef> tran = trans.stream().filter(t -> t.getName().equals(s.get("name"))).findFirst();

            assertThat(tran).isPresent();

            Transformer<?, ?> transformer = getTransformerByName(s.get("type"));

            assertThat(tran).get().extracting(TransformerDef::getType).isEqualTo(transformer.getClass().getSimpleName());
            assertThat(tran).get().extracting(TransformerDef::getPriority).isEqualTo(Integer.parseInt(s.get("priority")));
            assertThat(tran).get().matches(t -> jsonEquals(t.getConfiguration(), s.get("configuration")));
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

    private List<TransformerDef> readPackageTransformers() {
        return readPackages().stream()
                .filter(s -> s.getClass() == TransformerDefPackage.class)
                .map(p -> (TransformerDefPackage) p)
                .map(SharedPackage::getContent)
                .flatMap(List::stream)
                .collect(Collectors.toList());
    }

}
