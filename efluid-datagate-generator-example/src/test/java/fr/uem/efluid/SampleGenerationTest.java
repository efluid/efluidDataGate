package fr.uem.efluid;

import fr.uem.efluid.generation.DictionaryContent;
import fr.uem.efluid.generation.DictionaryExporter;
import fr.uem.efluid.generation.DictionaryGenerator;
import fr.uem.efluid.generation.DictionaryGeneratorConfig;
import org.junit.Assert;
import org.junit.Test;

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

        Assert.assertEquals(1, content.getAllDomains().size());
        Assert.assertEquals(1, content.getAllProjects().size());
        Assert.assertEquals(1, content.getAllVersions().size());
        Assert.assertEquals(4, content.getAllTables().size());
        Assert.assertEquals(0, content.getAllLinks().size());
        Assert.assertEquals(0, content.getAllMappings().size());

        Assert.assertEquals("Remarques Efluid", content.getAllDomains().iterator().next().getName());
        Assert.assertEquals("Generated Test", content.getAllProjects().iterator().next().getName());
        Assert.assertEquals(config.getProjectVersion(), content.getAllVersions().iterator().next().getName());
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
        };
    }
}
