package fr.uem.efluid.services;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import fr.uem.efluid.model.entities.Commit;
import fr.uem.efluid.model.repositories.CommitRepository;
import fr.uem.efluid.model.repositories.impls.InMemoryManagedRegenerateRepository;
import fr.uem.efluid.services.ExportImportService.ExportImportPackage;
import fr.uem.efluid.services.PilotableCommitPreparationService.PilotedCommitPreparation;
import fr.uem.efluid.services.types.CommitPackage;
import fr.uem.efluid.services.types.ExportImportFile;
import fr.uem.efluid.services.types.ExportImportResult;
import fr.uem.efluid.services.types.PreparedMergeIndexEntry;

/**
 * <p>
 * Features for commit preparation. Everything that can need a new diff preparation
 * <b>must</b> be used only from {@link PilotableCommitPreparationService} for init.
 * </p>
 * 
 * @author elecomte
 * @since v0.0.1
 * @version 1
 */
@Service
public class CommitService {

	private static final String PCKG_ALL = "commits-all";
	private static final String PCKG_AFTER = "commits-part";
	private static final String PCKG_CHERRY_PICK = "commits-cherry-pick";

	private static final Logger LOGGER = LoggerFactory.getLogger(InMemoryManagedRegenerateRepository.class);

	@Autowired
	private CommitRepository commits;

	@Autowired
	private ExportImportService exportImportService;

	/**
	 * @param startingWithCommit
	 */
	public ExportImportResult<ExportImportFile> exportCommits(UUID startingWithCommit) {

		/*
		 * Export contains all commits. But it is possible to load commit "ref" only for
		 * old commits, and to do a chery pick, with one selected commit
		 */

		LOGGER.debug("Asking for a commit export");

		String pckgName = startingWithCommit != null ? PCKG_ALL : PCKG_AFTER;
		List<Commit> commitsToExport = this.commits.findAll();

		// If starting uuid is specified, mark all "previous" as not exported as ref only
		if (startingWithCommit != null) {
			LOGGER.info("Partial commit export asked. Will use ref only for all commits BEFORE {}", startingWithCommit);

			Commit startCommit = this.commits.findOne(startingWithCommit);

			// Mark previous ones as "ref only"
			commitsToExport.stream().filter(c -> c.getCreatedTime().isBefore(startCommit.getCreatedTime())).forEach(Commit::setAsRefOnly);
		}

		// Then export :
		ExportImportFile file = this.exportImportService.exportPackages(
				Collections.singletonList(new CommitPackage(pckgName, LocalDateTime.now()).initWithContent(commitsToExport)));

		ExportImportResult<ExportImportFile> result = new ExportImportResult<>(file);

		// Update count is the "ref only" count
		long refOnly = commitsToExport.stream().filter(Commit::isRefOnly).count();
		result.addCount(pckgName, commitsToExport.size() - refOnly, refOnly, 0);

		LOGGER.info("Export package for commit is ready. {} total commits exported, uncluding {} exported as ref only. File size is {}b",
				Integer.valueOf(commitsToExport.size()), Long.valueOf(refOnly), Integer.valueOf(file.getSize()));

		// Result is for display / File load
		return result;
	}

	/**
	 * @param startingWithCommit
	 */
	public ExportImportResult<ExportImportFile> exportOneCommit(UUID commitUuid) {

		/*
		 * Export contains all commits. But it is possible to load commit "ref" only for
		 * old commits, and to do a chery pick, with one selected commit
		 */

		LOGGER.debug("Asking for a chery-pick commit export on commit {}", commitUuid);

		Commit exported = this.commits.findOne(commitUuid);

		// Then export :
		ExportImportFile file = this.exportImportService.exportPackages(
				Collections.singletonList(new CommitPackage(PCKG_CHERRY_PICK, LocalDateTime.now())
						.initWithContent(Collections.singletonList(exported))));

		LOGGER.info("Export package for commit {} is ready. Size is {}b", commitUuid, Integer.valueOf(file.getSize()));

		ExportImportResult<ExportImportFile> result = new ExportImportResult<>(file);

		// Single item
		result.addCount(PCKG_CHERRY_PICK, 1, 0, 0);

		// Result is for display / File load
		return result;
	}

	/**
	 * 
	 */
	void merge() {

		// Expo
	}

	/**
	 * <p>
	 * Reserved for launch from <tt>PilotableCommitPreparationService</tt>
	 * 
	 * @param importFile
	 */
	ExportImportResult<PilotedCommitPreparation<PreparedMergeIndexEntry>> importCommits(ExportImportFile importFile,
			PilotedCommitPreparation<PreparedMergeIndexEntry> currentPreparation) {

		LOGGER.debug("Asking for an import of commit in piloted preparation context {}", currentPreparation.getIdentifier());

		// #1 Load import
		List<ExportImportPackage<?>> commitPackages = this.exportImportService.importPackages(importFile);

		return null;

	}

	/**
	 * Rules for commit package : one package only
	 * @param commitPackages
	 */
	private void assertImportPackageIsValid(List<ExportImportPackage<?>> commitPackages){
		
	}
}
