package fr.uem.efluid.stubs;

import static fr.uem.efluid.utils.DataGenerationUtils.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;

import org.junit.Assert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import fr.uem.efluid.ColumnType;
import fr.uem.efluid.IntegrationTestConfig;
import fr.uem.efluid.model.entities.Commit;
import fr.uem.efluid.model.entities.DictionaryEntry;
import fr.uem.efluid.model.entities.FunctionalDomain;
import fr.uem.efluid.model.entities.IndexAction;
import fr.uem.efluid.model.entities.IndexEntry;
import fr.uem.efluid.model.entities.Project;
import fr.uem.efluid.model.entities.User;
import fr.uem.efluid.model.repositories.CommitRepository;
import fr.uem.efluid.model.repositories.DictionaryRepository;
import fr.uem.efluid.model.repositories.FunctionalDomainRepository;
import fr.uem.efluid.model.repositories.IndexRepository;
import fr.uem.efluid.model.repositories.ProjectRepository;
import fr.uem.efluid.model.repositories.TableLinkRepository;
import fr.uem.efluid.model.repositories.UserRepository;
import fr.uem.efluid.security.UserHolder;
import fr.uem.efluid.tools.ManagedValueConverter;
import fr.uem.efluid.utils.DataGenerationUtils;

/**
 * @author elecomte
 * @since v0.0.1
 * @version 1
 */
@Component
@SuppressWarnings("boxing")
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
	private ProjectRepository projects;

	@Autowired
	private ManagedValueConverter converter;

	@Autowired
	private UserHolder userHolder;

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
	 * @param updateName
	 */
	public void setupDatabaseForUpdate(String updateName) {
		setupSourceDatabaseForUpdate(updateName);
		setupDictionaryForUpdate();
	}

	/**
	 * @param diffName
	 */
	public void setupSourceDatabaseForUpdate(String updateName) {
		this.sourceChilds.deleteAll();
		this.sources.deleteAll();

		this.sources.initFromDataset(readDataset(updateName + "/actual-parent.csv"), this.converter);
		this.sourceChilds.initFromDataset(readDataset(updateName + "/actual-child.csv"), this.converter);

		this.sources.flush();
		this.sourceChilds.flush();
	}

	/**
	 * @return
	 */
	public Project setupDictionaryForUpdate() {

		this.links.deleteAll();
		this.dictionary.deleteAll();
		this.domains.deleteAll();
		this.projects.deleteAll();

		Project proj = this.projects.save(project("Default"));
		setActiveProject(proj);
		FunctionalDomain dom1 = this.domains.save(domain("Source exemple", proj));
		this.dictionary
				.save(entry("Sources de données parent", dom1, "VALUE, PRESET, SOMETHING", TestUtils.SOURCE_TABLE_NAME, "1=1", "KEY",
						ColumnType.ATOMIC));
		DictionaryEntry child = this.dictionary
				.save(entry("Sources de données enfant", dom1, "VALUE, PARENT", TestUtils.SOURCE_CHILD_TABLE_NAME, "1=1", "KEY",
						ColumnType.ATOMIC));

		this.links.save(link(child, "PARENT", TestUtils.SOURCE_TABLE_NAME));

		this.projects.flush();
		this.domains.flush();
		this.dictionary.flush();
		this.links.flush();
		
		return proj;
	}

	/**
	 * @return
	 */
	public void dropAllDictionary() {

		this.links.deleteAll();
		this.dictionary.deleteAll();
		this.domains.deleteAll();
		this.projects.deleteAll();

		this.projects.flush();
		this.domains.flush();
		this.dictionary.flush();
		this.links.flush();

	}

	/**
	 * For testing
	 * 
	 * @param domain
	 * @param tableName
	 * @return
	 */
	public DictionaryEntry addDictionaryWithTrinome(String domain, String tableName, Project proj) {

		FunctionalDomain dom1 = this.domains.save(domain(domain, proj));
		DictionaryEntry dict = this.dictionary
				.save(entry(tableName, dom1, "VALUE, PRESET, SOMETHING", tableName, "1=1", "KEY", ColumnType.ATOMIC));
		this.links.save(link(dict, "PARENT", tableName + "_dest"));
		return dict;
	}

	/**
	 * @return
	 */
	public DataLoadResult setupDictionnaryForDiff() {
		this.dictionary.deleteAll();
		this.domains.deleteAll();
		this.projects.deleteAll();

		Project proj = this.projects.save(project("Default"));
		setActiveProject(proj);
		FunctionalDomain dom1 = this.domains.save(domain("Source exemple", proj));
		DictionaryEntry cmat = this.dictionary
				.save(entry("Sources de données", dom1, "VALUE, PRESET, SOMETHING", TestUtils.SOURCE_TABLE_NAME, "1=1", "KEY",
						ColumnType.ATOMIC));

		this.projects.flush();
		this.domains.flush();
		this.dictionary.flush();
		return new DataLoadResult(cmat.getUuid(), proj.getUuid());
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
		return DataGenerationUtils.update(key, action, rawPayload != null ? DataGenerationUtils.content(rawPayload, this.converter) : null,
				dict, null);
	}

	/**
	 * @param diffName
	 * @return
	 */
	public DataLoadResult setupIndexDatabaseForDiff(String diffName) {

		// Reset database
		this.index.deleteAll();
		this.commits.deleteAll();

		// Prepare data - core items
		DataLoadResult res = setupDictionnaryForDiff();

		User tester = this.users.getOne("testeur");
		// Prepare existing commits
		Commit com1 = this.commits.save(commit("Commit initial de création", tester, 15, new Project(res.getProjectUuid())));
		Commit com2 = this.commits.save(commit("Commit de mise à jour", tester, 7,new Project(res.getProjectUuid())));

		// Prepare index entries for batch init
		List<IndexEntry> indexesCom1 = readDataset(diffName + "/knew-add.csv")
				.entrySet().stream()
				.map(d -> DataGenerationUtils.update(d.getKey(), IndexAction.ADD, d.getValue(), new DictionaryEntry(res.getDicUuid()), com1))
				.collect(Collectors.toList());

		List<IndexEntry> indexesCom2 = readDataset(diffName + "/knew-remove.csv")
				.entrySet().stream()
				.map(d -> DataGenerationUtils.update(d.getKey(), IndexAction.REMOVE, d.getValue(), new DictionaryEntry(res.getDicUuid()), com2))
				.collect(Collectors.toList());
		indexesCom2.addAll(readDataset(diffName + "/knew-update.csv")
				.entrySet().stream()
				.map(d -> DataGenerationUtils.update(d.getKey(), IndexAction.UPDATE, d.getValue(), new DictionaryEntry(res.getDicUuid()), com2))
				.collect(Collectors.toList()));

		// Batch init of data
		this.index.saveAll(indexesCom1);
		this.index.saveAll(indexesCom2);

		com1.setIndex(indexesCom1);
		com2.setIndex(indexesCom2);

		// BiDirectionnal for index <> commit
		this.commits.save(com1);
		this.commits.save(com2);

		// // Force set updates
		this.commits.flush();
		this.index.flush();

		return res;
	}

	/**
	 * For database init in test
	 * 
	 * @param diffName
	 * @return
	 */
	public DataLoadResult setupDatabaseForDiff(String diffName) {

		LOGGER.debug("Start to restore database for diff test");

		setupSourceDatabaseForDiff(diffName);
		DataLoadResult res = setupIndexDatabaseForDiff(diffName);

		LOGGER.debug("Database setup with dataset {} for a new diff test", diffName);

		return res;
	}

	public void flushSources() {
		this.sources.flush();
		this.sourceChilds.flush();
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
	public void assertDictionarySize(long size) {
		Assert.assertEquals(size, this.dictionary.count());
	}

	/**
	 * @param size
	 */
	public void assertDomainsSize(long size) {
		Assert.assertEquals(size, this.domains.count());
	}

	/**
	 * @param size
	 */
	public void assertLinksSize(long size) {
		Assert.assertEquals(size, this.links.count());
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
	public void assertSourceContentAllValidate(Predicate<List<SimulatedSource>> predicate) {
		Assert.assertTrue(predicate.test(this.sources.findAll()));
	}

	/**
	 * @param predicate
	 */
	public void assertSourceChildContentAllValidate(Predicate<List<SimulatedSourceChild>> predicate) {
		Assert.assertTrue(predicate.test(this.sourceChilds.findAll()));
	}

	/**
	 * @param predicate
	 */
	public void assertSourceContentValidate(long id, Predicate<SimulatedSource> predicate) {
		Assert.assertTrue(predicate.test(this.sources.getOne(id)));
	}

	/**
	 * @param predicate
	 */
	public void assertSourceChildContentValidate(long id, Predicate<SimulatedSourceChild> predicate) {
		Assert.assertTrue(predicate.test(this.sourceChilds.getOne(id)));
	}

	/**
	 * @param predicate
	 */
	public void assertDictionaryContentAllValidate(Predicate<List<DictionaryEntry>> predicate) {
		Assert.assertTrue(predicate.test(this.dictionary.findAll()));
	}

	/**
	 * @param predicate
	 */
	public void assertDictionaryContentValidate(String uuid, Predicate<DictionaryEntry> predicate) {
		Assert.assertTrue(predicate.test(this.dictionary.getOne(UUID.fromString(uuid))));
	}

	@PostConstruct
	public void addTestUser() {
		this.userHolder.setWizzardUser(this.users.save(user("testeur")));
	}
	
	public void setActiveProject(Project pro){
		User user = this.userHolder.getCurrentUser();
		user.setSelectedProject(pro);
		this.users.save(user);
	}
}
