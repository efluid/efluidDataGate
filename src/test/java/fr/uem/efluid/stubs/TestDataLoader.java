package fr.uem.efluid.stubs;

import static fr.uem.efluid.utils.DataGenerationUtils.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.junit.Assert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import fr.uem.efluid.IntegrationTestConfig;
import fr.uem.efluid.model.entities.Commit;
import fr.uem.efluid.model.entities.DictionaryEntry;
import fr.uem.efluid.model.entities.FunctionalDomain;
import fr.uem.efluid.model.entities.IndexAction;
import fr.uem.efluid.model.entities.IndexEntry;
import fr.uem.efluid.model.entities.User;
import fr.uem.efluid.model.repositories.CommitRepository;
import fr.uem.efluid.model.repositories.DictionaryRepository;
import fr.uem.efluid.model.repositories.FunctionalDomainRepository;
import fr.uem.efluid.model.repositories.IndexRepository;
import fr.uem.efluid.model.repositories.TableLinkRepository;
import fr.uem.efluid.model.repositories.UserRepository;
import fr.uem.efluid.tools.ManagedValueConverter;
import fr.uem.efluid.utils.DataGenerationUtils;

/**
 * @author elecomte
 * @since v0.0.1
 * @version 1
 */
@Component
public class TestDataLoader {

	private static final Logger LOGGER = LoggerFactory.getLogger(IntegrationTestConfig.class);

	@Autowired
	private SimulatedSourceRepository sources;

	@Autowired
	private SimulatedSourceChildRepository sourceChilds;

	@Autowired
	private FunctionalDomainRepository domains;

	@Autowired
	private DictionaryRepository dictionary;

	@Autowired
	private UserRepository users;

	@Autowired
	private IndexRepository index;

	@Autowired
	private CommitRepository commits;

	@Autowired
	private TableLinkRepository links;

	@Autowired
	private ManagedValueConverter converter;

	/**
	 * @param path
	 * @return
	 */
	public Map<String, String> readDataset(String path) {
		return TestUtils.readDataset(path, this.converter);
	}

	/**
	 * @param diffName
	 */
	public void setupSourceDatabaseForDiff(String diffName) {
		this.sources.deleteAll();
		this.sources.initFromDataset(readDataset(diffName + "/actual.csv"), this.converter);
		this.sources.flush();
	}

	/**
	 * @param diffName
	 */
	public void setupSourceDatabaseForUpdate(String updateName) {
		this.sources.deleteAll();
		this.sources.initFromDataset(readDataset(updateName + "/actual-parent.csv"), this.converter);
		this.sources.flush();

		this.sourceChilds.deleteAll();
		this.sourceChilds.initFromDataset(readDataset(updateName + "/actual-child.csv"), this.converter);
		this.sourceChilds.flush();
	}

	/**
	 * @return
	 */
	public void setupDictionnaryForUpdate() {

		this.links.deleteAll();
		this.dictionary.deleteAll();
		this.domains.deleteAll();

		FunctionalDomain dom1 = this.domains.save(domain("Source exemple"));
		this.dictionary
				.save(entry("Sources de données parent", dom1, "VALUE, PRESET, SOMETHING", TestUtils.SOURCE_TABLE_NAME, "1=1", "KEY"));
		DictionaryEntry child = this.dictionary
				.save(entry("Sources de données parent", dom1, "VALUE, PARENT", TestUtils.SOURCE_CHILD_TABLE_NAME, "1=1", "KEY"));

		this.links.save(link(child, "PARENT", TestUtils.SOURCE_TABLE_NAME));

		this.domains.flush();
		this.dictionary.flush();
		this.links.flush();
	}

	/**
	 * @return
	 */
	public DictionaryEntry setupDictionnaryForDiff() {
		this.dictionary.deleteAll();
		this.domains.deleteAll();

		FunctionalDomain dom1 = this.domains.save(domain("Source exemple"));
		DictionaryEntry cmat = this.dictionary
				.save(entry("Sources de données", dom1, "VALUE, PRESET, SOMETHING", TestUtils.SOURCE_TABLE_NAME, "1=1", "KEY"));

		this.domains.flush();
		this.dictionary.flush();
		return cmat;
	}

	/**
	 * Prepare one unsaved compliant IndexEntry
	 * 
	 * @param tableName
	 * @param key
	 * @param action
	 * @param rawPayload
	 * @return
	 */
	public IndexEntry initIndexEntry(String tableName, String key, IndexAction action, String rawPayload) {

		DictionaryEntry dict = this.dictionary.findByTableName(tableName);
		return DataGenerationUtils.update(key, action, DataGenerationUtils.content(rawPayload, this.converter), dict, null);
	}

	/**
	 * @param diffName
	 * @return
	 */
	public DictionaryEntry setupIndexDatabaseForDiff(String diffName) {

		// Reset database
		this.index.deleteAll();
		this.commits.deleteAll();
		this.users.deleteAll();

		// Prepare data - core items
		User dupont = this.users.save(user("dupont"));
		DictionaryEntry cmat = setupDictionnaryForDiff();

		// Prepare existing commits
		Commit com1 = this.commits.save(commit("Commit initial de création", dupont, 15));
		Commit com2 = this.commits.save(commit("Commit de mise à jour", dupont, 7));

		// Prepare index entries for batch init
		List<IndexEntry> indexes = readDataset(diffName + "/knew-add.csv")
				.entrySet().stream()
				.map(d -> DataGenerationUtils.update(d.getKey(), IndexAction.ADD, d.getValue(), cmat, com1))
				.collect(Collectors.toList());
		indexes.addAll(readDataset(diffName + "/knew-remove.csv")
				.entrySet().stream()
				.map(d -> DataGenerationUtils.update(d.getKey(), IndexAction.REMOVE, d.getValue(), cmat, com2))
				.collect(Collectors.toList()));
		indexes.addAll(readDataset(diffName + "/knew-update.csv")
				.entrySet().stream()
				.map(d -> DataGenerationUtils.update(d.getKey(), IndexAction.UPDATE, d.getValue(), cmat, com2))
				.collect(Collectors.toList()));

		// Batch init of data
		this.index.save(indexes);

		// // Force set updates
		this.users.flush();
		this.commits.flush();
		this.index.flush();

		return cmat;
	}

	/**
	 * For database init in test
	 * 
	 * @param diffName
	 * @return
	 */
	public UUID setupDatabaseForDiff(String diffName) {

		LOGGER.debug("Start to restore database for diff test");

		setupSourceDatabaseForDiff(diffName);
		DictionaryEntry cmat = setupIndexDatabaseForDiff(diffName);

		LOGGER.debug("Database setup with dataset {} for a new diff test", diffName);

		return cmat.getUuid();
	}

	/**
	 * @param datasToCompare
	 * @param dataset
	 */
	public void assertDatasetEqualsRegardingConverter(Map<String, String> datasToCompare, String dataset) {
		TestUtils.assertDatasetEquals(datasToCompare, dataset, this.converter);
	}

	/**
	 * @param size
	 */
	public void assertSourceSize(long size) {
		Assert.assertEquals(size, this.sources.count());
	}

	/**
	 * @param size
	 */
	public void assertSourceChildSize(long size) {
		Assert.assertEquals(size, this.sourceChilds.count());
	}

	/**
	 * @param predicate
	 */
	public void assertSourceContentValidate(Predicate<List<SimulatedSource>> predicate) {
		Assert.assertTrue(predicate.test(this.sources.findAll()));
	}

	/**
	 * @param predicate
	 */
	public void assertSourceChildContentValidate(Predicate<List<SimulatedSourceChild>> predicate) {
		Assert.assertTrue(predicate.test(this.sourceChilds.findAll()));
	}
}
