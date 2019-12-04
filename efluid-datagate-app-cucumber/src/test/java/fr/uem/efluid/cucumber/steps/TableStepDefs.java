package fr.uem.efluid.cucumber.steps;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.cucumber.datatable.DataTable;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import fr.uem.efluid.cucumber.common.CucumberStepDefs;
import fr.uem.efluid.model.entities.DictionaryEntry;
import fr.uem.efluid.services.types.DictionaryEntryEditData;
import fr.uem.efluid.services.types.TestQueryData;
import fr.uem.efluid.cucumber.stubs.ManagedDatabaseAccess;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;

/**
 * @author elecomte
 * @version 1
 * @since v0.0.8
 */
@Ignore // Means it will be ignored by junit start, but will be used by cucumber
@SuppressWarnings("unchecked")
public class TableStepDefs extends CucumberStepDefs {

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

    @Given("^the parameter table for managed table \"(.*)\" already exists$")
    public void existing_parameter_table(String table) throws Throwable {
        // Init one table in dict
        initDictionaryForDefaultVersionWithTables(getDefaultDomainFromCurrentProject(), getCurrentUserProject(), table);
    }

    @Given("^a prepared parameter table data with name \"(.*)\" for managed table \"(.*)\"$")
    public void a_prepared_parameter_table_data(String name, String table) throws Throwable {

        // Implicit init with tables
        a_managed_database_with_two_tables();

        // Implicit select
        get(getCorrespondingLinkForPageName("new parameter table page") + "/" + table);

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


    @Given("^a prepared parameter table data with name \"(.*)\" for managed table \"(.*)\" and columns selected as this :$")
    public void a_prepared_parameter_table_data_with_select_col(String name, String table, DataTable select) throws Throwable {

        // Implicit init with tables
        a_managed_database_with_two_tables();

        // Implicit select
        get(getCorrespondingLinkForPageName("new parameter table page") + "/" + table);

        // Get provided data to post them updated
        DictionaryEntryEditData data = (DictionaryEntryEditData) currentAction.andReturn()
                .getModelAndView().getModel().get("entry");

        // Apply selection
        select.asMaps()
                .forEach(s -> data.getColumns().stream()
                        .filter(c -> c.getName().equalsIgnoreCase(s.get("name")))
                        .forEach(c -> {
                            switch (s.get("selection")) {
                                case "ignored":
                                    c.setKey(false);
                                    c.setSelected(false);
                                    break;
                                case "key":
                                    c.setKey(true);
                                    c.setSelected(false);
                                    break;
                                case "selected":
                                    c.setKey(false);
                                    c.setSelected(true);
                                    break;
                            }
                        }));

        params = postParams().with("name", name)
                .with("domainUuid", data.getDomainUuid())
                .with("table", data.getTable())
                .with("where", data.getWhere())
                .with("columns", data.getColumns());
    }

    @When("^the user request a test on parameter table content$")
    public void when_user_request_test_content() throws Throwable {

        throw new AssertionError("Todo : get serialized content and post 'as this' ");
    }

    @When("^the user select one table to create$")
    public void the_user_select_one_table_to_create() throws Throwable {

        // Go to page new for TABLE_ONE
        get(currentStartPage + "/" + ManagedDatabaseAccess.TABLE_ONE);
    }

    @When("^the parameter table is (.*) by user$")
    public void the_parameter_table_is_saved_by_user(String type) throws Throwable {

        // Post prepared data
        post(getCorrespondingLinkForPageName(type + " parameter table"), params);

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
    public void the_parameter_table_is_added_to_the_current_user_project_dictionary() {
        // Fixed default
        the_parameter_table_is_added_to_the_current_user_project_dictionary(ManagedDatabaseAccess.TABLE_ONE);
    }

    @Then("^the parameter table for managed table \"(.*)\" is added to the current user's project dictionary$")
    public void the_parameter_table_is_added_to_the_current_user_project_dictionary(String name) {
        Assert.assertNotNull(
                modelDatabase().findDictionaryEntryByProjectAndTableName(getCurrentUserProject(), name));
    }

    @Then("^the selection clause for the parameter table for managed table \"(.*)\" is empty$")
    public void the_selection_clause_is_empty(String name) {
        DictionaryEntry dic = modelDatabase().findDictionaryEntryByProjectAndTableName(getCurrentUserProject(), name);
        assertThat(dic.getSelectClause()).isNullOrEmpty();
    }

    @Then("^the selection clause for the parameter table for managed table \"(.*)\" is equals to \"(.*)\"$")
    public void the_selection_clause_is_valid(String name, String clause) {
        DictionaryEntry dic = modelDatabase().findDictionaryEntryByProjectAndTableName(getCurrentUserProject(), name);
        assertThat(dic).isNotNull();
        assertThat(dic.getSelectClause()).isNotNull();
        assertThat(dic.getSelectClause().trim()).isEqualTo(clause.trim());
    }

    @Then("^the parameter table query result is provided with (.*) detailled lines from managed table \"(.*)\"$")
    public void the_parameter_table_query_result_is_provided(int detailCount, String tableName) throws Throwable {

        ObjectMapper mapper = new ObjectMapper();
        TestQueryData data = mapper.readValue(currentAction.andReturn().getResponse().getContentAsString(), TestQueryData.class);

        // Contains column names
        Assert.assertEquals("Should have specified number of detail items", detailCount, data.getTable().size() - 1);

        Assert.assertEquals("Should have all the content with specified where clause", this.managedDatabase().countTable(tableName),
                data.getTotalCount());

        List<Map<String, String>> allTable = managedDatabase().getAllContentForTable(tableName);

        // Same order for columns
        List<String> names = managedDatabase().getColumnNamesForTable(tableName).stream().sorted().collect(Collectors.toList());

        boolean first = true;
        int linePos = 0;
        for (List<String> line : data.getTable()) {

            List<String> sorted = line.stream().sorted().collect(Collectors.toList());

            // First : check headers
            if (first) {
                for (int i = 0; i < sorted.size(); i++) {
                    Assert.assertTrue(names.contains(sorted.get(i)));
                }
                first = false;
            }

            // Other : check content
            else {
                Map<String, String> res = allTable.get(linePos);

                for (int i = 0; i < sorted.size(); i++) {
                    Assert.assertEquals(res.get(names.get(i)), sorted.get(i));
                }
                linePos++;
            }
        }
    }
}
