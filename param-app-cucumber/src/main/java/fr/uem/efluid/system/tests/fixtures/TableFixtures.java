package fr.uem.efluid.system.tests.fixtures;

import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.isIn;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import org.junit.Assert;
import org.junit.Before;

import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import fr.uem.efluid.services.types.DictionaryEntryEditData;
import fr.uem.efluid.system.common.SystemTest;
import fr.uem.efluid.system.stubs.ManagedDatabaseAccess;

/**
 * @author elecomte
 * @since v0.0.8
 * @version 1
 */
@SuppressWarnings("unchecked")
public class TableFixtures extends SystemTest {

	private static List<String> specifiedTables;

	private static PostParamSet params;

	@Before
	public void resetFixture() {
		specifiedTables = null;
	}

	@Given("^a managed database with two tables$")
	public void a_managed_database_with_two_tables() throws Throwable {

		// Implicit init with default domain
		initMinimalWizzardData();

		// Implicit authentication and on page
		implicitlyAuthenticatedAndOnPage("parameter table main page");

		// Init default data to tables
		managedDatabase().initTabOneData(15, "preset", "something", "value");
		managedDatabase().initTabTwoData(27, "key", "value", "other");

		specifiedTables = Arrays.asList(ManagedDatabaseAccess.TABLE_ONE, ManagedDatabaseAccess.TABLE_TWO);
	}

	@Given("^the user is on new parameter table page$")
	public void the_user_is_on_new_parameter_table_page() throws Throwable {

		// Current page
		currentStartPage = getCorrespondingLinkForPageName("new parameter table page");
	}

	@Given("^a prepared parameter table data with name \"(.*)\"$")
	public void a_prepared_parameter_table_data(String name) throws Throwable {

		// Implicit init with tables
		a_managed_database_with_two_tables();

		// Implicit select
		get(getCorrespondingLinkForPageName("new parameter table page") + "/" + ManagedDatabaseAccess.TABLE_ONE);

		// Get provided data to post them updated
		DictionaryEntryEditData data = (DictionaryEntryEditData) currentAction.andReturn()
				.getModelAndView().getModel().get("entry");

		data.getColumns().forEach(c -> {
			if (c.getName().equals("VALUE")) {
				c.setKey(true);
			} else {
				c.setSelected(true);
			}
		});

		params = postParams().with("name", name)
				.with("domainUuid", data.getDomainUuid())
				.with("table", data.getTable())
				.with("where", data.getWhere())
				.with("columns", data.getColumns());
	}

	@When("^the user select one table to create$")
	public void the_user_select_one_table_to_create() throws Throwable {

		// Go to page new for TABLE_ONE
		get(currentStartPage + "/" + ManagedDatabaseAccess.TABLE_ONE);
	}

	@When("^the parameter table is saved by user$")
	public void the_parameter_table_is_saved_by_user() throws Throwable {

		// Post prepared data
		post(getCorrespondingLinkForPageName("save parameter table"), params);

	}

	@Then("^the existing tables are displayed$")
	public void the_existing_tables_are_displayed() throws Throwable {

		// Check list of tables
		currentAction = currentAction.andExpect(model().attribute("tables", allOf(
				hasItems(hasProperty("tableName", isIn(specifiedTables)))// Values
		)));
	}

	@Then("^the selected table data are initialized$")
	public void the_selected_table_data_are_initialized() throws Throwable {

		// Check edit entry details
		currentAction = currentAction.andExpect(model()
				.attribute("entry", allOf(// Values
						hasProperty("name", equalTo(ManagedDatabaseAccess.TABLE_ONE)),
						hasProperty("table", equalTo(ManagedDatabaseAccess.TABLE_ONE)))));

		DictionaryEntryEditData data = (DictionaryEntryEditData) currentAction.andReturn().getModelAndView().getModel().get("entry");

		data.getColumns().stream().allMatch(c -> managedDatabase().getColumnMatchersForTableOne().stream().anyMatch(m -> m.matches(c)));
	}

	@Then("^the default domain is automatically selected$")
	public void the_default_domain_is_automatically_selected() throws Throwable {

		UUID domainUuid = getDefaultDomainFromCurrentProject().getUuid();

		// Check domain uuid in edit entry details
		currentAction = currentAction.andExpect(model()
				.attribute("entry", hasProperty("domainUuid", equalTo(domainUuid))));
	}

	@Then("^the parameter table is added to the current user's project dictionary$")
	public void the_parameter_table_is_added_to_the_current_user_project_dictionary() throws Throwable {
		Assert.assertNotNull(
				modelDatabase().findDictionaryEntryByProjectAndTableName(getCurrentUserProject(), ManagedDatabaseAccess.TABLE_ONE));
	}
}
