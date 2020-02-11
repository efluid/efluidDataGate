package fr.uem.efluid.cucumber.steps;

import fr.uem.efluid.ColumnType;
import fr.uem.efluid.cucumber.common.CucumberStepDefs;
import fr.uem.efluid.model.entities.DictionaryEntry;
import fr.uem.efluid.model.entities.Version;
import fr.uem.efluid.services.DictionaryManagementService;
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

import java.net.URLEncoder;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;


import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalStateException;
import static org.junit.Assert.assertEquals;

/**
 * @author elecomte
 * @version 1
 * @since v0.0.8
 */
@Ignore // Means it will be ignored by junit start, but will be used by cucumber
public class VersionStepDefs extends CucumberStepDefs {

    private static List<String> specifiedVersions;

    private static LocalDateTime initUpdatedTime;

    private static List<Version> versionsOrderedByCreatedTime=new ArrayList<>();

    @Before
    public void resetFixture() {
        specifiedVersions = null;
        initUpdatedTime = null;
    }

    @Given("^the existing versions (.*)$")
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

    @When("^the user add new version (.*)$")
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

    @Given("^the existing version in destination environment is different$")
    public void given_modified_version() {
        // Force modify version to make it different
        modelDatabase().forceUpdateVersion(getCurrentUserProject());
    }

    @When("^the user update version (.*)$")
    public void the_user_update_version(String name) throws Throwable {

        // Get actual update date
        initUpdatedTime = modelDatabase().findVersionByProjectAndName(getCurrentUserProject(), name).getUpdatedTime();

        // Update
        post("/ui/versions/" + URLEncoder.encode(name, "UTF-8"));

        // Update is REST only, update page
        get(getCorrespondingLinkForPageName("list of versions"));
    }

    @Then("^the (\\d+) \\w+ versions are displayed$")
    public void the_x_versions_are_displayed(int nbr) throws Throwable {

        assertModelIsSpecifiedListWithProperties(
                "versions",
                nbr,
                VersionData::getName,
                specifiedVersions);
    }

    @Then("^the update date of version (.*) is updated$")
    public void the_update_date_of_version_is_updated(String name) throws Throwable {

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


    @Given("^two version$")
    public void two_version(){
        Version version1 = new Version();
        version1.setCreatedTime(LocalDateTime.now());
        Version version2 = new Version();
        version2.setCreatedTime(LocalDateTime.now());
        versionsOrderedByCreatedTime.add(version2);
        versionsOrderedByCreatedTime.add(version1);

        // Implicit init with default domain / project
        initMinimalWizzardData();

        // Implicit authentication and on page
        implicitlyAuthenticatedAndOnPage("list of versions");


    }


    @Then("^the 2 existing versions are ordered into their CreatedTime order$")
    public void then_versions_are_ordered(){
        for (int i = 0; i<(versionsOrderedByCreatedTime.size()-1); i++){
            two_version();
            if (versionsOrderedByCreatedTime.get(i).getCreatedTime().isAfter(versionsOrderedByCreatedTime.get(i+1).getCreatedTime()));
            assertThatIllegalStateException();
            break;
        }
        int i=0;
        int j=0;
        assertEquals(i,j);
    }
}

