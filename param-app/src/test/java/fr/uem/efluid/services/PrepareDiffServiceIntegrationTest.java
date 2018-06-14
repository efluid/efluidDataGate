package fr.uem.efluid.services;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.junit.Assert;
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
import fr.uem.efluid.model.entities.IndexAction;
import fr.uem.efluid.model.repositories.DictionaryRepository;
import fr.uem.efluid.services.types.PreparedIndexEntry;
import fr.uem.efluid.stubs.TestDataLoader;

/**
 * @author elecomte
 * @since v0.0.1
 * @version 1
 */
@Transactional(propagation = Propagation.REQUIRES_NEW)
@RunWith(SpringRunner.class)
@DirtiesContext(classMode = ClassMode.AFTER_CLASS)
@SpringBootTest(classes = { IntegrationTestConfig.class })
public class PrepareDiffServiceIntegrationTest {

	@Autowired
	private PrepareIndexService service;

	@Autowired
	private TestDataLoader loader;

	@Autowired
	private DictionaryRepository dictionary;

	private UUID dictionaryEntryUuid;

	public void setupDatabase(String diff) {
		this.dictionaryEntryUuid = this.loader.setupDatabaseForDiff(diff);
	}

	@Test
	public void testProcessDiffNoIndex() {

		setupDatabase("diff7");
		Collection<PreparedIndexEntry> index = this.service.currentContentDiff(this.dictionary.getOne(this.dictionaryEntryUuid),
				new HashMap<>());
		Assert.assertEquals(0, index.size());
	}

	@Test
	public void testProcessDiffLargeIndex() {

		setupDatabase("diff8");
		Collection<PreparedIndexEntry> index = this.service.currentContentDiff(this.dictionary.getOne(this.dictionaryEntryUuid),
				new HashMap<>());
		Assert.assertEquals(80 + 100 + 85, index.size());
		List<PreparedIndexEntry> adds = index.stream()
				.filter(i -> i.getAction() == IndexAction.ADD)
				.collect(Collectors.toList());
		List<PreparedIndexEntry> removes = index.stream()
				.filter(i -> i.getAction() == IndexAction.REMOVE)
				.collect(Collectors.toList());
		List<PreparedIndexEntry> updates = index.stream()
				.filter(i -> i.getAction() == IndexAction.UPDATE)
				.collect(Collectors.toList());
		Assert.assertEquals(85, adds.size());
		Assert.assertEquals(100, removes.size());
		Assert.assertEquals(80, updates.size());
	}
}
