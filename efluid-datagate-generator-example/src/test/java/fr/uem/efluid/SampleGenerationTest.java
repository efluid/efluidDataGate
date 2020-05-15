package fr.uem.efluid;

import org.junit.Test;

import static fr.uem.efluid.GeneratorTester.onPackage;

/**
 * @author elecomte
 * @version 1
 * @since v0.0.1
 */
public class SampleGenerationTest {


    @Test
    public void testCallGenerationOnAll() {

        onPackage("fr.uem.efluid").generate().assertThatContentWereIdentified();
    }

    @Test
    public void testCallGenerationOnRemarks() {

        GeneratorTester tester = onPackage("fr.uem.efluid.sample.remarks").generate();

        tester.assertThatContentWereIdentified();
        tester.assertFoundDomainsAre("Remarques Efluid", "Entities Efluid");
        tester.assertFoundTablesAre(
                "T_ETAPE_WFL",
                "T_ETAPE_WFL_INHER",
                "T_ETAPE_WFL_SUB",
                "T_ETAPE_WFL_GEN",
                "T_CHILD_TABLE_TYPE",
                "T_CHILD_TABLE_TYPE_CUSTO",
                "T_CHILD_TABLE_TYPE_DROP",
                "T_BASIC_ENTITY",
                "T_OTHER_ENTITY"
        );
        tester.assertFoundLinkCountIs(0);
        tester.assertFoundMappingCountIs(0);

        tester.assertThatTable("T_ETAPE_WFL")
                .hasDictionaryEntryName("EtapeWorkflow")
                .hasKey("KEY", ColumnType.ATOMIC)
                .hasColumns("VALUE", "TIME");

        tester.assertThatTable("T_ETAPE_WFL_INHER")
                .hasDictionaryEntryName("EtapeWorkflowIgnoreParam")
                .hasKey("KEY", ColumnType.ATOMIC)
                .hasColumns("VALUE", "TIME");

        tester.assertThatTable("T_ETAPE_WFL_SUB")
                .hasDictionaryEntryName("EtapeWorkflowObjetGeneriqueSubOne")
                .hasKey("KEY", ColumnType.ATOMIC)
                .hasColumns("EXTENDED", "VALUE");

        tester.assertThatTable("T_ETAPE_WFL_GEN")
                .hasDictionaryEntryName("EtapeWorkflowObjetGeneriqueSubTwo")
                .hasKey("KEY", ColumnType.ATOMIC)
                .hasColumns("EXTENDED");

        tester.assertThatTable("T_CHILD_TABLE_TYPE")
                .hasDictionaryEntryName("InheritingParentType")
                .hasKey("KEYFIELD", ColumnType.STRING)
                .hasColumns("SOMETHING", "ENABLED", "VALUE");

        tester.assertThatTable("T_CHILD_TABLE_TYPE_CUSTO")
                .hasDictionaryEntryName("InheritingParentTypeTestCusto")
                .hasKey("KEYFIELD", ColumnType.STRING)
                .hasColumns("VALUE");

        tester.assertThatTable("T_CHILD_TABLE_TYPE_DROP")
                .hasDictionaryEntryName("InheritingParentTypeTestDrop")
                .hasKey("KEYFIELD", ColumnType.STRING)
                .hasColumns("VALUE", "OTHER");

        tester.assertThatTable("T_BASIC_ENTITY")
                .hasDictionaryEntryName("BasicEntity")
                .hasKey("ID", ColumnType.STRING)
                .hasColumns("VALUE", "OTHER", "SOMETHING");

        tester.assertThatTable("T_OTHER_ENTITY")
                .hasDictionaryEntryName("OtherEntity")
                .hasKey("ID", ColumnType.STRING)
                .hasColumns("VALUE", "OTHER");
    }

}
