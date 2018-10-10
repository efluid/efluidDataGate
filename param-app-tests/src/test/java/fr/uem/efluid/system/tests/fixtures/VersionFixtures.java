package fr.uem.efluid.system.tests.fixtures;

import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.isIn;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;

import java.net.URLEncoder;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Assert;
import org.junit.Before;

import cucumber.api.Delimiter;
import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import fr.uem.efluid.system.common.SystemTest;

/**
 * @author elecomte
 * @since v0.0.8
 * @version 1
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
	public void the_existing_versions(@Delimiter(",") List<String> versions) throws Throwable {

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

	@When("^the user update version (.*)$")
	public void the_user_update_version(String name) throws Throwable {

		// Get actual update date
		initUpdatedTime = modelDatabase().findVersionByProjectAndName(getCurrentUserProject(), name).getUpdatedTime();

		// Update
		post("/ui/versions/" + URLEncoder.encode(name, "UTF-8"));

		// Update is REST only, update page
		get(getCorrespondingLinkForPageName("list of versions"));
	}

	@SuppressWarnings("unchecked")
	@Then("^the (\\d+) \\w+ versions are displayed$")
	public void the_x_versions_are_displayed(int nbr) throws Throwable {

		currentAction.andReturn().getModelAndView().getModel();
		
		currentAction = currentAction.andExpect(model().attribute("versions", allOf(
				hasSize(nbr), // List size ok
				hasItems(hasProperty("name", isIn(specifiedVersions)))// Values
		)));
	}

	@Then("^the update date of version (.*) is updated$")
	public void the_update_date_of_version_is_updated(String name) throws Throwable {

		Assert.assertNotEquals(initUpdatedTime,
				modelDatabase().findVersionByProjectAndName(getCurrentUserProject(), name).getUpdatedTime());
	}
}
