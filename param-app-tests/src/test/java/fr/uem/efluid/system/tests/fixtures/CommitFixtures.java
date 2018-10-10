package fr.uem.efluid.system.tests.fixtures;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import java.util.Arrays;
import java.util.UUID;

import org.junit.Assert;
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

	private static UUID runningPrep;

	@Autowired
	private PilotableCommitPreparationService prep;

	@Before
	public void resetFixture() {
		resetAsyncProcess();
		runningPrep = null;
	}

	@Given("^a diff analysis can be started$")
	public void a_diff_analysis_can_be_started() throws Throwable {

		// Full dic init
		initCompleteDictionaryWith3Tables();

		// Authenticated
		implicitlyAuthenticatedAndOnPage("home page");

		// Valid version ready
		modelDatabase().initVersions(getCurrentUserProject(), Arrays.asList("vCurrent"), 0);

		// Mark async to "eternal" process
		mockEternalAsyncProcess();
	}

	@Given("^no diff is running$")
	public void no_diff_is_running() throws Throwable {
		this.prep.cancelCommitPreparation();
	}

	@Given("^a diff has already been launched$")
	public void a_diff_has_already_been_launched() throws Throwable {

		// Cancel anything running
		no_diff_is_running();

		// Start new
		get(getCorrespondingLinkForPageName("diff launch page"));

		// And get diff uuid for testing
		runningPrep = this.prep.getCurrentCommitPreparation().getIdentifier();
	}

	@Then("^a diff is running$")
	public void a_diff_is_running() throws Throwable {

		Assert.assertEquals(PilotedCommitStatus.DIFF_RUNNING, this.prep.getCurrentCommitPreparationStatus());
	}

	@Then("^an alert says that the diff is still running$")
	public void an_alert_says_that_the_diff_is_still_running() throws Throwable {

		// Alert => We are on running diff page
		currentAction = currentAction.andExpect(view().name(getCorrespondingTemplateForName("diff running")));

		// The running preparation is still the previous one
		Assert.assertEquals(runningPrep, this.prep.getCurrentCommitPreparation().getIdentifier());
	}
}
