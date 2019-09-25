package fr.uem.efluid.tools;

import fr.uem.efluid.ColumnType;
import fr.uem.efluid.model.entities.DictionaryEntry;
import fr.uem.efluid.utils.DatasourceUtils;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class ManagedQueriesGeneratorTest {

    @Test
    public void testProducesGetOneQuerySimpleKey() {
        ManagedQueriesGenerator generator = new ManagedQueriesGenerator(rules());

        String query = generator.producesGetOneQuery(entrySimpleKey("TABLE"), "1234");

        assertThat(query).isEqualTo("SELECT 1 FROM \"TABLE\" cur  WHERE \"KEY\"=1234 ORDER BY cur.\"KEY\"");
    }

    @Test
    public void testProducesGetOneQueryCompositeKey() {
        ManagedQueriesGenerator generator = new ManagedQueriesGenerator(rules());

        String query = generator.producesGetOneQuery(entryCompositeKey("TABLE"), "1234 / VALUE");

        assertThat(query).isEqualTo("SELECT 1 FROM \"TABLE\" cur  WHERE  ( \"KEY\"=1234 AND \"OTHER_KEY\"='VALUE' )  ORDER BY cur.\"KEY\"");
    }

    // TODO : check all producer methods here !

    private static DictionaryEntry entrySimpleKey(String name) {
        DictionaryEntry entry = new DictionaryEntry();
        entry.setKeyName("KEY");
        entry.setKeyType(ColumnType.PK_ATOMIC);
        entry.setTableName(name);
        entry.setWhereClause("1=1");
        entry.setSelectClause("cur.\"COLA\", cur.\"COLB\"");
        return entry;
    }

    private static DictionaryEntry entryCompositeKey(String name) {
        DictionaryEntry entry = new DictionaryEntry();
        entry.setKeyName("KEY");
        entry.setKeyType(ColumnType.PK_ATOMIC);
        entry.setExt1KeyName("OTHER_KEY");
        entry.setExt1KeyType(ColumnType.PK_STRING);
        entry.setTableName(name);
        entry.setWhereClause("1=1");
        entry.setSelectClause("cur.\"COLA\", cur.\"COLB\"");
        return entry;
    }

    private static DatasourceUtils.CustomQueryGenerationRules rules() {
        DatasourceUtils.CustomQueryGenerationRules rules = new DatasourceUtils.CustomQueryGenerationRules();

        rules.setColumnNamesProtected(true);
        rules.setDatabaseDateFormat("dd-MM-yyyy HH:mm:ss");
        rules.setTableNamesProtected(true);

        return rules;
    }
}
