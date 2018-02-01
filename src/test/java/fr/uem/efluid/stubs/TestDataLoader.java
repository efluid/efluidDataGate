package fr.uem.efluid.stubs;

import static fr.uem.efluid.model.entities.IndexAction.ADD;
import static fr.uem.efluid.model.entities.IndexAction.REMOVE;
import static fr.uem.efluid.model.entities.IndexAction.UPDATE;
import static fr.uem.efluid.utils.DataGenerationUtils.*;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import fr.uem.efluid.TestUtils;
import fr.uem.efluid.model.entities.Commit;
import fr.uem.efluid.model.entities.DictionaryEntry;
import fr.uem.efluid.model.entities.FunctionalDomain;
import fr.uem.efluid.model.entities.User;
import fr.uem.efluid.model.repositories.CommitRepository;
import fr.uem.efluid.model.repositories.DictionaryRepository;
import fr.uem.efluid.model.repositories.FunctionalDomainRepository;
import fr.uem.efluid.model.repositories.IndexRepository;
import fr.uem.efluid.model.repositories.UserRepository;

/**
 * @author elecomte
 * @since v0.0.1
 * @version 1
 */
@Component
public class TestDataLoader {

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
	 * For database init in test
	 * 
	 * @param multiply
	 */
	public UUID setupDatabaseForDiff(String diffName, int multiply) {

		// Prepare data - sources = parameter source
		this.sources.initFromDataset(TestUtils.multiplyDataset(TestUtils.readDataset(diffName + "/actual.csv"), multiply));

		// Prepare data - core items
		User dupont = this.users.save(user("dupont"));
		FunctionalDomain dom1 = this.domains.save(domain("Source exemple"));
		DictionaryEntry cmat = this.dictionary.save(entry("Sources de données", dom1, TestUtils.SOURCE_TABLE_NAME, "1=1", "key"));

		// Prepare existing commit 1
		Commit com1 = this.commits.save(commit("Commit initial de création", dupont, 15));
		TestUtils.multiplyDataset(TestUtils.readDataset(diffName + "/knew-add.csv"), multiply).entrySet()
				.forEach(d -> this.index.save(update(d.getKey(), ADD, d.getValue(), cmat, com1)));

		this.index.flush();
		// Prepare existing commit 2
		Commit com2 = this.commits.save(commit("Commit de mise à jour", dupont, 7));
		TestUtils.multiplyDataset(TestUtils.readDataset(diffName + "/knew-remove.csv"), multiply).entrySet()
				.forEach(d -> this.index.save(update(d.getKey(), REMOVE, d.getValue(), cmat, com2)));
		this.index.flush();
		TestUtils.multiplyDataset(TestUtils.readDataset(diffName + "/knew-update.csv"), multiply).entrySet()
				.forEach(d -> this.index.save(update(d.getKey(), UPDATE, d.getValue(), cmat, com2)));

		// // Force set updates
		this.sources.flush();
		this.users.flush();
		this.domains.flush();
		this.dictionary.flush();
		this.commits.flush();
		this.index.flush();

		return cmat.getUuid();
	}
}
