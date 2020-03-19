package fr.uem.efluid.cucumber.steps;

import fr.uem.efluid.cucumber.common.CucumberStepDefs;
import fr.uem.efluid.model.repositories.FunctionalDomainRepository;
import fr.uem.efluid.services.DictionaryManagementService;
import fr.uem.efluid.services.types.ExportFile;
import fr.uem.efluid.services.types.FunctionalDomainData;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.junit.Before;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author elecomte
 * @version 1
 * @since v0.0.8
 */
//@Ignore // Means it will be ignored by junit start, but will be used by cucumber
public class DomainStepDefs extends CucumberStepDefs {

    private static List<String> specifiedDomainNames;

    private static UUID currentDomainUUID;

    @Autowired
    private DictionaryManagementService dictionaryManagementService;

    @Autowired
    private FunctionalDomainRepository domains;

    @Before
    public void resetFixture() {
        specifiedDomainNames = null;
    }

    @Given("^the existing functional domains (.*)$")
    public void the_following_functional_domains_exist(String domainNamesRaw) {

        List<String> domainNames = Stream.of(domainNamesRaw.split(", ")).collect(Collectors.toList());

        // Implicit init with domains
        initMinimalWizzardDataWithDomains(domainNames);

        // Implicit authentication and on page
        implicitlyAuthenticatedAndOnPage("functional domain edit page");

        // Keep domains
        specifiedDomainNames = domainNames;
    }

    @When("^the user add functional domain (.*)$")
    public void the_user_add_functional_domain(String name) throws Throwable {

        post("/ui/domains/add/" + name); // URLEncoder.encode(name, "UTF-8"));

        // List can be unmodifiable, reset it
        List<String> updatedDomainNames = new ArrayList<>();

        updatedDomainNames.addAll(specifiedDomainNames);
        updatedDomainNames.add(name);

        specifiedDomainNames = updatedDomainNames;

        // Update is REST only, update page
        get(getCorrespondingLinkForPageName("functional domain edit page"));
    }

    @When("^the user import a package \"(.*)\" containing a new domain name (.*)$")
    public void the_user_import_package_with_domain(String file, String name) throws IOException {

        File importFile = new File("src/test/resources/" + file);

        currentDomainUUID = modelDatabase().findDomainByProjectAndName(getCurrentUserProject(), name).getUuid();

        this.dictionaryManagementService.importAll(new ExportFile(importFile.toPath(), ""));
    }

    @When("^the user remove functional domain (.*)$")
    public void the_user_remove_functional_domain(String name) throws Throwable {

        UUID domainUuid = modelDatabase().findDomainByProjectAndName(getCurrentUserProject(), name).getUuid();

        post("/ui/domains/remove/" + domainUuid);

        // List can be unmodifiable, reset it
        List<String> updatedDomainNames = new ArrayList<>();

        updatedDomainNames.addAll(specifiedDomainNames);
        updatedDomainNames.remove(name);

        specifiedDomainNames = updatedDomainNames;

        // Update is REST only, update page
        get(getCorrespondingLinkForPageName("functional domain edit page"));
    }

    @Then("^the (\\d+) \\w+ functional domains are displayed$")
    public void the_x_functional_domains_are_displayed(int nbr) throws Throwable {

        assertModelIsSpecifiedListWithProperties(
                "domains",
                nbr,
                FunctionalDomainData::getName,
                specifiedDomainNames);
    }

    @Then("^only (\\d+) domains are still existing and the old (.*) functional domain is deleted$")
    public void deduplicated_domain(int remaining, String name) {

        assertThat(this.domains.findAll()).hasSize(remaining);
        assertThat(modelDatabase().findDomainByProjectAndName(getCurrentUserProject(), name).getUuid()).isNotEqualTo(currentDomainUUID);
    }
}
