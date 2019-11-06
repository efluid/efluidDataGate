package fr.uem.efluid.cucumber.hooks;

import fr.uem.efluid.services.PilotableCommitPreparationService;
import io.cucumber.java.After;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Drop all async process running after test
 */
public class ProcessAbortHook  {

    @Autowired
    private PilotableCommitPreparationService commitPreparationService;

    @After(order = 100)
    public void cancelAllPreparations(){
        this.commitPreparationService.killAllCommitPreparations();
    }
}
