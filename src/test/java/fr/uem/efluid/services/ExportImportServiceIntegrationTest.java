package fr.uem.efluid.services;

import java.util.UUID;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import fr.uem.efluid.IntegrationTestConfig;
import fr.uem.efluid.model.repositories.DictionaryRepository;
import fr.uem.efluid.model.repositories.FunctionalDomainRepository;
import fr.uem.efluid.model.repositories.TableLinkRepository;
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
public class ExportImportServiceIntegrationTest {

	private static final String VALID_FILE = "valid-export.par";

	private static final String D = "import domain";
	private static final String T = "IMPORT_TABLE";

	@Autowired
	private DictionaryManagementService dictService;

	@Autowired
	private TestDataLoader loader;

	@Autowired
	private DictionaryRepository dict;

	@Autowired
	private FunctionalDomainRepository functs;

	@Autowired
	private TableLinkRepository links;

	private UUID dictionaryEntryUuid;

	@Transactional
	public void setupDatabase() {
		this.loader.setupDictionnaryForUpdate();
		this.dictionaryEntryUuid = this.loader.addDictionaryWithTrinome(D, T).getUuid();
	}

	@Test
	public void testExportFullDictionary() {
		setupDatabase();

		TestUtils.writeExportFile(this.dictService.exportAll(), VALID_FILE);
	}
}
