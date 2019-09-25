package fr.uem.efluid.tools;

import fr.uem.efluid.ColumnType;
import fr.uem.efluid.model.entities.*;
import fr.uem.efluid.services.types.VersionCompare.ChangeType;
import fr.uem.efluid.services.types.VersionCompare.ColumnChanges;
import fr.uem.efluid.services.types.VersionCompare.DictionaryTableChanges;
import fr.uem.efluid.services.types.VersionCompare.DomainChanges;
import fr.uem.efluid.utils.DataGenerationUtils;
import fr.uem.efluid.utils.DatasourceUtils.CustomQueryGenerationRules;
import fr.uem.efluid.utils.FormatUtils;
import org.junit.Test;

import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class VersionContentChangesGeneratorTest {

    private static final Project PROJ = DataGenerationUtils.project("Project");
    private static final VersionContentChangesGenerator GEN = generator();

    @Test
    public void testContentPrepareValid() {

        FunctionalDomain domain = DataGenerationUtils.domain("domain", PROJ);
        DictionaryEntry dict = DataGenerationUtils.entry("entry", domain, "cur.\"COLA\", cur.\"COLB\"", "T_TABLE", "1=1", "KEY", ColumnType.PK_ATOMIC);

        Version one = version("one", domain, dict);

        assertThat(one.getDomainsContent()).isEqualTo(
                "{\"uid\":\"" + domain.getUuid().toString()
                        + "\",\"cre\":\"" + FormatUtils.format(domain.getCreatedTime())
                        + "\",\"upd\":\"" + FormatUtils.format(domain.getUpdatedTime()) + "\",\"nam\":\"domain\","
                        + "\"pro\":\"" + PROJ.getUuid() + "\"}");

        assertThat(one.getDictionaryContent()).isEqualTo(
                "{\"uid\":\"" + dict.getUuid() + "\",\"kty\":\"PK_ATOMIC\","
                        + "\"dom\":\"" + domain.getUuid() + "\",\"tab\":\"T_TABLE\",\"whe\":\"1=1\","
                        + "\"cre\":\"" + FormatUtils.format(dict.getCreatedTime())
                        + "\",\"upd\":\"" + FormatUtils.format(dict.getUpdatedTime()) + "\",\"nam\":\"entry\"" +
                        ",\"sel\":\"cur.\\\"COLA\\\", cur.\\\"COLB\\\"\",\"kna\":\"KEY\"}");
    }

    @Test
    public void testContentPrepareConsistant() {


        FunctionalDomain domain = DataGenerationUtils.domain("domain", PROJ);
        DictionaryEntry dict = DataGenerationUtils.entry("entry", domain, "cur.\"COLA\", cur.\"COLB\"", "T_TABLE", "1=1", "KEY", ColumnType.PK_ATOMIC);

        Version one = version("one", domain, dict);
        Version two = version("two", domain, dict);

        assertThat(one.getDomainsContent()).isEqualTo(two.getDomainsContent());
        assertThat(one.getDictionaryContent()).isEqualTo(two.getDictionaryContent());
        assertThat(one.getLinksContent()).isEqualTo(two.getLinksContent());
        assertThat(one.getMappingsContent()).isEqualTo(two.getMappingsContent());
    }

    @Test
    public void testContentCompareNoDiff() {

        FunctionalDomain domain = DataGenerationUtils.domain("domain", PROJ);
        DictionaryEntry dict = DataGenerationUtils.entry("entry", domain, "cur.\"COLA\", cur.\"COLB\"", "T_TABLE", "1=1", "KEY", ColumnType.PK_ATOMIC);

        // Same content
        List<DomainChanges> changes = GEN.generateChanges(version("one", domain, dict), version("two", domain, dict));

        assertThat(changes).hasSize(1);

        DomainChanges dch = changes.iterator().next();

        assertThat(dch.getChangeType()).isEqualTo(ChangeType.UNCHANGED);

        assertThat(dch.getName()).isEqualTo("domain");
        assertThat(dch.getUnmodifiedTableCount()).isEqualTo(1);
        assertThat(dch.getTableChanges()).hasSize(1);

        DictionaryTableChanges tableChange = dch.getTableChanges().get(0);

        assertThat(tableChange.getName()).isEqualTo("entry");
        assertThat(tableChange.getChangeType()).isEqualTo(ChangeType.UNCHANGED);
        assertThat(tableChange.getColumnChanges()).hasSize(3);
        assertThat(tableChange.getFilter()).isEqualTo("1=1");
        assertThat(tableChange.getNameChange()).isEqualTo("entry");
        assertThat(tableChange.getTableName()).isEqualTo("T_TABLE");
        assertThat(tableChange.getTableNameChange()).isEqualTo("T_TABLE");

        assertThat(tableChange.getColumnChanges()).allMatch(c -> c.getChangeType() == ChangeType.UNCHANGED);
    }

    @Test
    public void testContentCompareColumnRemoved() {

        FunctionalDomain domain = DataGenerationUtils.domain("domain", PROJ);
        DictionaryEntry dict = DataGenerationUtils.entry("entry", domain, "cur.\"COLA\", cur.\"COLB\"", "T_TABLE", "1=1", "KEY", ColumnType.PK_ATOMIC);

        Version one = version("one", domain, dict);

        // Add change - remove 1 col
        dict.setSelectClause("cur.\"COLA\"");

        // Same content
        List<DomainChanges> changes = GEN.generateChanges(one, version("two", domain, dict));

        assertThat(changes).hasSize(1);

        DomainChanges dch = changes.iterator().next();

        assertThat(dch.getChangeType()).isEqualTo(ChangeType.MODIFIED);

        assertThat(dch.getName()).isEqualTo("domain");
        assertThat(dch.getUnmodifiedTableCount()).isEqualTo(0);
        assertThat(dch.getTableChanges()).hasSize(1);

        DictionaryTableChanges tableChange = dch.getTableChanges().get(0);

        assertThat(tableChange.getName()).isEqualTo("entry");
        assertThat(tableChange.getChangeType()).isEqualTo(ChangeType.MODIFIED);
        assertThat(tableChange.getColumnChanges()).hasSize(3);
        assertThat(tableChange.getFilter()).isEqualTo("1=1");
        assertThat(tableChange.getNameChange()).isEqualTo("entry");
        assertThat(tableChange.getTableName()).isEqualTo("T_TABLE");
        assertThat(tableChange.getTableNameChange()).isEqualTo("T_TABLE");

        ColumnChanges key = tableChange.getColumnChanges().stream().filter(c -> c.getName().equals("KEY")).findFirst().orElseThrow();
        ColumnChanges cola = tableChange.getColumnChanges().stream().filter(c -> c.getName().equals("COLA")).findFirst().orElseThrow();
        ColumnChanges colb = tableChange.getColumnChanges().stream().filter(c -> c.getName().equals("COLB")).findFirst().orElseThrow();

        assertThat(key.getChangeType()).isEqualTo(ChangeType.UNCHANGED);
        assertThat(cola.getChangeType()).isEqualTo(ChangeType.UNCHANGED);
        assertThat(colb.getChangeType()).isEqualTo(ChangeType.REMOVED);
        assertThat(colb.getName()).isEqualTo("COLB");
    }

    @Test
    public void testContentCompareColumnAdded() {

        FunctionalDomain domain = DataGenerationUtils.domain("domain", PROJ);
        DictionaryEntry dict = DataGenerationUtils.entry("entry", domain, "cur.\"COLA\", cur.\"COLB\"", "T_TABLE", "1=1", "KEY", ColumnType.PK_ATOMIC);

        Version one = version("one", domain, dict);

        // Add change - remove 1 col
        dict.setSelectClause("cur.\"COLA\", cur.\"COLB\", cur.\"COLC\"");

        // Same content
        List<DomainChanges> changes = GEN.generateChanges(one, version("two", domain, dict));

        assertThat(changes).hasSize(1);

        DomainChanges dch = changes.iterator().next();

        assertThat(dch.getChangeType()).isEqualTo(ChangeType.MODIFIED);

        assertThat(dch.getName()).isEqualTo("domain");
        assertThat(dch.getUnmodifiedTableCount()).isEqualTo(0);
        assertThat(dch.getTableChanges()).hasSize(1);

        DictionaryTableChanges tableChange = dch.getTableChanges().get(0);

        assertThat(tableChange.getName()).isEqualTo("entry");
        assertThat(tableChange.getChangeType()).isEqualTo(ChangeType.MODIFIED);
        assertThat(tableChange.getColumnChanges()).hasSize(4);
        assertThat(tableChange.getFilter()).isEqualTo("1=1");
        assertThat(tableChange.getNameChange()).isEqualTo("entry");
        assertThat(tableChange.getTableName()).isEqualTo("T_TABLE");
        assertThat(tableChange.getTableNameChange()).isEqualTo("T_TABLE");

        ColumnChanges key = tableChange.getColumnChanges().stream().filter(c -> c.getName().equals("KEY")).findFirst().orElseThrow();
        ColumnChanges cola = tableChange.getColumnChanges().stream().filter(c -> c.getName().equals("COLA")).findFirst().orElseThrow();
        ColumnChanges colb = tableChange.getColumnChanges().stream().filter(c -> c.getName().equals("COLB")).findFirst().orElseThrow();
        ColumnChanges colc = tableChange.getColumnChanges().stream().filter(c -> c.getName().equals("COLC")).findFirst().orElseThrow();

        assertThat(key.getChangeType()).isEqualTo(ChangeType.UNCHANGED);
        assertThat(cola.getChangeType()).isEqualTo(ChangeType.UNCHANGED);
        assertThat(colb.getChangeType()).isEqualTo(ChangeType.UNCHANGED);
        assertThat(colc.getChangeType()).isEqualTo(ChangeType.ADDED);
        assertThat(colc.getName()).isEqualTo("COLC");
    }

    private static VersionContentChangesGenerator generator() {
        CustomQueryGenerationRules rules = new CustomQueryGenerationRules();

        rules.setColumnNamesProtected(true);
        rules.setDatabaseDateFormat("dd-MM-yyyy HH:mm:ss");
        rules.setTableNamesProtected(true);

        return new VersionContentChangesGenerator(new ManagedQueriesGenerator(rules));
    }

    private static Version version(String name, FunctionalDomain domain, DictionaryEntry dictionaryEntry) {

        return version(
                name,
                Collections.singletonList(domain),
                Collections.singletonList(dictionaryEntry),
                Collections.emptyList(),
                Collections.emptyList());

    }

    private static Version version(
            String name,
            List<FunctionalDomain> domains,
            java.util.List<DictionaryEntry> dictionary,
            List<TableLink> links,
            List<TableMapping> mappings) {

        Version ver = DataGenerationUtils.version(name, PROJ);
        GEN.completeVersionContentForChangeGeneration(ver, domains, dictionary, links, mappings);
        return ver;
    }


}
