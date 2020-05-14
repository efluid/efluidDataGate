package fr.uem.efluid.cucumber.hooks;

import fr.uem.efluid.cucumber.common.CucumberStepDefs;
import fr.uem.efluid.model.repositories.impls.PreSpecifiedFeatureManager;
import io.cucumber.java.After;
import org.junit.Ignore;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Forced cleanup without transaction management
 */
@Ignore // Means it will be ignored by junit start, but will be used by cucumber
public class DatabaseResetHook extends CucumberStepDefs {

    @Autowired
    private PreSpecifiedFeatureManager featureManager;

    @After(order = 50)
    public void dropAllWithoutContextManagement() {
        backlogDatabase().dropBacklog();
        managedDatabase().dropManaged();
        modelDatabase().dropModel();
        resetQueryGenerator();
        this.featureManager.resetFromEnvironment();
    }
}
