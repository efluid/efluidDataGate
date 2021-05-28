package fr.uem.efluid.cucumber.steps;

import fr.uem.efluid.cucumber.common.CucumberStepDefs;
import fr.uem.efluid.model.entities.User;
import fr.uem.efluid.model.repositories.UserRepository;
import fr.uem.efluid.security.UserHolder;
import fr.uem.efluid.services.types.UserDetails;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.junit.Assert;
import org.junit.Ignore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.util.StringUtils;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author elecomte
 * @version 1
 * @since v0.0.8
 */
@Ignore // Means it will be ignored by junit start, but will be used by cucumber
public class UserStepDefs extends CucumberStepDefs {

    @Autowired
    private UserRepository users;

    @Given("^the user is authenticated$")
    public void the_user_is_authenticated() {

        // Implicit authentication and on page
        implicitlyAuthenticatedAndOnPage("home page");
    }

    @Given("^the user is not authenticated$")
    public void the_user_is_not_authenticated() {
        resetAuthentication();
    }

    @Then("^the current user email is (.*)$")
    public void the_current_user_email_is(String email) {

        Assert.assertEquals(email, getCurrentUserEmail());
    }

    @Then("^the current user is (.*)$")
    public void the_current_user_is_user(String user) {
        Assert.assertEquals(user, getCurrentUserLogin());
    }

    @Then("^the (.*) user is stored$")
    public void the_user_is_stored(String user) {

        // No init error
        assertModelHasNoSpecifiedProperty("error");

        String login = cleanUserParameter(user);
        Optional<User> real = this.users.findByLogin(login);
        assertThat(real).isPresent();
    }

    @Then("^the (.*) user is not stored$")
    public void the_user_is_not_stored(String user) {
        String login = cleanUserParameter(user);
        Optional<User> real = this.users.findByLogin(login);
        assertThat(real).isNotPresent();
    }

    @Then("^it is (.*)possible to create a new user$")
    public void a_user_can_be_created(String type) {
        assertModelIsSpecifiedProperty("canCreate", Boolean.class, v -> v == !StringUtils.hasText(type));
    }

    @Then("^the user \"(.*)\" exists in database$")
    public void user_in_dbt(String user) {
        assertThat(this.users.findByLogin(user)).isPresent();
    }

    @Then("^the list of users doesn't include \"(.*)\"$")
    public void not_in_user_list(String user) {
        assertThat(getCurrentSpecifiedPropertyList("users", UserDetails.class))
                .describedAs("List of users")
                .noneMatch(u -> u.getLogin().equals(user));
    }

    @When("^the technical user with token \"(.*)\" connect to an authenticated rest service$")
    public void connect_rest_tech(String token) throws Exception {


        String url = getCorrespondingLinkForPageName("the feature listing rest service");


        MockHttpServletRequestBuilder builder = MockMvcRequestBuilders.get(url);

        if (currentStartPage != null) {
            builder.header("Referer", currentStartPage);
        }
        builder.param("token", token);


        builder.accept(MediaType.APPLICATION_JSON);

        currentAction = this.mockMvc.perform(builder);

        assertRequestWasOk();
    }
}
