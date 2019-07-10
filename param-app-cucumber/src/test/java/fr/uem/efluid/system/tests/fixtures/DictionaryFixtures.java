package fr.uem.efluid.system.tests.fixtures;

import cucumber.api.java.en.Then;
import fr.uem.efluid.model.metas.ManagedModelDescription;
import fr.uem.efluid.services.types.VersionData;
import fr.uem.efluid.system.common.SystemTest;

public class DictionaryFixtures extends SystemTest {

    @Then("^the active version \"(.*)\" is displayed$")
    public void the_active_version_is_displayed(String name) {

        assertModelIsSpecifiedProperty("version", VersionData.class, d -> d.getName().equals(name));
    }

    @Then("^the active schema is displayed$")
    public void the_active_schema_is_displayed() {

        assertModelIsSpecifiedProperty("modelDesc", ManagedModelDescription.class, d -> d.getSchema().equals("TWEAKED"));
    }

}
