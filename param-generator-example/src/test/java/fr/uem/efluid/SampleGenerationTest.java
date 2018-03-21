package fr.uem.efluid;

import org.junit.Test;

import fr.uem.efluid.generation.DictionaryGenerator;
import fr.uem.efluid.generation.DictionaryGeneratorConfig;

/**
 * @author elecomte
 * @since v0.0.1
 * @version 1
 */
public class SampleGenerationTest {

	@Test
	public void testCallGeneration() throws Exception {

		DictionaryGenerator generator = new DictionaryGenerator(new DictionaryGeneratorConfig() {

			@Override
			public String getSourcePackage() {
				return "fr.uem.efluid";
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
				return true;
			}

			@Override
			public String getUploadEntryPointUri() {
				return "http://127.0.0.1:8080/rest/v1/dictionary";
			}

			@Override
			public String getUploadSecurityToken() {
				return "afc9921811684c7f88062cd47ddf0ff5";
			}
		});

		generator.generateDictionaryExport(getClass().getClassLoader());
	}
}
