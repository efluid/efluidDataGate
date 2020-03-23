package fr.uem.efluid.cucumber.hooks;

import fr.uem.efluid.cucumber.common.CucumberStepDefs;
import io.cucumber.java.After;
import org.junit.Ignore;

/**
 * Forced cleanup without transaction management
 */
@Ignore // Means it will be ignored by junit start, but will be used by cucumber
public class DatabaseResetHook extends CucumberStepDefs {

    @After(order = 50)
    public void dropAllWithoutContextManagement() {
        backlogDatabase().dropBacklog();
        managedDatabase().dropManaged();
        modelDatabase().dropModel();
        resetQueryGenerator();
    }
}
