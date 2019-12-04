package fr.uem.efluid.cucumber;

import fr.uem.efluid.cucumber.common.CucumberStepDefs;
import io.cucumber.junit.Cucumber;
import io.cucumber.junit.CucumberOptions;
import org.junit.runner.RunWith;

/**
 * @author elecomte
 * @version 2
 * @since v0.0.8
 */
@RunWith(Cucumber.class)
@CucumberOptions(
        features = "src/test/resources/gherkins",
        plugin = {"pretty", "html:target/cucumber-html-report", "json:target/cucumber.json"})
public class AllCucumberTest extends CucumberStepDefs {

    // Run tests from specified gherkins
}

