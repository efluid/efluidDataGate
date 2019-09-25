package fr.uem.efluid.system.tests.fixtures;

import cucumber.api.java.en.Given;
import fr.uem.efluid.model.entities.DictionaryEntry;
import fr.uem.efluid.model.entities.FunctionalDomain;
import fr.uem.efluid.model.entities.TableLink;
import fr.uem.efluid.model.entities.Version;
import fr.uem.efluid.system.common.SystemTest;

import java.util.ArrayList;
import java.util.List;

public class DictionaryFixture extends SystemTest {

    @Given("^a dictionary table is added for table \"(.*)\"$")
    public void given_init_dict_table(String name){

        List<DictionaryEntry> tables = new ArrayList<>();
        List<TableLink > links = new ArrayList<>();

        initDefaultTables( getDefaultDomainFromCurrentProject(), tables,links, name);

        Version currentVersion = modelDatabase().findLastVersionForProject(getCurrentUserProject());

        modelDatabase().initDictionary(tables, links, currentVersion);
    }
}
