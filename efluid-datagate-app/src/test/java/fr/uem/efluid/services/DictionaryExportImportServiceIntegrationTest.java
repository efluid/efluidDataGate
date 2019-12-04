package fr.uem.efluid.services;

import java.io.File;
import java.io.IOException;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import fr.uem.efluid.IntegrationTestConfig;
import fr.uem.efluid.model.entities.Project;
import fr.uem.efluid.services.types.ExportFile;
import fr.uem.efluid.stubs.TestDataLoader;
import fr.uem.efluid.stubs.TestUtils;

/**
 * @author elecomte
 * @since v0.0.1
 * @version 1
 */
@Transactional(propagation = Propagation.REQUIRES_NEW)
@RunWith(SpringRunner.class)
@DirtiesContext(classMode = ClassMode.AFTER_CLASS)
@SpringBootTest(classes = { IntegrationTestConfig.class })
public class DictionaryExportImportServiceIntegrationTest {

	private static final String VALID_FILE = "valid-export.par";

	private static final String D = "import domain";
	private static final String T = "IMPORT_TABLE";

	private static final String TTESTSOURCE_UUID_IMPORT = "d0bfeac8-9cec-4962-839b-c7fed7025c25";
	private static final String IMPORT_TABLE_UUID_IMPORT = "29bc9e4e-f767-4e05-beef-b0266e800889";
	
	@Autowired
	private DictionaryManagementService dictService;

	@Autowired
	private ProjectManagementService proService;

	@Autowired
	private TestDataLoader loader;

	public Project setupDatabase() {
		Project pro = this.loader.setupDictionaryForUpdate();
		this.loader.addDictionaryWithTrinome(D, T, pro).getUuid();
		return pro;
	}

	@Test
	public void testExportFullDictionary() {
		setupDatabase();

		TestUtils.writeExportFile(this.dictService.exportAll().getResult(), VALID_FILE);
	}

	@Test
	public void testImportFullDictionary() throws IOException {

		// No initial DB
		this.loader.setupProject();

		ExportFile importFile = new ExportFile(new File("src/test/resources/" + VALID_FILE).toPath(), "");

		this.dictService.importAll(importFile);

		this.loader.assertDictionarySize(3);
		this.loader.assertDictionaryContentValidate(TTESTSOURCE_UUID_IMPORT, d -> d.getTableName().equals("TTESTSOURCE"));
		this.loader.assertDictionaryContentValidate(IMPORT_TABLE_UUID_IMPORT,
				d -> d.getTableName().equals(T) && d.getParameterName().equals(T));
		this.loader.assertDomainsSize(2);
		this.loader.assertLinksSize(2);
	}

	@Test
	public void testExportImportFullDictionary() throws IOException {
		
		Project pro = setupDatabase();

		this.proService.selectProject(pro.getUuid());

		TestUtils.writeExportFile(this.dictService.exportAll().getResult(), "2" + VALID_FILE);

		// Reset db after export, to reimport it
		this.loader.dropAllDictionary();
		this.loader.setupProject();

		ExportFile importFile = new ExportFile(new File("src/test/resources/" + VALID_FILE).toPath(), "");

		this.dictService.importAll(importFile);

		this.loader.assertDictionarySize(3);
		this.loader.assertDictionaryContentValidate(TTESTSOURCE_UUID_IMPORT, d -> d.getTableName().equals("TTESTSOURCE"));
		this.loader.assertDictionaryContentValidate(IMPORT_TABLE_UUID_IMPORT,
				d -> d.getTableName().equals(T) && d.getParameterName().equals(T));
		this.loader.assertDomainsSize(2);
		this.loader.assertLinksSize(2);
	}
}
