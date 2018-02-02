package fr.uem.efluid.stubs;

import static fr.uem.efluid.utils.DataGenerationUtils.*;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import fr.uem.efluid.IntegrationTestConfig;
import fr.uem.efluid.TestUtils;
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
import fr.uem.efluid.model.repositories.UserRepository;
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
	private FunctionalDomainRepository domains;

	@Autowired
	private DictionaryRepository dictionary;

	@Autowired
	private UserRepository users;

	@Autowired
	private IndexRepository index;

	@Autowired
	private CommitRepository commits;

	/**
	 * @param diffName
	 */
	public void setupSourceDatabase(String diffName) {
		this.sources.deleteAll();
		this.sources.initFromDataset(TestUtils.readDataset(diffName + "/actual.csv"));
		this.sources.flush();
	}

	/**
	 * @return
	 */
	public DictionaryEntry setupDictionnary() {
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
	 * @param diffName
	 * @return
	 */
	public DictionaryEntry setupIndexDatabase(String diffName) {

		// Reset database
		this.index.deleteAll();
		this.commits.deleteAll();
		this.users.deleteAll();

		// Prepare data - core items
		User dupont = this.users.save(user("dupont"));
		DictionaryEntry cmat = setupDictionnary();

		// Prepare existing commits
		Commit com1 = this.commits.save(commit("Commit initial de création", dupont, 15));
		Commit com2 = this.commits.save(commit("Commit de mise à jour", dupont, 7));

		// Prepare index entries for batch init
		List<IndexEntry> indexes = TestUtils.readDataset(diffName + "/knew-add.csv")
				.entrySet().stream()
				.map(d -> DataGenerationUtils.update(d.getKey(), IndexAction.ADD, d.getValue(), cmat, com1))
				.collect(Collectors.toList());
		indexes.addAll(TestUtils.readDataset(diffName + "/knew-remove.csv")
				.entrySet().stream()
				.map(d -> DataGenerationUtils.update(d.getKey(), IndexAction.REMOVE, d.getValue(), cmat, com2))
				.collect(Collectors.toList()));
		indexes.addAll(TestUtils.readDataset(diffName + "/knew-update.csv")
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

		setupSourceDatabase(diffName);
		DictionaryEntry cmat = setupIndexDatabase(diffName);

		LOGGER.debug("Database setup with dataset {} for a new diff test", diffName);

		return cmat.getUuid();
	}
}
