package fr.uem.efluid.cucumber.steps;

import fr.uem.efluid.cucumber.common.CucumberStepDefs;
import fr.uem.efluid.utils.FormatUtils;
import io.cucumber.datatable.DataTable;
import io.cucumber.docstring.DocString;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.util.FileCopyUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.OptionalDouble;
import java.util.OptionalLong;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * @author elecomte
 * @version 1
 * @since v0.0.8
 */
//@Ignore // Means it will be ignored by junit start, but will be used by cucumber
public class CommonStepDefs extends CucumberStepDefs {

    public static boolean efluidCase = false;

    static String specifiedVariation;


    @Given("^the test is a performance standard scenario for variation \"(.*)\"$")
    public void perf_test_case(String variation) {
        specifiedVariation = variation;
        efluidCase = true;
    }

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

        if (efluidCase) {
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
        this.prep.cancelCommitPreparation();
    }

    @Given("^the user accesses to the destination environment with only the versions until \"(.*)\"$")
    public void user_witch_environment_partial_dic(String version) {

        // Drop all indexes + Test tables datas
        backlogDatabase().dropBacklog();
        managedDatabase().dropManaged();
        this.prep.cancelCommitPreparation();

        // But reset versions after a specified one
        modelDatabase().dropVersionsAfter(getCurrentUserProject(), version);
    }

    @Given("^ldap auth is enabled with search at \"(.*)\", with login attr \"(.*)\" and email attr \"(.*)\" and this content :$")
    public void ldap_is_specified(String searchBase, String loginAttribute, String mailAttribute, DocString config) {
        enableLdap(config.getContent(), searchBase, loginAttribute, mailAttribute);
    }

    @Given("^the profiling is started$")
    public void profiling() {
        startupTime = System.currentTimeMillis();
        startProfiling();
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

    @When("^we check content in datagate database$")
    public void check_database() {
        // Nothing required
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

    @Then("^the page is displayed$")
    public void page_content_is_rendered() throws Exception {
        currentAction.andDo(MockMvcResultHandlers.print());
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

    @Given("^the (.*) generated data in managed table \"(.*)\" :$")
    public void existing_data_in_managed_table(int count, String name, DataTable data) {

        // Variation depends on large test size
        if (specifiedVariation != null) {
            specifiedVariation = specifiedVariation + " - " + count + " items";
        }

        managedDatabase().initHeavyTab(count, name, data);
    }

    @Given("^the data in managed table \"(.*)\" is now :$")
    public void check_data_in_managed_table(String name, DataTable data) {
        managedDatabase().assertCurrentTabComplies(name, data);
    }

    @Given("^the user do nothing more$")
    public void does_nothing() {
        // Nothing
    }

    @Given("^the current model id is \"(.*)\"$")
    public void mock_model_id(String id) {
        mockDatabaseIdentifierWithVersion(id, true);
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

        // Implicit wizard init
        initMinimalWizzardData();

        // Implicit authentication
        implicitlyAuthenticatedAndOnPage(page);
    }

    @SuppressWarnings("ThrowableNotThrown")
    @Then("^an error is provided with this message :$")
    public void error_message(DocString message) {
        assertErrorMessageContent(message.getContent().trim());
    }

    @SuppressWarnings("ThrowableNotThrown")
    @Then("^an error of type (.*) is provided with this message :$")
    public void error_message(String type, DocString message) {
        assertErrorMessageType(type);
        error_message(message);
    }

    @SuppressWarnings("ThrowableNotThrown")
    @Then("^an error of type (.*) is provided with this payload :$")
    public void error_payload(String type, DocString payload) {
        assertErrorMessageType(type);
        assertErrorMessagePayload(payload.getContent().trim());
    }

    @Then("^no error is provided$")
    public void no_error() {
        assertThat(currentException).isNull();

        if (currentAction != null) {
            assertThat(currentAction.andReturn().getResolvedException()).isNull();
        }
    }

    @Then("^the result \"(.*)\" is provided$")
    public void result_check(String result) {
        if ("SUCCESS".equals(result)) {
            assertThat(currentAction.andReturn().getResolvedException()).describedAs("A success is expected from the application").isNull();
        } else {
            assertErrorMessageContent(result);
        }
    }

    @Then("^the request is a success$")
    public void success_request() {
        assertRequestWasOk();
    }

    @Then("^an auth error is displayed$")
    public void login_auth_error() {
        assertModelHasSpecifiedProperty("error");
    }

    @Then("^the test process reference values are logged in \"(.*)\"$")
    public void perf_log(String filename) throws IOException {

        List<BasicProfiler.Stats> stats = stopProfilingAndGetStats();

        // Prepare values
        OptionalLong peakFree = stats.stream().mapToLong(BasicProfiler.Stats::getFree).max();
        OptionalLong peakTotal = stats.stream().mapToLong(BasicProfiler.Stats::getTotal).max();

        OptionalDouble avgFree = stats.stream().mapToLong(BasicProfiler.Stats::getFree).average();
        OptionalDouble avgTotal = stats.stream().mapToLong(BasicProfiler.Stats::getTotal).average();

        String time = FormatUtils.format(LocalDateTime.now());
        long duration = System.currentTimeMillis() - startupTime;

        Path dest = Paths.get(filename);

        if (!Files.exists(dest)) {
            Files.write(
                    dest,
                    "variation;time;total duration;peak free;peak total;avg free;avg total\n".getBytes(),
                    StandardOpenOption.CREATE);
        }

        String line = String.format("%s;%s;%d;%s;%s;%s;%s\n",
                specifiedVariation,
                time,
                duration,
                (peakFree.isPresent() ? Math.round(peakFree.getAsLong() / (1024 * 1024d)) + "Mb" : "n/a"),
                (peakTotal.isPresent() ? Math.round(peakTotal.getAsLong() / (1024 * 1024d)) + "Mb" : "n/a"),
                (avgFree.isPresent() ? Math.round(avgFree.getAsDouble() / (1024 * 1024d)) + "Mb" : "n/a"),
                (avgTotal.isPresent() ? Math.round(avgTotal.getAsDouble() / (1024 * 1024d)) + "Mb" : "n/a")
        );

        Files.write(
                Paths.get(filename),
                line.getBytes(),
                StandardOpenOption.CREATE,
                StandardOpenOption.APPEND);

        // Reset variation - no more in perf test
        specifiedVariation = null;
    }
}
