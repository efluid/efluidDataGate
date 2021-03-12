package fr.uem.efluid.cucumber.steps;

import fr.uem.efluid.ColumnType;
import fr.uem.efluid.cucumber.common.CucumberStepDefs;
import fr.uem.efluid.model.entities.*;
import fr.uem.efluid.model.metas.ManagedModelDescription;
import fr.uem.efluid.services.types.ExportFile;
import fr.uem.efluid.services.types.ExportImportResult;
import fr.uem.efluid.services.types.VersionData;
import fr.uem.efluid.utils.DataGenerationUtils;
import io.cucumber.datatable.DataTable;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

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

    @Given("^this dictionary .*is modified to current default domain :$")
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

    @When("^the current full dictionary is exported$")
    public void full_dict_export() {

        // We edit the export
        ExportImportResult<ExportFile> export = this.dictService.exportAll();
        currentExports.put("dict-full", export);
    }

    @When("^the project \"(.*)\" is exported$")
    public void project_dict_export(String name) {

        var project = modelDatabase().getAllProjects().stream().filter(p -> p.getName().equals(name)).findFirst();

        assertThat(project).isPresent();

        this.projectMgmt.selectProject(project.get().getUuid());

        // We edit the export
        ExportImportResult<ExportFile> export = this.dictService.exportCurrentProject();
        currentExports.put("dict-proj-" + name, export);
    }

    @When("^the user import the available dictionary package as this$")
    public void when_import_dict_package() {

        ExportImportResult<ExportFile> currentExport = getSingleCurrentExport();

        this.dictService.importAll(currentExport.getResult());
    }

    @When("^the user import the available dictionary package in current project")
    public void when_import_dict_package_current_project() {

        ExportImportResult<ExportFile> currentExport = getSingleCurrentExport();

        this.dictService.importAllInCurrentProject(currentExport.getResult());
    }

    @Then("^a dictionary archive is produced$")
    public void full_dict_available() {
        assertThat(currentExports.get("dict-full")).describedAs("Full dictionary export").isNotNull();
    }

    @Then("^a dictionary archive is produced for project \"(.*)\"$")
    public void proj_dict_available(String name) {
        assertThat(currentExports.get("dict-proj-" + name))
                .describedAs("Dictionary export for project " + name).isNotNull();
    }

    @Then("^the active version \"(.*)\" is displayed$")
    public void then_active_version(String version) {
        assertModelIsSpecifiedProperty(
                "version",
                VersionData.class,
                v -> v.getName().equals(version));
    }

    @Then("^the active schema is displayed$")
    public void then_active_schema() {
        assertModelIsSpecifiedProperty(
                "modelDesc",
                ManagedModelDescription.class,
                d -> d.getSchema().equals("TWEAKED"));
    }
}
