package fr.uem.efluid.model;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

import fr.uem.efluid.IntegrationTestConfig;
import fr.uem.efluid.model.repositories.DictionaryRepository;
import fr.uem.efluid.model.repositories.ManagedExtractRepository;
import fr.uem.efluid.model.repositories.ManagedRegenerateRepository;
import fr.uem.efluid.stubs.TestDataLoader;

/**
 * @author elecomte
 * @since v0.0.1
 * @version 1
 */
@Ignore
@RunWith(SpringRunner.class)
@DirtiesContext(classMode = ClassMode.AFTER_CLASS)
@SpringBootTest(classes = { IntegrationTestConfig.class })
public class ManagedParametersRepositoryIntegrationTest {

	@Autowired
	private ManagedExtractRepository extracted;

	@Autowired
	private ManagedRegenerateRepository regenerated;

	@Autowired
	private TestDataLoader loader;

	@Autowired
	private DictionaryRepository dictionary;

	private UUID dictionaryEntryUuid;

	@Transactional
	public void setupDatabase(String diff) {
		this.dictionaryEntryUuid = this.loader.setupDatabaseForDiff(diff);
	}

	@Test
	@Transactional
	public void testExtractCurrentContentLow() {
		setupDatabase("diff7");
		Map<String, String> raw = this.extracted.extractCurrentContent(this.dictionary.getOne(this.dictionaryEntryUuid), new HashMap<>());
		this.loader.assertDatasetEqualsRegardingConverter(raw, "diff7/actual.csv");
	}

	@Test
	@Transactional
	public void testRegenerateKnewContentLow() {
		setupDatabase("diff7");
		Map<String, String> raw = this.regenerated.regenerateKnewContent(this.dictionary.getOne(this.dictionaryEntryUuid));
		this.loader.assertDatasetEqualsRegardingConverter(raw, "diff7/knew.csv");
	}

	@Test
	@Transactional
	public void testExtractCurrentContentHeavy() {
		setupDatabase("diff8");
		Map<String, String> raw = this.extracted.extractCurrentContent(this.dictionary.getOne(this.dictionaryEntryUuid), new HashMap<>());
		this.loader.assertDatasetEqualsRegardingConverter(raw, "diff8/actual.csv");
	}

	@Test
	@Transactional
	public void testRegenerateKnewContentHeavy() {
		setupDatabase("diff8");
		Map<String, String> raw = this.regenerated.regenerateKnewContent(this.dictionary.getOne(this.dictionaryEntryUuid));
		this.loader.assertDatasetEqualsRegardingConverter(raw, "diff8/knew.csv");
	}
}
