package fr.uem.efluid.services;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import fr.uem.efluid.model.entities.Commit;
import fr.uem.efluid.model.repositories.CommitRepository;
import fr.uem.efluid.model.repositories.impls.InMemoryManagedRegenerateRepository;
import fr.uem.efluid.services.ExportImportService.ExportImportPackage;
import fr.uem.efluid.services.PilotableCommitPreparationService.PilotedCommitPreparation;
import fr.uem.efluid.services.types.CommitEditData;
import fr.uem.efluid.services.types.CommitPackage;
import fr.uem.efluid.services.types.ExportImportFile;
import fr.uem.efluid.services.types.ExportImportResult;
import fr.uem.efluid.services.types.LocalPreparedDiff;
import fr.uem.efluid.services.types.MergePreparedDiff;
import fr.uem.efluid.services.types.PreparedIndexEntry;
import fr.uem.efluid.utils.Associate;
import fr.uem.efluid.utils.TechnicalException;

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
	 * <p>
	 * From the prepared commit, rollback in local managed DB everything which was
	 * rejected
	 * </p>
	 */
	public void applyExclusionsFromLocalCommit(PilotedCommitPreparation<LocalPreparedDiff> prepared) {

		

	}

	/**
	 * <p>
	 * Apply the changes from the prepared merge diff, and store the commit (including the
	 * index content)
	 * </p>
	 */
	public void applyAndSaveMergeCommit() {

	}

	/**
	 * <p>
	 * Apply the changes from the prepared local diff, and store the commit (including the
	 * index content)
	 * </p>
	 */
	public void applyAndSaveLocalCommit() {

	}

	/**
	 * <p>
	 * Reserved for launch from <tt>PilotableCommitPreparationService</tt>
	 * 
	 * @param importFile
	 */
	ExportImportResult<PilotedCommitPreparation<MergePreparedDiff>> importCommits(
			ExportImportFile importFile,
			PilotedCommitPreparation<MergePreparedDiff> currentPreparation) {

		LOGGER.debug("Asking for an import of commit in piloted preparation context {}", currentPreparation.getIdentifier());

		// TODO : dedicated process for Cherry pick

		// #1 Load import
		List<ExportImportPackage<?>> commitPackages = this.exportImportService.importPackages(importFile);

		// #2 Check package validity
		assertImportPackageIsValid(commitPackages);

		CommitPackage commitPckg = (CommitPackage) commitPackages.get(0);

		Map<UUID, Commit> localCommits = this.commits.findAll().stream()
				.collect(Collectors.toMap(Commit::getUuid, v -> v));

		Map<UUID, Commit> mergedCommits = localCommits.values().stream()
				.flatMap(c -> Associate.onFlatmapOf(c.getMergeSources(), c))
				.collect(Collectors.toMap(Associate::getOne, Associate::getTwo));

		List<Commit> toProcess = new ArrayList<>();
		LocalDateTime timeProcessStart = null;

		// Need to be sorted by create time
		Collections.sort(commitPckg.getContent(), Comparator.comparing(Commit::getCreatedTime));

		// #3
		for (Commit imported : commitPckg.getContent()) {

			boolean hasItLocaly = localCommits.containsKey(imported.getUuid()) || mergedCommits.containsKey(imported.getUuid());

			// It's a ref : we MUST have it locally (imported as this or merged)
			if (imported.isRefOnly()) {

				// Impossible situation
				if (!hasItLocaly) {
					throw new TechnicalException("Imported package is not compliant : the requested ref commit " + imported.getUuid()
							+ " is not imported yet nore merged in local commit base.");
				}

				LOGGER.debug("Imported ref commit {} is already managed in local db. As a valid reference, ignore it", imported.getUuid());

			} else {

				if (hasItLocaly) {
					LOGGER.debug("Imported commit {} is already managed in local db. Ignore it", imported.getUuid());
				}

				// This one is not yet imported or merged : keep it for processing
				else {
					LOGGER.debug("Imported commit {} is not yet managed in local db. Will process it", imported.getUuid());
					toProcess.add(imported);

					// Start time for local diff search
					if (timeProcessStart == null) {
						timeProcessStart = imported.getCreatedTime();
						LOGGER.debug("As the imported commit {} is the first missing one, will use it's time {} to identify"
								+ " local diff to run", imported.getUuid(), timeProcessStart);
					}
				}
			}
		}

		// Create the future merge commit info
		currentPreparation.setCommitData(new CommitEditData());
		currentPreparation.getCommitData().setMergeSources(toProcess.stream().map(Commit::getUuid).collect(Collectors.toList()));
		currentPreparation.getCommitData().setRangeStartTime(timeProcessStart);
		currentPreparation.getCommitData().setComment(generateMergeCommitComment(toProcess));

		// Init prepared merge with imported index
		currentPreparation.setPreparedContent(importedCommitIndexes(toProcess));

		// Result for direct display (with ref to preparation)
		ExportImportResult<PilotedCommitPreparation<MergePreparedDiff>> result = new ExportImportResult<>(currentPreparation);

		// Can show number of processing commits
		result.addCount(PCKG_ALL, commitPckg.getContent().size(), toProcess.size(), 0);

		return result;

	}

	/**
	 * @param importedSources
	 * @return
	 */
	private static List<MergePreparedDiff> importedCommitIndexes(List<Commit> importedSources) {

		// Organized by DictionaryEntries
		Map<UUID, List<PreparedIndexEntry>> organized = importedSources.stream()
				.flatMap(c -> c.getIndex().stream())
				.map(PreparedIndexEntry::fromExistingEntity)
				.collect(Collectors.groupingBy(PreparedIndexEntry::getDictionaryEntryUuid));

		// And specified as MergePreparedDiff for complete compatibility with prepare
		return organized.entrySet().stream()
				.map(e -> new MergePreparedDiff(e.getKey(), e.getValue()))
				.collect(Collectors.toList());
	}

	/**
	 * @param sources
	 * @return
	 */
	private static String generateMergeCommitComment(List<Commit> sources) {

		return "###MERGE COMMIT###\nSources :\n * " + sources.stream().map(Commit::getComment).collect(Collectors.joining("\n * "));
	}

	/**
	 * Rules for commit package : one package only
	 * 
	 * @param commitPackages
	 */
	private static void assertImportPackageIsValid(List<ExportImportPackage<?>> commitPackages) {
		if (commitPackages.size() != 1) {
			throw new TechnicalException("Import of commits can contain only one package file");
		}

		if (!(commitPackages.get(0) instanceof CommitPackage)) {
			throw new TechnicalException("Import of commits doens't contains the expected package");
		}
	}
}
