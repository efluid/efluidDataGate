package fr.uem.efluid.system.tests.fixtures;

import cucumber.api.Delimiter;
import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import fr.uem.efluid.services.types.VersionData;
import fr.uem.efluid.system.common.SystemTest;
import org.junit.Assert;
import org.junit.Before;

import java.net.URLEncoder;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author elecomte
 * @version 1
 * @since v0.0.8
 */
public class VersionFixtures extends SystemTest {

    private static List<String> specifiedVersions;

    private static LocalDateTime initUpdatedTime;

    @Before
    public void resetFixture() {
        specifiedVersions = null;
        initUpdatedTime = null;
    }

    @Given("^the existing versions (.*)$")
    public void the_existing_versions(@Delimiter(", ") List<String> versions) throws Throwable {

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
}
