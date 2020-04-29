package fr.uem.efluid.cucumber.steps;

import fr.uem.efluid.cucumber.common.CucumberStepDefs;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.junit.Ignore;
import org.springframework.util.StringUtils;

import java.util.Objects;

/**
 * @author elecomte
 * @version 1
 * @since v0.0.8
 */
@Ignore // Means it will be ignored by junit start, but will be used by cucumber
public class WizzardStepDefs extends CucumberStepDefs {

    private static int currentStep = 0;

    @Given("^the application is fully initialized with the wizard$")
    public void the_application_is_fully_initialized_with_the_wizard() throws Throwable {
        initMinimalWizzardData();
    }

    @Given("^the user is on wizard welcome page$")
    public void the_user_is_on_wizard_welcome_page() throws Throwable {
        currentStep = 0;
    }

    @Given("^the user is on wizard user creation$")
    public void the_user_is_on_wizard_user_creation() throws Throwable {
        currentStep = 1;
    }

    @When("^the login \"(\\w*)\", the email \"(.*)\" and the password \"(\\w*)\" are specified$")
    public void the_login_the_email_and_the_password_are_specified(
            String login,
            String email,
            String password)
            throws Throwable {

        post("/wizard/" + currentStep,
                p("login", login),
                p("email", email),
                p("password", password));
    }

    @When("^the login \"(\\w*)\" and the password \"(\\w*)\" are specified$")
    public void the_login_and_the_password_are_specified(
            String login,
            String password)
            throws Throwable {

        post("/wizard/" + currentStep,
                p("login", login),
                p("password", password));
    }

    @Then("^the user creation page is (.*)an ldap authentication request$")
    public void user_create_is_login(String mode) {
        assertModelIsSpecifiedProperty("externalAuth", Boolean.class, v -> v == StringUtils.isEmpty(mode));
    }

}
