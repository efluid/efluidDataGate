package fr.uem.efluid.services;

import fr.uem.efluid.IntegrationTestConfig;
import fr.uem.efluid.model.entities.Export;
import fr.uem.efluid.model.repositories.CommitRepository;
import fr.uem.efluid.model.repositories.ExportRepository;
import fr.uem.efluid.services.types.CommitExportDisplay;
import fr.uem.efluid.services.types.CommitExportEditData;
import fr.uem.efluid.stubs.TestDataLoader;
import fr.uem.efluid.stubs.TestUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author elecomte
 * @since v0.0.1
 * @version 1
 */
@Transactional(propagation = Propagation.REQUIRES_NEW)
@ExtendWith(SpringExtension.class)
@DirtiesContext(classMode = ClassMode.BEFORE_CLASS)
@SpringBootTest(classes = { IntegrationTestConfig.class })
public class CommitExportImportServiceIntegrationTest {

	private static final String VALID_FILE = "valid-commits.par";

	@Autowired
	private CommitService commitService;

	@Autowired
	private TestDataLoader loader;

	@Autowired
	private CommitRepository commits;

	@Autowired
	private ExportRepository exports;

	public void setupDatabase(String diff) {
		this.loader.setupDatabaseForDiff(diff);
	}

	@Test
	public void testExportCanPrepareWithoutStarting() {
		setupDatabase("diff7");

		// We edit the export
		CommitExportEditData exportEdit = this.commitService.initCommitExport(CommitExportEditData.CommitSelectType.RANGE_FROM, null);

		// From the edited data, create an export ...
		CommitExportDisplay exportDisplay = this.commitService.saveCommitExport(exportEdit);

		// ... Stored in DB
		Optional<Export> export = this.exports.findById(exportDisplay.getUuid());

		assertThat(export).isPresent();
		assertThat(export.get().getStartCommit().getUuid()).isEqualTo(this.commits.findAll().get(0).getUuid());
		assertThat(export.get().getDownloadedTime()).isNull();
		assertThat(this.commitService.isCommitExportDownloaded(export.get().getUuid())).isFalse();

		// Then export
		this.commitService.processCommitExport(export.get().getUuid());

		// Status is updated
		assertThat(this.commitService.isCommitExportDownloaded(export.get().getUuid())).isTrue();
	}

	@Test
	public void testExportFullCommits() {
		setupDatabase("diff7");

		CommitExportEditData export = this.commitService.initCommitExport(CommitExportEditData.CommitSelectType.SINGLE_ONE, this.commits.findAll().get(0).getUuid());
		UUID exportUuid = this.commitService.saveCommitExport(export).getUuid();
		TestUtils.writeExportFile(this.commitService.processCommitExport(exportUuid).getResult(), VALID_FILE);
	}

}
