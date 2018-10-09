package fr.uem.efluid.system.tests.fixtures;

import org.junit.Assert;
import org.springframework.beans.factory.annotation.Autowired;

import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import fr.uem.efluid.model.entities.User;
import fr.uem.efluid.model.repositories.UserRepository;
import fr.uem.efluid.system.common.SystemTest;

/**
 * @author elecomte
 * @since v0.0.8
 * @version 1
 */
public class UserFixtures extends SystemTest {

	@Autowired
	private UserRepository users;

	@Given("^the user is authenticated$")
	public void the_user_is_authenticated() throws Throwable {

		// Implicit authentication and on page
		implicitlyAuthenticatedAndOnPage("home page");
	}

	@Given("^the user is not authenticated$")
	public void the_user_is_not_authenticated() throws Throwable {
		resetAuthentication();
	}

	@Then("^the current user is (.*)$")
	public void the_current_user_is_user(String user) throws Throwable {
		Assert.assertEquals(user, getCurrentUserLogin());
	}

	@Then("^the (.*) user is stored$")
	public void the_user_is_stored(String user) throws Throwable {
		String login = cleanUserParameter(user);
		User def = user(login);
		User real = this.users.findByLogin(login);
		Assert.assertTrue(real != null);
		Assert.assertEquals(def.getEmail(), real.getEmail());
	}
}
