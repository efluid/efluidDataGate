package fr.uem.efluid.cucumber.steps;

import fr.uem.efluid.ColumnType;
import fr.uem.efluid.cucumber.common.CucumberStepDefs;
import fr.uem.efluid.model.entities.*;
import fr.uem.efluid.utils.DataGenerationUtils;
import io.cucumber.datatable.DataTable;
import io.cucumber.java.en.Given;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

//@Ignore // Means it will be ignored by junit start, but will be used by cucumber
public class DictionaryStepDefs extends CucumberStepDefs {

    @Given("^a dictionary table is added for table \"(.*)\"$")
    public void given_init_dict_table(String name) {

        List<DictionaryEntry> tables = new ArrayList<>();
        List<TableLink> links = new ArrayList<>();

        initDefaultTables(getDefaultDomainFromCurrentProject(), tables, links, name);

        Version currentVersion = modelDatabase().findLastVersionForProject(getCurrentUserProject());

        modelDatabase().initDictionary(tables, links, currentVersion);
    }

    @Given("^this dictionary is added to current default domain :$")
    public void given_init_dict(DataTable dataTable) {
        List<Map<String, String>> content = dataTable.asMaps();

        FunctionalDomain domain = getDefaultDomainFromCurrentProject();

        List<DictionaryEntry> tables = content.stream().map(m ->
                DataGenerationUtils.entry(
                        m.get("entry name"),
                        domain,
                        m.get("select clause"),
                        m.get("table name"),
                        m.get("filter clause"),
                        m.get("key name"),
                        ColumnType.valueOf(m.get("key type"))
                )).collect(Collectors.toList());

        Version currentVersion = modelDatabase().findLastVersionForProject(getCurrentUserProject());

        modelDatabase().initDictionary(tables, new ArrayList<>(), currentVersion);
    }

    @Given("^this dictionary is modified to current default domain :$")
    public void given_modified_dict(DataTable dataTable) {
        List<Map<String, String>> content = dataTable.asMaps(String.class, String.class);
        Project project = getCurrentUserProject();

        FunctionalDomain domain = getDefaultDomainFromCurrentProject();

        List<DictionaryEntry> tables = content.stream().map(m ->
                DataGenerationUtils.entry(
                        m.get("entry name"),
                        domain,
                        m.get("select clause"),
                        m.get("table name"),
                        m.get("filter clause"),
                        m.get("key name"),
                        ColumnType.valueOf(m.get("key type"))
                )).map(d -> {
            try {
                DictionaryEntry existing = modelDatabase().findDictionaryEntryByProjectAndTableName(project, d.getTableName());
                existing.setParameterName(d.getParameterName());
                existing.setSelectClause(d.getSelectClause());
                existing.setWhereClause(d.getWhereClause());
                existing.setKeyName(d.getKeyName());
                existing.setKeyType(d.getKeyType());
                return existing;
            } catch (Throwable e) {
                // Do not exist - will be added
                return d;
            }
        }).collect(Collectors.toList());

        Version currentVersion = modelDatabase().findLastVersionForProject(project);

        modelDatabase().initDictionary(tables, new ArrayList<>(), currentVersion);
    }
}
