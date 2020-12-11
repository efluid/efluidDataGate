package fr.uem.efluid.cucumber.steps;

import fr.uem.efluid.ColumnType;
import fr.uem.efluid.cucumber.common.CucumberStepDefs;
import fr.uem.efluid.model.entities.DictionaryEntry;
import fr.uem.efluid.model.entities.Version;
import fr.uem.efluid.services.DictionaryManagementService;
import fr.uem.efluid.services.types.VersionCompare;
import fr.uem.efluid.services.types.VersionData;
import fr.uem.efluid.tools.VersionContentChangesGenerator;
import fr.uem.efluid.utils.DataGenerationUtils;
import io.cucumber.datatable.DataTable;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.springframework.beans.factory.annotation.Autowired;

import java.net.URLEncoder;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author elecomte
 * @version 1
 * @since v0.0.8
 */
@Ignore // Means it will be ignored by junit start, but will be used by cucumber
public class VersionStepDefs extends CucumberStepDefs {

    private static List<String> specifiedVersions;

    private static LocalDateTime initUpdatedTime;

    @Autowired
    private DictionaryManagementService dictionaryManagementService;

    @Before
    public void resetFixture() {
        specifiedVersions = null;
        initUpdatedTime = null;
    }

    @Given("^the existing versions \"(.*)\"$")
    public void the_existing_versions(String versionsRaw) throws Throwable {

        List<String> versions = Stream.of(versionsRaw.split(", ")).collect(Collectors.toList());

        // Implicit init with default domain / project
        initMinimalWizzardData();

        // Implicit authentication and on page
        implicitlyAuthenticatedAndOnPage("list of versions");

        // Init with specified versions
        modelDatabase().initVersions(getCurrentUserProject(), versions, 1);
        // Keep version for update check
        specifiedVersions = versions;
    }

    @Given("^a version \"(.*)\" is defined$")
    public void a_version_x_is_defined(String name) throws Throwable {

        // Just setup a new update version
        modelDatabase().initVersions(getCurrentUserProject(), Arrays.asList(name), 50);
    }

    @When("^the user add new version \"(.*)\".*$")
    public void the_user_add_new_version(String name) throws Throwable {
        // Add new
        post("/ui/versions/" + URLEncoder.encode(name, "UTF-8"));

        // List can be unmodifiable, reset it
        List<String> updatedVersions = new ArrayList<>();

        updatedVersions.addAll(specifiedVersions);

        updatedVersions.add(name);

        specifiedVersions = updatedVersions;

        // Update is REST only, update page
        get(getCorrespondingLinkForPageName("list of versions"));

    }

    @When("^the user request to compare the version \"(.*)\" with last version$")
    public void the_user_compare_versions(String version) throws Exception {
        get("/ui/versions/compare/" + version);
    }

    @When("^the user delete version \"(.*)\"$")
    public void the_user_delete_version(String name) throws Throwable {
        UUID versionUuid = modelDatabase().findVersionByProjectAndName(getCurrentUserProject(), name).getUuid();

        post("/ui/versions/remove/" + versionUuid);

        // List can be unmodifiable, reset it
        List<String> updatedVersion = new ArrayList<>();

        updatedVersion.addAll(specifiedVersions);
        updatedVersion.remove(name);

        specifiedVersions = updatedVersion;

        // Update is REST only, update page
        get(getCorrespondingLinkForPageName("list of versions"));
    }

    @Given("^the version (.*) has no lots")
    public void the_version_x_has_no_lots(String name) throws Throwable {
        assertThat(this.commitService.getAvailableCommits()).noneMatch((c) -> {
            return name.equals(c.getVersionName());
        });
    }

    @Given("^the existing version in destination environment is different$")
    public void given_modified_version() {// Force modify version to make it different
        modelDatabase().forceUpdateVersion(getCurrentUserProject());
    }

    @When("^the user update version \"(.*)\"$")
    public void the_user_update_version(String name) throws Throwable {

        // Get actual update date
        initUpdatedTime = modelDatabase().findVersionByProjectAndName(getCurrentUserProject(), name).getUpdatedTime();

        // Update - it's a navigation also
        post("/ui/versions", p("name", name));
    }

    @Then("^the (\\d+) \\w+ versions are displayed$")
    public void the_x_versions_are_displayed(int nbr) {

        assertModelIsSpecifiedListWithProperties(
                "versions",
                nbr,
                VersionData::getName,
                specifiedVersions);

    }

    @Then("^a confirmation message on update is displayed$")
    public void then_update_version_msg() {
        assertModelIsSpecifiedProperty("updateDone", Boolean.class, v -> v != null && v);
    }

    @Then("^the current version name is \"(.*)\"$")
    public void the_current_version_name_is(String name) {

        VersionData current = this.dictionaryManagementService.getLastVersion();

        assertThat(current).isNotNull();
        assertThat(current.getName()).isEqualTo(name);
    }

    @Then("^the user cannot add a new version$")
    public void the_user_cannot_add_version() {

        // Use page context property
        assertModelIsSpecifiedProperty("canCreateVersion", Boolean.class, v -> !v);
    }

    @Then("^the user can add a new version$")
    public void the_user_can_add_version() {

        // Use page context property
        assertModelIsSpecifiedProperty("canCreateVersion", Boolean.class, v -> v);
    }

    @Then("^the update date of version \"(.*)\" is updated$")
    public void the_update_date_of_version_is_updated(String name) {

        Assert.assertNotEquals(initUpdatedTime,
                modelDatabase().findVersionByProjectAndName(getCurrentUserProject(), name).getUpdatedTime());
    }

    @Then("^the version (.*) contains this dictionary content :$")
    public void the_version_contains_dict_content(String name, DataTable data) {

        List<Map<String, String>> content = data.asMaps(String.class, String.class);

        Version version = modelDatabase().findVersionByProjectAndName(getCurrentUserProject(), name);

        assertThat(version.getDictionaryContent()).isNotBlank();

        // Need gen to get extracted content
        VersionContentChangesGenerator gen = new VersionContentChangesGenerator(
                null // Query Gen is not used for reading
        );

        List<DictionaryEntry> contentTables = new ArrayList<>();

        gen.readVersionContent(
                version,
                new ArrayList<>(),
                contentTables, // Will use only tables
                new ArrayList<>(),
                new ArrayList<>()
        );

        List<DictionaryEntry> modelTables = content.stream().map(m ->
                DataGenerationUtils.entry(
                        m.get("entry name"),
                        null,
                        m.get("select clause"),
                        m.get("table name"),
                        m.get("filter clause"),
                        m.get("key name"),
                        ColumnType.valueOf(m.get("key type"))
                )).collect(Collectors.toList());

        assertThat(contentTables).hasSize(modelTables.size());

        contentTables.forEach(t -> {
            DictionaryEntry model = modelTables.stream()
                    .filter(d -> d.getParameterName().equals(t.getParameterName()))
                    .findFirst()
                    .orElseThrow(() ->
                            new AssertionError("Cannot find model for table with name \""
                                    + t.getParameterName() + "\""));

            assertThat(t.getSelectClause()).isEqualTo(model.getSelectClause());
            assertThat(t.getTableName()).isEqualTo(model.getTableName());
            assertThat(t.getWhereClause()).isEqualTo(model.getWhereClause());
            assertThat(t.getKeyName()).isEqualTo(model.getKeyName());
            assertThat(t.getKeyType()).isEqualTo(model.getKeyType());

        });
    }

    @Then("^these domain changes are identified for the dictionary content :$")
    public void dict_change_domains(DataTable data) {

        List<Map<String, String>> content = data.asMaps(String.class, String.class);

        VersionCompare compare = getCurrentSpecifiedProperty("compare", VersionCompare.class);

        assertThat(compare.getDomainChanges()).hasSize(content.size());

        Map<String, VersionCompare.DomainChanges> domainChanges = compare.getDomainChanges().stream().collect(Collectors.toMap(VersionCompare.DomainChanges::getName, d -> d));

        content.forEach(c -> {
            VersionCompare.DomainChanges domainChange = domainChanges.get(c.get("domain"));
            assertThat(domainChange).as("domain %s", c.get("domain")).isNotNull();
            assertThat(domainChange.getChangeType().name()).as("type of change for domain %s", c.get("domain")).isEqualTo(c.get("change"));
            assertThat(domainChange.getUnmodifiedTableCount()).as("unmodified table count for domain %s", c.get("domain")).isEqualTo(Long.valueOf(c.get("unmodified table count")));
        });
    }

    @Then("^these table changes are identified for the dictionary content :$")
    public void dict_change_tables(DataTable data) {

        List<Map<String, String>> content = data.asMaps(String.class, String.class);

        VersionCompare compare = getCurrentSpecifiedProperty("compare", VersionCompare.class);

        Map<String, VersionCompare.DomainChanges> domainChanges = compare.getDomainChanges().stream().collect(Collectors.toMap(VersionCompare.DomainChanges::getName, d -> d));

        content.forEach(c -> {
            VersionCompare.DomainChanges domainChange = domainChanges.get(c.get("domain"));
            assertThat(domainChange).as("domain %s", c.get("domain")).isNotNull();
            assertThat(domainChange.getTableChanges().size()).as("table count for domaine %s", c.get("domain")).isEqualTo(content.stream().filter(d -> d.get("domain").equals(c.get("domain"))).count());
            Optional<VersionCompare.DictionaryTableChanges> tableChange = domainChange.getTableChanges().stream().filter(t -> t.getTableName().equals(c.get("table"))).findFirst();
            assertThat(tableChange).as("table %s", c.get("table")).isPresent();
            assertThat(tableChange.get().getChangeType().name()).as("type of change for table %s", c.get("table")).isEqualTo(c.get("change"));
            assertThat(tableChange.get().getTableNameChange() + " -> " + tableChange.get().getTableName()).as("table change for table %s", c.get("table")).isEqualTo(c.get("table change"));
            assertThat(tableChange.get().getNameChange() + " -> " + tableChange.get().getName()).as("name change for table %s", c.get("table")).isEqualTo(c.get("name change"));
            assertThat(tableChange.get().getFilterChange() + " -> " + tableChange.get().getFilter()).as("filter change for table %s", c.get("table")).isEqualTo(c.get("filter change"));

            assertThat(tableChange.get().getColumnChanges().stream().filter(h -> h.getChangeType() != VersionCompare.ChangeType.UNCHANGED).count())
                    .isEqualTo(Long.valueOf(c.get("column change count")));
        });
    }

    @Then("^these column changes are identified for the dictionary content :$")
    public void dict_change_columns(DataTable data) {

        List<Map<String, String>> content = data.asMaps(String.class, String.class);

        VersionCompare compare = getCurrentSpecifiedProperty("compare", VersionCompare.class);

        Map<String, VersionCompare.DomainChanges> domainChanges = compare.getDomainChanges().stream().collect(Collectors.toMap(VersionCompare.DomainChanges::getName, d -> d));

        content.forEach(c -> {
            VersionCompare.DomainChanges domainChange = domainChanges.get(c.get("domain"));
            assertThat(domainChange).as("domain %s", c.get("domain")).isNotNull();
            Optional<VersionCompare.DictionaryTableChanges> tableChange = domainChange.getTableChanges().stream().filter(t -> t.getTableName().equals(c.get("table"))).findFirst();
            assertThat(tableChange).as("table %s", c.get("table")).isPresent();
            assertThat(tableChange.get().getColumnChanges().size()).as("number of columns for table %s", c.get("table")).isEqualTo(content.stream().filter(d -> d.get("column").equals(c.get("column"))).count());
            Optional<VersionCompare.ColumnChanges> columnChanges = tableChange.get().getColumnChanges().stream().filter(n -> n.getName().equals(c.get("column"))).findFirst();
            assertThat(columnChanges).as("columns for table %s", c.get("table")).isPresent();
            assertThat(columnChanges.get().getChangeType().name()).as("type of change for column %s.%s", c.get("table"), c.get("column")).isEqualTo(c.get("change"));
            assertThat(columnChanges.get().getLinkChange() + " -> " + columnChanges.get().getLinkChange()).as("link change for column %s.%s", c.get("table"), c.get("column")).isEqualTo(c.get("link change"));
            assertThat(columnChanges.get().isKeyChange() + " -> " + columnChanges.get().isKey()).as("key change for column %s.%s", c.get("table"), c.get("column")).isEqualTo(c.get("key change"));
        });
    }
}
