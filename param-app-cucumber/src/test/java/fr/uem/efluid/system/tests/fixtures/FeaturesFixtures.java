package fr.uem.efluid.system.tests.fixtures;

import com.fasterxml.jackson.annotation.JsonFormat.Feature;

import cucumber.api.DataTable;
import cucumber.api.PendingException;
import cucumber.api.java.en.Then;
import fr.uem.efluid.system.common.SystemTest;

/**
 * @author elecomte
 * @since v0.0.8
 * @version 1
 */
public class FeaturesFixtures extends SystemTest {
	
	@Then("^there are the listed features:$")
	public void there_are_the_listed_features(DataTable data) throws Throwable {
		
		assertRequestWasOk();
		
		currentAction.andReturn().getResponse().getContentAsString();
		
		data.asMap(String.class, String.class);

	    throw new PendingException();
	}
}
