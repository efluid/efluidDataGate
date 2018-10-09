package fr.uem.efluid.system.tests.fixtures;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasProperty;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;

import org.junit.Before;
import org.springframework.beans.factory.annotation.Autowired;

import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import fr.uem.efluid.services.PilotableCommitPreparationService;
import fr.uem.efluid.services.types.PilotedCommitStatus;
import fr.uem.efluid.system.common.SystemTest;

/**
 * @author elecomte
 * @since v0.0.8
 * @version 1
 */
public class CommitFixtures extends SystemTest {

	@Autowired
	private PilotableCommitPreparationService prep;

	@Before
	public void resetFixture() {
		resetAsyncProcess();
	}

	@Given("^no diff is running$")
	public void no_diff_is_running() throws Throwable {
		this.prep.cancelCommitPreparation();
	}

	@Given("^a diff has already been launched$")
	public void a_diff_has_already_been_launched() throws Throwable {

		// Cancel anything running
		no_diff_is_running();

		// Mark async to "eternal" process
		mockEternalAsyncProcess();

		// And start new
		get(getCorrespondingLinkForPageName("diff launch page"));
	}

	@Then("^a diff is running$")
	public void a_diff_is_running() throws Throwable {

		// Check preparation status in model
		currentAction = currentAction.andExpect(model().attribute("preparation",
				hasProperty("status", equalTo(PilotedCommitStatus.DIFF_RUNNING))));
	}
}
