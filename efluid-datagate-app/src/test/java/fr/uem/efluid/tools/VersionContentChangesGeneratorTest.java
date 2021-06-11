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
import org.junit.jupiter.api.Test;

import java.util.*;

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
    public void testContentDomainRead() {
        String domainCompres = "{\"uid\":\"6063f985-95eb-4b32-9682-9b7f29dd5370\",\"cre\":\"2019-09-19 22:47:49\"," +
                "\"upd\":\"2019-09-19 22:48:49\",\"nam\":\"domainWri\",\"pro\":\"ccc1dbf6-1851-4ee2-aaf5-f6bec3097398\"}";
        String dictCompres = "{\"uid\":\"cc9b2994-9082-4baa-8cbe-987127252026\",\"kty\":\"PK_ATOMIC\"," +
                "\"dom\":\"6063f985-95eb-4b32-9682-9b7f29dd5370\",\"tab\":\"T_TABLE_WRI\",\"whe\":\"1=1\"," +
                "\"cre\":\"2019-09-20 22:47:49\",\"upd\":\"2019-09-20 22:48:49\",\"nam\":\"entryWri\"," +
                "\"sel\":\"cur.\\\"COLA\\\", cur.\\\"COLB\\\"\",\"kna\":\"KEY\"}";

        Version testVersion = new Version();
        testVersion.setDomainsContent(domainCompres);
        testVersion.setDictionaryContent(dictCompres);

        List<FunctionalDomain> domains = new ArrayList<>();
        List<DictionaryEntry> dictionary = new ArrayList<>();
        List<TableLink> links = new ArrayList<>();
        List<TableMapping> mappings = new ArrayList<>();
        GEN.readVersionContent(testVersion, domains, dictionary, links, mappings);

        assertThat(domains).hasSize(1);
        assertThat(domains.get(0).getName()).isEqualTo("domainWri");
        assertThat(domains.get(0).getUuid()).isEqualTo(UUID.fromString("6063f985-95eb-4b32-9682-9b7f29dd5370"));


        assertThat(dictionary).hasSize(1);
        assertThat(dictionary.get(0).getTableName()).isEqualTo("T_TABLE_WRI");
        assertThat(dictionary.get(0).getParameterName()).isEqualTo("entryWri");
        assertThat(dictionary.get(0).getSelectClause()).isEqualTo("cur.\"COLA\", cur.\"COLB\"");
        assertThat(dictionary.get(0).getUuid()).isEqualTo(UUID.fromString("cc9b2994-9082-4baa-8cbe-987127252026"));
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

    @Test
    public void testContentCompareTableAdded() {

        FunctionalDomain domain = DataGenerationUtils.domain("domain", PROJ);
        DictionaryEntry dict = DataGenerationUtils.entry("entry", domain, "cur.\"COLA\", cur.\"COLB\"", "T_TABLE", "1=1", "KEY", ColumnType.PK_ATOMIC);
        DictionaryEntry dict2 = DataGenerationUtils.entry("entry2", domain, "cur.\"COLA\", cur.\"COLB\"", "T_TABLE2", "1=1", "KEY", ColumnType.PK_ATOMIC);

        // Content with new table
        List<DomainChanges> changes = GEN.generateChanges(
                version("one", domain, dict),
                version("two", domain, dict, dict2)
        );

        assertThat(changes).hasSize(1);

        DomainChanges dch = changes.iterator().next();

        assertThat(dch.getChangeType()).isEqualTo(ChangeType.MODIFIED);

        assertThat(dch.getName()).isEqualTo("domain");
        assertThat(dch.getUnmodifiedTableCount()).isEqualTo(1); // One table not changed
        assertThat(dch.getTableChanges()).hasSize(2);

        DictionaryTableChanges entryChange = dch.getTableChanges().stream().filter(c -> c.getName().equals("entry")).findFirst().orElseThrow();
        DictionaryTableChanges entry2Change = dch.getTableChanges().stream().filter(c -> c.getName().equals("entry2")).findFirst().orElseThrow();

        assertThat(entryChange.getName()).isEqualTo("entry");
        assertThat(entryChange.getChangeType()).isEqualTo(ChangeType.UNCHANGED);

        assertThat(entry2Change.getName()).isEqualTo("entry2");
        assertThat(entry2Change.getChangeType()).isEqualTo(ChangeType.ADDED);
        assertThat(entry2Change.getColumnChanges()).hasSize(3);
        assertThat(entry2Change.getFilter()).isEqualTo("1=1");
        assertThat(entry2Change.getTableName()).isEqualTo("T_TABLE2");

        assertThat(entry2Change.getColumnChanges()).allMatch(c -> c.getChangeType() == ChangeType.ADDED);
    }

    @Test
    public void testContentCompareTableRemoved() {

        FunctionalDomain domain = DataGenerationUtils.domain("domain", PROJ);
        DictionaryEntry dict = DataGenerationUtils.entry("entry", domain, "cur.\"COLA\", cur.\"COLB\"", "T_TABLE", "1=1", "KEY", ColumnType.PK_ATOMIC);
        DictionaryEntry dict2 = DataGenerationUtils.entry("entry2", domain, "cur.\"COLA\", cur.\"COLB\"", "T_TABLE2", "1=1", "KEY", ColumnType.PK_ATOMIC);

        // Content with new table
        List<DomainChanges> changes = GEN.generateChanges(
                version("one", domain, dict, dict2),
                version("two", domain, dict)
        );

        assertThat(changes).hasSize(1);

        DomainChanges dch = changes.iterator().next();

        assertThat(dch.getChangeType()).isEqualTo(ChangeType.MODIFIED);

        assertThat(dch.getName()).isEqualTo("domain");
        assertThat(dch.getUnmodifiedTableCount()).isEqualTo(1); // One table not changed
        assertThat(dch.getTableChanges()).hasSize(2);

        DictionaryTableChanges entryChange = dch.getTableChanges().stream().filter(c -> c.getName().equals("entry")).findFirst().orElseThrow();
        DictionaryTableChanges entry2Change = dch.getTableChanges().stream().filter(c -> c.getName().equals("entry2")).findFirst().orElseThrow();

        assertThat(entryChange.getName()).isEqualTo("entry");
        assertThat(entryChange.getChangeType()).isEqualTo(ChangeType.UNCHANGED);

        assertThat(entry2Change.getName()).isEqualTo("entry2");
        assertThat(entry2Change.getChangeType()).isEqualTo(ChangeType.REMOVED);
        assertThat(entry2Change.getColumnChanges()).isNull();
        assertThat(entry2Change.getFilter()).isNull();
        assertThat(entry2Change.getTableName()).isNull();
    }

    @Test
    public void testContentCompareDomainAdded() {

        FunctionalDomain domain = DataGenerationUtils.domain("domain", PROJ);
        FunctionalDomain domain2 = DataGenerationUtils.domain("domain2", PROJ);
        DictionaryEntry dict = DataGenerationUtils.entry("entry", domain, "cur.\"COLA\", cur.\"COLB\"", "T_TABLE", "1=1", "KEY", ColumnType.PK_ATOMIC);
        DictionaryEntry dict2 = DataGenerationUtils.entry("entry2", domain2, "cur.\"COLA\", cur.\"COLB\"", "T_TABLE2", "1=1", "KEY", ColumnType.PK_ATOMIC);
        DictionaryEntry dict3 = DataGenerationUtils.entry("entry3", domain2, "cur.\"COLA\", cur.\"COLB\"", "T_TABLE3", "1=1", "KEY", ColumnType.PK_ATOMIC);

        // Content with new table
        List<DomainChanges> changes = GEN.generateChanges(
                version("one", domain, dict),
                version("two", domain, domain2, dict, dict2, dict3)
        );

        assertThat(changes).hasSize(2);

        DomainChanges domChange = changes.stream().filter(c -> c.getName().equals("domain")).findFirst().orElseThrow();
        DomainChanges dom2Change = changes.stream().filter(c -> c.getName().equals("domain2")).findFirst().orElseThrow();

        assertThat(domChange.getChangeType()).isEqualTo(ChangeType.UNCHANGED);
        assertThat(domChange.getName()).isEqualTo("domain");
        assertThat(domChange.getUnmodifiedTableCount()).isEqualTo(1); // One table not changed
        assertThat(domChange.getTableChanges()).hasSize(1);

        assertThat(dom2Change.getChangeType()).isEqualTo(ChangeType.ADDED);
        assertThat(dom2Change.getName()).isEqualTo("domain2");
        assertThat(dom2Change.getUnmodifiedTableCount()).isEqualTo(0); // All new
        assertThat(dom2Change.getTableChanges()).hasSize(2); // Added table

        assertThat(dom2Change.getTableChanges()).allMatch(c -> c.getChangeType() == ChangeType.ADDED);
    }


    private static VersionContentChangesGenerator generator() {
        CustomQueryGenerationRules rules = new CustomQueryGenerationRules();

        rules.setColumnNamesProtected(true);
        rules.setDatabaseDateFormat("dd-MM-yyyy HH:mm:ss");
        rules.setTableNamesProtected(true);
        rules.setJoinOnNullableKeys(false);

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

    private static Version version(String name, FunctionalDomain domain, DictionaryEntry... dictionaryEntry) {

        return version(
                name,
                Collections.singletonList(domain),
                Arrays.asList(dictionaryEntry),
                Collections.emptyList(),
                Collections.emptyList());

    }

    private static Version version(String name, FunctionalDomain domain, FunctionalDomain domain2, DictionaryEntry... dictionaryEntry) {

        return version(
                name,
                Arrays.asList(domain, domain2),
                Arrays.asList(dictionaryEntry),
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
