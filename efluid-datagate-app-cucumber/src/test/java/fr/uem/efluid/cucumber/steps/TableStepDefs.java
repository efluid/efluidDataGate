package fr.uem.efluid.cucumber.steps;

import com.fasterxml.jackson.databind.ObjectMapper;
import fr.uem.efluid.cucumber.common.CucumberStepDefs;
import fr.uem.efluid.cucumber.stubs.ManagedDatabaseAccess;
import fr.uem.efluid.model.entities.DictionaryEntry;
import fr.uem.efluid.model.entities.Project;
import fr.uem.efluid.model.repositories.DictionaryRepository;
import fr.uem.efluid.services.types.DictionaryEntryEditData;
import fr.uem.efluid.services.types.DictionaryEntrySummary;
import fr.uem.efluid.services.types.ProjectData;
import fr.uem.efluid.services.types.TestQueryData;
import io.cucumber.datatable.DataTable;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.*;
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

    @Autowired
    private DictionaryRepository dicts;

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

    @Given("^the parameter table for managed tables \"(.*)\" already exists in project \"(.*)\"$")
    public void existing_parameter_table_in_project(String tableRaw, String projectName) throws Throwable {

        Optional<ProjectData> found = this.projectMgmt.getAllProjects().stream().filter(p -> p.getName().equals(projectName)).findFirst();

        assertThat(found).isPresent();

        Project project = new Project(found.get().getUuid());

        // Init one table in dict
        initDictionaryForDefaultVersionWithTables(modelDatabase().findDomainByProjectAndName(project, DEFAULT_DOMAIN), project, tableRaw.split(", "));
    }

    @Given("^the parameter table for managed table \"(.*)\" is specified with filter clause \"(.*)\"$")
    public void existing_parameter_table(String table, String filter) throws Throwable {
        initDictionaryForDefaultVersionWithTables(getDefaultDomainFromCurrentProject(), getCurrentUserProject(), table);
        modelDatabase().updateDictionaryFilterClause(table, filter);
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

        // Implicit init with tables and data
        a_managed_database_with_two_tables();

        // Prepared
        init_dictionary_table(name, table, null, select);
    }

    @Given("^a created parameter table with name \"(.*)\" for managed table \"([^\"]*)\" and columns selected as this :$")
    public void a_created_parameter_table_with_select_col(String name, String table, DataTable select) throws Throwable {

        // Implicit authentication and on page
        implicitlyAuthenticatedAndOnPage("parameter table main page");

        // Prepared
        init_dictionary_table(name, table, null, select);

        // And saved
        the_parameter_table_is_saved_by_user("saved");
    }

    @Given("^in project \"(.*)\", a created parameter table with name \"(.*)\" for managed table \"([^\"]*)\" and columns selected as this :$")
    public void a_created_parameter_table_with_select_col_in_project(String projectName, String name, String table, DataTable select) throws Throwable {

        Optional<ProjectData> project = this.projectMgmt.getAllProjects().stream().filter(p -> p.getName().equals(projectName)).findFirst();

        assertThat(project).as("Project with name " + projectName).isPresent();

        // Enable project
        this.projectMgmt.selectProject(project.get().getUuid());

        // Implicit authentication and on page
        implicitlyAuthenticatedAndOnPage("parameter table main page");

        // Prepared
        init_dictionary_table(name, table, null, select);

        // And saved
        the_parameter_table_is_saved_by_user("saved");
    }

    @Given("^a created parameter table with name \"(.*)\" for managed table \"(.*)\" with filter \"(.*)\" and columns selected as this :$")
    public void a_created_parameter_table_with_select_col_and_filter(String name, String table, String filter, DataTable select) throws Throwable {

        // Implicit authentication and on page
        implicitlyAuthenticatedAndOnPage("parameter table main page");

        // Prepared
        init_dictionary_table(name, table, filter, select);

        // And saved
        the_parameter_table_is_saved_by_user("saved");
    }

    @When("^the user asks to update the filter clause for current project with \"(.*)\"$")
    public void when_force_filter_clause(String clause) throws Exception {
        post(getCorrespondingLinkForPageName("force parameter table filter clause"), p("clause", clause));
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

    @Then("^the parameter table for managed table \"(.*)\" has the filter clause \"(.*)\"$")
    public void the_parameter_table_clause_is(String table, String clause) {
        DictionaryEntry dict = modelDatabase().findDictionaryEntryByProjectAndTableName(getCurrentUserProject(), table);
        assertThat(dict.getWhereClause()).isEqualTo(clause);
    }

    @Then("^the non compatible tables for filter clause are :$")
    public void non_compatible_table_for_filter(DataTable data) {

        var expected = data.asMaps().stream().map(t -> t.get("table")).collect(Collectors.toList());
        var displayed = getCurrentSpecifiedPropertyList("notCompatibleTables", DictionaryEntrySummary.class).stream().map(d -> d.getTableName()).collect(Collectors.toList());

        assertThat(displayed).describedAs("Displayed non compatible tables for filter clause").hasSameElementsAs(expected);
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

        List<String> organizedHeaders = null;
        int linePos = 0;
        for (List<String> line : data.getTable()) {

            // First : check headers
            if (organizedHeaders == null) {
                organizedHeaders = new ArrayList<>();
                for (int i = 0; i < line.size(); i++) {
                    String header = line.get(i);
                    Assert.assertTrue(names.contains(line.get(i)));
                    organizedHeaders.add(header);
                }
            }

            // Other : check content
            else {
                Map<String, String> res = allTable.get(linePos);

                for (int i = 0; i < line.size(); i++) {
                    String colName = names.get(i);
                    int resPosition = organizedHeaders.indexOf(colName);
                    Assert.assertEquals(res.get(colName), line.get(resPosition));
                }
                linePos++;
            }
        }
    }

    @Then("^these parameter tables are specified :$")
    public void table_exist(DataTable data) {

        var expected = data.asMaps();

        List<DictionaryEntry> existings = this.dicts.findAll();

        assertThat(existings).hasSize(expected.size());

        Map<String, List<DictionaryEntry>> byProjects = existings.stream().collect(Collectors.groupingBy(d -> d.getDomain().getProject().getName()));

        expected.forEach(t -> {

            List<DictionaryEntry> forProject = byProjects.get(t.get("project"));

            assertThat(forProject).describedAs("Tables for project " + t.get("project")).isNotNull();

            var found = forProject.stream().filter(d -> d.getTableName().equals(t.get("table name"))).findFirst();

            assertThat(found).describedAs("Existing table with name " + t.get("table name") + " for project " + t.get("project")).isPresent();
            assertThat(found.get().getSelectClause()).describedAs("Select clause for table with name " + t.get("table name")).isEqualTo(t.get("select clause"));

            if (t.get("key 1") != null) {
                assertThat(found.get().getKeyName()).describedAs("Key 1 for table with name " + t.get("table name")).isEqualTo(t.get("key 1"));
            }
            if (t.get("key 2") != null) {
                assertThat(found.get().getExt1KeyName()).describedAs("Key 2 for table with name " + t.get("table name")).isEqualTo(t.get("key 2"));
            }
            if (t.get("key 3") != null) {
                assertThat(found.get().getExt2KeyName()).describedAs("Key 3 for table with name " + t.get("table name")).isEqualTo(t.get("key 3"));
            }
        });

    }

    private void init_dictionary_table(String name, String table, String filter, DataTable select) throws Throwable {

        // Implicit select
        get(getCorrespondingLinkForPageName("new parameter table page") + "/" + table);

        // Get provided data to post them updated
        DictionaryEntryEditData data = (DictionaryEntryEditData) currentAction.andReturn()
                .getModelAndView().getModel().get("entry");

        if (filter != null) {
            data.setWhere(filter);
        }

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

}
