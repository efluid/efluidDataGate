package fr.uem.efluid.cucumber.hooks;

import fr.uem.efluid.cucumber.common.CucumberStepDefs;
import fr.uem.efluid.services.types.DiffContentSearch;
import io.cucumber.java.Before;
import org.junit.Ignore;

/**
 * Reset test context on each scenario. For details on "context", see {@link CucumberStepDefs} for details
 */
@Ignore // Means it will be ignored by junit start, but will be used by cucumber
public class TestContextResetHook extends CucumberStepDefs {

    @Before(order = 3)
    public void resetContext() {

        currentSearch = new DiffContentSearch();
        currentAction = null;
        currentException = null;
        currentStartPage = null;

        resetAuthentication();
        resetAsyncProcess();
        resetDatabaseIdentifier();
        disableLdap();
    }
}
