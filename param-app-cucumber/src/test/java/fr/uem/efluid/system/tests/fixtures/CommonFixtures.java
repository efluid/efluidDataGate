package fr.uem.efluid.system.tests.fixtures;

import cucumber.api.DataTable;
import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import fr.uem.efluid.system.common.SystemTest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * @author elecomte
 * @version 1
 * @since v0.0.8
 */
public class CommonFixtures extends SystemTest {

    public static boolean efluidCase = false;

    @Given("^the test is an Efluid standard scenario$")
    public void efluid_test_case() {
        efluidCase = true;
    }

    @Given("^the application is after a fresh install$")
    public void the_application_is_after_a_fresh_install() throws Throwable {
        // Nothing : no database init
    }

    @Given("^the dictionary is fully initialized with tables 1, 2 and 3$")
    public void the_dictionary_is_fully_initialized_with_tables() throws Throwable {

        if(efluidCase){
            initCompleteDictionaryWithEfluidTestTables();
        } else {
            initCompleteDictionaryWith7Tables();
        }

        efluidCase = false;
    }

    @Given("^the user accesses to the destination environment with the same dictionary$")
    public void user_witch_environment_same_dic() {

        // For this, we simply keep current dict, and drop all indexes + Test tables datas
        backlogDatabase().dropBacklog();
        managedDatabase().dropManaged();
    }

    @When("^(.+) access to (.+)$")
    public void user_access_to_page(String user, String page) throws Throwable {

        String login = cleanUserParameter(user);

        if (login != null) {
            // Authenticate
        }

        // Then go to page
        String link = getCorrespondingLinkForPageName(page);

        get(link);
    }

    @Then("^the user is (.+) to (.+)$")
    public void the_user_is_oriented_to_page(String action, String page) throws Throwable {

        String link = getCorrespondingLinkForPageName(page);

        if ("forwarded".equals(action.trim())) {
            currentAction = currentAction.andExpect(status().isOk()).andExpect(forwardedUrl(link));
        } else {
            currentAction = currentAction.andExpect(status().is3xxRedirection()).andExpect(redirectedUrl(link));
        }
    }

    @Then("^the provided template is (.*)$")
    public void the_provided_template_is_name(String name) throws Throwable {

        String template = getCorrespondingTemplateForName(name);

        currentAction = currentAction.andExpect(view().name(template));
    }

    @Given("^the user is currently on (.*)$")
    public void the_user_is_currently_on_page(String page) throws Throwable {

        currentStartPage = getCorrespondingLinkForPageName(page);
    }

    @Given("^the existing data in managed table \"(.*)\" :$")
    public void existing_data_in_managed_table(String name, DataTable data) {
        managedDatabase().initTab(name, data);
    }

    @Given("^the data in managed table \"(.*)\" is now :$")
    public void check_data_in_managed_table(String name, DataTable data) {
        managedDatabase().assertCurrentTabComplies(name, data);
    }

    @Given("^the user do nothing more$")
    public void does_nothing() {
        // Nothing
    }

    @Given("^the existing data in managed table \"(.*)\" in destination environment :$")
    public void existing_data_in_managed_table_in_dest(String name, DataTable data) {
        // Switched on same db
        existing_data_in_managed_table(name, data);
    }

    @Given("^no existing data in managed table \"(.*)\" in destination environment$")
    public void no_data_in_managed_table_in_dest(String name) {

        // Already dropped by switch to destination
        assertThat(managedDatabase().countTable(name)).isEqualTo(0);
    }

    @Then("^the data in managed table \"(.*)\" in destination environment is now :$")
    public void check_data_in_managed_table_in_dest(String name, DataTable data) {
        // Switched on same db
        check_data_in_managed_table(name, data);
    }

    @Given("^these changes are applied to table \"(.*)\" :$")
    public void updated_data_in_managed_table(String name, DataTable data) {
        managedDatabase().updateTab(name, data);
    }

    @Given("^these changes are applied to table \"(.*)\" in destination environment :$")
    public void updated_data_in_managed_table_in_dest(String name, DataTable data) {
        // Switched env use same database
        updated_data_in_managed_table(name, data);
    }

    /**
     * <p>
     * A common test step with :
     * <ul>
     * <li>Wizzard init with default data</li>
     * <li>A default "any" user is authenticated</li>
     * <li>The user is already on home page</li>
     * </p>
     * <p>
     * Can be reused as default process spec
     * </p>
     *
     * @param page
     * @throws Throwable
     */
    @Given("^from (.*)$")
    public void from_page(String page) throws Throwable {

        // Implicit wizzard init
        initMinimalWizzardData();

        // Implicit authentication
        implicitlyAuthenticatedAndOnPage(page);
    }

}
