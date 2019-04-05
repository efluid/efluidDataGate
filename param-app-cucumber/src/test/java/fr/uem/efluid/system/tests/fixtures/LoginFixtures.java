package fr.uem.efluid.system.tests.fixtures;

import static org.hamcrest.Matchers.hasKey;
import static org.hamcrest.Matchers.nullValue;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.request;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import fr.uem.efluid.model.entities.User;
import fr.uem.efluid.system.common.SystemTest;

/**
 * @author elecomte
 * @since v0.0.8
 * @version 1
 */
public class LoginFixtures extends SystemTest {

	private static String currentLogin;

	@When("^the user specify .* credentials from \"(.*)\"$")
	public void the_user_specify_credential_from_login(String user) throws Throwable {
		currentLogin = cleanUserParameter(user);

		getCurrentUserLogin();

		User data = user(currentLogin);

		post(getCorrespondingLinkForPageName("login callback"),
				p("client_name", "web"),
				p("username", data.getLogin()),
				p("password", data.getPassword()));
	}

	@Then("^the authentication is successful$")
	public void the_authentication_is_successful() throws Throwable {

		currentAction = currentAction
				.andExpect(request().sessionAttribute("pac4jUserProfiles", hasKey("web")))
				.andExpect(status().is3xxRedirection());
	}

	@Then("^the authentication is failed$")
	public void the_authentication_is_failed() throws Throwable {

		currentAction = currentAction
				.andExpect(request().sessionAttribute("pac4jUserProfiles", nullValue()))
				.andExpect(status().is3xxRedirection());
	}
}
