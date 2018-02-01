package fr.uem.efluid.model;

import java.util.Map;
import java.util.UUID;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import fr.uem.efluid.IntegrationTestConfig;
import fr.uem.efluid.TestUtils;
import fr.uem.efluid.model.repositories.DictionaryRepository;
import fr.uem.efluid.model.repositories.ManagedParametersRepository;
import fr.uem.efluid.stubs.TestDataLoader;

/**
 * @author elecomte
 * @since v0.0.1
 * @version 1
 */
@RunWith(SpringJUnit4ClassRunner.class)
@DirtiesContext(classMode = ClassMode.AFTER_CLASS)
@SpringBootTest(classes = { IntegrationTestConfig.class })
public class ManagedParametersRepositoryIntegrationTest {

	@Autowired
	private ManagedParametersRepository managed;

	@Autowired
	private TestDataLoader loader;

	@Autowired
	private DictionaryRepository dictionary;

	private UUID dictionaryEntryUuid;

	@Before
	@Transactional
	public void setupDatabase() {

		this.dictionaryEntryUuid = this.loader.setupDatabaseForDiff("diff7", 1);
	}

	@Test
	public void testExtractCurrentContentLow() {

		Map<String, String> raw = this.managed.extractCurrentContent(this.dictionary.findOne(this.dictionaryEntryUuid));

		TestUtils.assertDatasetEquals(raw, "diff7/actual.csv");
	}

	@Test
	public void testRegenerateKnewContentLow() {

		Map<String, String> raw = this.managed.regenerateKnewContent(this.dictionary.findOne(this.dictionaryEntryUuid));

		TestUtils.assertDatasetEquals(raw, "diff7/knew.csv");
	}
}
