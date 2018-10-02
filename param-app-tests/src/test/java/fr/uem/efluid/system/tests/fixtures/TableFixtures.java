package fr.uem.efluid.system.tests.fixtures;

import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.isIn;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;

import java.util.Arrays;
import java.util.List;

import org.junit.Before;

import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
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

	@Then("^the existing tables are displayed$")
	public void the_existing_tables_are_displayed() throws Throwable {

		currentAction = currentAction.andExpect(model().attribute("tables", allOf(
				hasItems(hasProperty("tableName", isIn(specifiedTables)))// Values
		)));
	}
}
