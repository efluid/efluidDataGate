package fr.uem.efluid.cucumber.hooks;

import fr.uem.efluid.services.ApplicationDetailsService;
import io.cucumber.java.Before;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Reset Wizard at startup
 */
public class WizardResetHook {

    @Autowired
    private ApplicationDetailsService applicationDetailsService;

    @Before
    public void resetWizard() {
        // If no data, wizard put to active
        this.applicationDetailsService.completeWizard();
    }
}
