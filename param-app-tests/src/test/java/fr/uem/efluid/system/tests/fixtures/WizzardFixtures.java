package fr.uem.efluid.system.tests.fixtures;

import cucumber.api.java.en.Given;
import cucumber.api.java.en.When;
import fr.uem.efluid.system.common.SystemTest;

/**
 * @author elecomte
 * @since v0.0.8
 * @version 1
 */
public class WizzardFixtures extends SystemTest {

	private static int currentStep = 0;

	@Given("^the application is fully initialized with the wizzard$")
	public void the_application_is_fully_initialized_with_the_wizzard() throws Throwable {
		initMinimalWizzardData();
	}

	@Given("^the user is on wizzard welcome page$")
	public void the_user_is_on_wizzard_welcome_page() throws Throwable {
		currentStep = 0;
	}

	@Given("^the user is on wizzard user creation$")
	public void the_user_is_on_wizzard_user_creation() throws Throwable {
		currentStep = 1;
	}

	@When("the login \"(.*)\", the email \"(.*)\" and the password \"(.*)\" are specified")
	public void the_login_the_email_and_the_password_are_specified(
			String login,
			String email,
			String password)
			throws Throwable {

		post("/wizzard/" + currentStep,
				p("login", login),
				p("email", email),
				p("password", password));
	}

}
