package fr.uem.efluid.system.tests;

import org.junit.runner.RunWith;

import cucumber.api.CucumberOptions;
import cucumber.api.junit.Cucumber;
import fr.uem.efluid.system.common.SystemTest;

/**
 * @author elecomte
 * @since v0.0.8
 * @version 1
 */
@RunWith(Cucumber.class)
@CucumberOptions(features = "src/test/resources/gherkins") //, tags = "@SINGLE")
public class AllCucumberTest extends SystemTest {

	// Run tests from specified gherkins
}

