package fr.uem.efluid.services;

import java.io.File;
import java.io.IOException;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import fr.uem.efluid.IntegrationTestConfig;
import fr.uem.efluid.services.types.ExportFile;
import fr.uem.efluid.stubs.TestDataLoader;
import fr.uem.efluid.stubs.TestUtils;

/**
 * @author elecomte
 * @since v0.0.1
 * @version 1
 */
@RunWith(SpringJUnit4ClassRunner.class)
@DirtiesContext(classMode = ClassMode.AFTER_CLASS)
@SpringBootTest(classes = { IntegrationTestConfig.class })
public class DictionaryExportImportServiceIntegrationTest {

	private static final String VALID_FILE = "valid-export.par";

	private static final String D = "import domain";
	private static final String T = "IMPORT_TABLE";

	@Autowired
	private DictionaryManagementService dictService;

	@Autowired
	private TestDataLoader loader;

	@Transactional
	public void setupDatabase() {
		this.loader.setupDictionaryForUpdate();
		this.loader.addDictionaryWithTrinome(D, T).getUuid();
	}

	@Test
	public void testExportFullDictionary() {
		setupDatabase();

		TestUtils.writeExportFile(this.dictService.exportAll().getResult(), VALID_FILE);
	}

	@Test
	public void testImportFullDictionary() throws IOException {

		// No initial DB

		ExportFile importFile = new ExportFile(new File("src/test/resources/" + VALID_FILE).toPath(), "");

		this.dictService.importAll(importFile);

		this.loader.assertDictionarySize(3);
		this.loader.assertDictionaryContentValidate("effd3aaa-ed84-4386-a68b-9e62374f462d", d -> d.getTableName().equals("TTESTSOURCE"));
		this.loader.assertDictionaryContentValidate("8402ea6b-da3c-445e-9b20-3ccb15e2eb22",
				d -> d.getTableName().equals(T) && d.getParameterName().equals(T));
		this.loader.assertDomainsSize(2);
		this.loader.assertLinksSize(2);
	}

	@Test
	public void testExportImportFullDictionary() throws IOException {
		setupDatabase();

		TestUtils.writeExportFile(this.dictService.exportAll().getResult(), "2" + VALID_FILE);

		// Reset db after export, to reimport it
		this.loader.dropAllDictionary();

		ExportFile importFile = new ExportFile(new File("src/test/resources/" + VALID_FILE).toPath(), "");

		this.dictService.importAll(importFile);

		this.loader.assertDictionarySize(3);
		this.loader.assertDictionaryContentValidate("effd3aaa-ed84-4386-a68b-9e62374f462d", d -> d.getTableName().equals("TTESTSOURCE"));
		this.loader.assertDictionaryContentValidate("8402ea6b-da3c-445e-9b20-3ccb15e2eb22",
				d -> d.getTableName().equals(T) && d.getParameterName().equals(T));
		this.loader.assertDomainsSize(2);
		this.loader.assertLinksSize(2);
	}
}
