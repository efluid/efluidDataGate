package fr.uem.efluid;

import fr.uem.efluid.generation.DictionaryContent;
import fr.uem.efluid.generation.DictionaryExporter;
import fr.uem.efluid.generation.DictionaryGenerator;
import fr.uem.efluid.generation.DictionaryGeneratorConfig;
import fr.uem.efluid.model.ParameterTableDefinition;
import org.junit.Assert;
import org.junit.Test;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author elecomte
 * @version 1
 * @since v0.0.1
 */
public class SampleGenerationTest {


    @Test
    public void testCallGenerationOnAll() throws Exception {

        DictionaryGeneratorConfig config = config("fr.uem.efluid");

        DictionaryGenerator generator = new DictionaryGenerator(config);
        DictionaryExporter exporter = new DictionaryExporter(config);

        exporter.export(generator.extractDictionary(getClass().getClassLoader()));
    }

    @Test
    public void testCallGenerationOnRemarks() {

        DictionaryGeneratorConfig config = config("fr.uem.efluid.sample.remarks");

        DictionaryGenerator generator = new DictionaryGenerator(config);

        DictionaryContent content = generator.extractDictionary(getClass().getClassLoader());

        Assert.assertNotNull(content);

        Assert.assertEquals(2, content.getAllDomains().size());
        Assert.assertEquals(9, content.getAllTables().size());
        Assert.assertEquals(0, content.getAllLinks().size());
        Assert.assertEquals(0, content.getAllMappings().size());

        // 2 identified domains
        Assert.assertTrue(content.getAllDomains().stream().allMatch(
                d -> d.getName().equals("Remarques Efluid")
                        || d.getName().equals("Entities Efluid")
        ));

        assertTableDefValid(content.getAllTables(), "T_ETAPE_WFL", "EtapeWorkflow", "KEY", "VALUE", "TIME");
        assertTableDefValid(content.getAllTables(), "T_ETAPE_WFL_INHER", "EtapeWorkflowIgnoreParam", "KEY", "VALUE", "TIME");
        assertTableDefValid(content.getAllTables(), "T_ETAPE_WFL_SUB", "EtapeWorkflowObjetGeneriqueSubOne", "KEY", "EXTENDED", "VALUE");
        assertTableDefValid(content.getAllTables(), "T_ETAPE_WFL_GEN", "EtapeWorkflowObjetGeneriqueSubTwo", "KEY", "EXTENDED");
        assertTableDefValid(content.getAllTables(), "T_CHILD_TABLE_TYPE", "InheritingParentType", "KEYFIELD", "SOMETHING", "ENABLED", "VALUE");
        assertTableDefValid(content.getAllTables(), "T_CHILD_TABLE_TYPE_CUSTO", "InheritingParentTypeTestCusto", "KEYFIELD", "VALUE");
        assertTableDefValid(content.getAllTables(), "T_CHILD_TABLE_TYPE_DROP", "InheritingParentTypeTestDrop", "KEYFIELD", "VALUE", "OTHER");
        assertTableDefValid(content.getAllTables(), "T_BASIC_ENTITY", "BasicEntity", "ID", "VALUE", "OTHER", "SOMETHING");
        assertTableDefValid(content.getAllTables(), "T_OTHER_ENTITY", "OtherEntity", "ID", "VALUE", "OTHER");
    }

    private static void assertTableDefValid(Collection<ParameterTableDefinition> tables, String tableName, String name, String key, String... selectCols) {

        Optional<ParameterTableDefinition> tableOpt = tables.stream().filter(t -> t.getTableName().equals(tableName)).findFirst();
        Assert.assertTrue(tableOpt.isPresent());
        ParameterTableDefinition table = tableOpt.get();

        Assert.assertEquals("Specified name is not valid. Expected \"" + name + "\" but get \"" + table.getParameterName() + "\" for table \"" + tableName + "\"", name, table.getParameterName());
        Assert.assertEquals("Specified key is not valid. Expected \"" + key + "\" but get \"" + table.getKeyName() + "\" for table \"" + tableName + "\"", key, table.getKeyName());

        // Cannot validate order (depends on reflexion, which may depends itself on environment)
        // So validate on array
        String[] splitSelect = table.getSelectClause().split(", ");
        Assert.assertEquals("Specified select is not valid. Do not find the correct number of columns in current select \"" + table.getSelectClause()
                + "\" for table \"" + tableName + "\"", selectCols.length, splitSelect.length);
        List<String> cols = Stream.of(splitSelect).map(v -> v.replaceAll("cur\\.\"", "").replaceAll("\"", "")).collect(Collectors.toList());
        Stream.of(selectCols).forEach(v -> Assert.assertTrue("Specified select is not valid. Cannot found specified col \"" + v + "\" for table \"" + tableName + "\"", cols.contains(v)));
    }


    private static DictionaryGeneratorConfig config(final String packageName) {

        return new DictionaryGeneratorConfig() {

            @Override
            public String getSourcePackage() {
                return packageName;
            }

            @Override
            public LogFacade getLogger() {
                return new LogFacade() {

                    @Override
                    public void debug(CharSequence var1) {
                        System.out.println("[DEBUG] " + var1);
                    }

                    @Override
                    public void info(CharSequence var1) {
                        System.out.println("[INFO] " + var1);
                    }

                    @Override
                    public void error(CharSequence var1, Throwable var2) {
                        System.out.println("[ERROR] " + var1 + ". " + var2.getMessage() + ":");
                        var2.printStackTrace(System.out);
                    }

                };
            }

            @Override
            public String getDestinationFolder() {
                return "./target/";
            }

            @Override
            public boolean isProtectColumn() {
                return true;
            }

            @Override
            public String getDestinationFileDesignation() {
                return DictionaryGeneratorConfig.AUTO_GEN_DEST_FILE_DESG;
            }

            @Override
            public boolean isUploadToServer() {
                return false;
            }

            @Override
            public String getUploadEntryPointUri() {
                return "http://127.0.0.1:8080/rest/v1/dictionary";
            }

            @Override
            public String getUploadSecurityToken() {
                return "afc9921811684c7f88062cd47ddf0ff5";
            }

            @Override
            public String getProjectVersion() {
                return "1.2.3";
            }

            @Override
            public boolean isCheckDuplicateTables() {
                return false;
            }
        };
    }
}
