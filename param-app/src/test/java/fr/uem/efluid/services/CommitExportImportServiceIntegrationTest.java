package fr.uem.efluid.services;

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
import fr.uem.efluid.model.repositories.CommitRepository;
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
public class CommitExportImportServiceIntegrationTest {

	private static final String VALID_FILE = "valid-commits.par";

	@Autowired
	private CommitService commitService;

	@Autowired
	private TestDataLoader loader;

	@Autowired
	private CommitRepository commits;

	public void setupDatabase(String diff) {
		this.loader.setupDatabaseForDiff(diff);
	}

	@Test
	public void testExportFullCommits() {
		setupDatabase("diff7");

		TestUtils.writeExportFile(this.commitService.exportCommits(this.commits.findAll().get(0).getUuid()).getResult(), VALID_FILE);
	}

}
