package fr.uem.efluid.tools;

import fr.uem.efluid.ColumnType;
import fr.uem.efluid.model.entities.DictionaryEntry;
import fr.uem.efluid.model.entities.TableLink;
import fr.uem.efluid.services.types.Value;
import fr.uem.efluid.utils.DatasourceUtils;
import org.junit.Test;

import java.util.List;
import java.util.Locale;
import java.util.Map;

import static fr.uem.efluid.stubs.TestUtils.value;
import static org.assertj.core.api.Assertions.assertThat;

public class ManagedQueriesGeneratorTest {

    @Test
    public void testProducesGetOneQuerySimpleKeyValue() {
        ManagedQueriesGenerator generator = new ManagedQueriesGenerator(rules());

        String query = generator.producesGetOneQuery(entrySimpleKey("TABLE"), "1234");

        assertThat(query).isEqualTo("SELECT 1 FROM \"TABLE\" cur  WHERE \"KEY\"=1234 ORDER BY cur.\"KEY\"");
    }

    @Test
    public void testProducesGetOneQuerySimpleKeyNull() {
        ManagedQueriesGenerator generator = new ManagedQueriesGenerator(rules());

        String query = generator.producesGetOneQuery(entrySimpleKey("TABLE"), ManagedQueriesGenerator.NULL_KEY_VALUE);

        assertThat(query).isEqualTo("SELECT 1 FROM \"TABLE\" cur  WHERE \"KEY\" IS null ORDER BY cur.\"KEY\"");
    }

    @Test
    public void testProducesGetOneQueryCompositeKeyValues() {
        ManagedQueriesGenerator generator = new ManagedQueriesGenerator(rules());

        String query = generator.producesGetOneQuery(entryCompositeKey("TABLE"), "1234 / VALUE");

        assertThat(query).isEqualTo("SELECT 1 FROM \"TABLE\" cur  WHERE  ( \"KEY\"=1234 AND \"OTHER_KEY\"='VALUE' )  ORDER BY cur.\"KEY\"");
    }

    @Test
    public void testProducesGetOneQueryCompositeKeyWithNull() {
        ManagedQueriesGenerator generator = new ManagedQueriesGenerator(rules());

        String query = generator.producesGetOneQuery(entryCompositeKey("TABLE"), "1234 / " + ManagedQueriesGenerator.NULL_KEY_VALUE);

        assertThat(query).isEqualTo("SELECT 1 FROM \"TABLE\" cur  WHERE  ( \"KEY\"=1234 AND \"OTHER_KEY\" IS null )  ORDER BY cur.\"KEY\"");
    }

    @Test
    public void testProducesApplyRemoveOneKeyWithValue() {
        ManagedQueriesGenerator generator = new ManagedQueriesGenerator(rules());

        String query = generator.producesApplyRemoveQuery(entrySimpleKey("TABLE"), "1234");

        assertThat(query).isEqualTo("DELETE FROM \"TABLE\" WHERE \"KEY\"=1234 ");
    }

    @Test
    public void testProducesApplyRemoveOneKeyWithNull() {
        ManagedQueriesGenerator generator = new ManagedQueriesGenerator(rules());

        String query = generator.producesApplyRemoveQuery(entrySimpleKey("TABLE"), ManagedQueriesGenerator.NULL_KEY_VALUE);

        assertThat(query).isEqualTo("DELETE FROM \"TABLE\" WHERE \"KEY\" IS null ");
    }

    @Test
    public void testProducesApplyUpdateOneKeyWithValue() {
        ManagedQueriesGenerator generator = new ManagedQueriesGenerator(rules());

        String query = generator.producesApplyUpdateQuery(entrySimpleKey("TABLE"), "1234", List.of(
                value("VAL1", "FFDSFS"),
                value("VAL2", null),
                value("VAL3", 12)
        ), null, null, null);

        assertThat(query).isEqualTo("UPDATE \"TABLE\" SET \"VAL1\"='FFDSFS', \"VAL2\"=null, \"VAL3\"=12 WHERE \"KEY\"=1234");
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
