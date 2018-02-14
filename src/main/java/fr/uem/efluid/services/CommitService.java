package fr.uem.efluid.services;

import static fr.uem.efluid.utils.ErrorType.COMMIT_EXISTS;
import static fr.uem.efluid.utils.ErrorType.COMMIT_IMPORT_INVALID;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.assertj.core.util.Lists;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import fr.uem.efluid.model.DiffLine;
import fr.uem.efluid.model.entities.Commit;
import fr.uem.efluid.model.entities.CommitState;
import fr.uem.efluid.model.entities.DictionaryEntry;
import fr.uem.efluid.model.entities.IndexEntry;
import fr.uem.efluid.model.repositories.CommitRepository;
import fr.uem.efluid.model.repositories.IndexRepository;
import fr.uem.efluid.model.repositories.impls.InMemoryManagedRegenerateRepository;
import fr.uem.efluid.services.ExportImportService.ExportImportPackage;
import fr.uem.efluid.services.PilotableCommitPreparationService.PilotedCommitPreparation;
import fr.uem.efluid.services.types.CommitDetails;
import fr.uem.efluid.services.types.CommitEditData;
import fr.uem.efluid.services.types.CommitPackage;
import fr.uem.efluid.services.types.DiffDisplay;
import fr.uem.efluid.services.types.ExportImportFile;
import fr.uem.efluid.services.types.ExportImportResult;
import fr.uem.efluid.services.types.MergePreparedDiff;
import fr.uem.efluid.services.types.PreparedIndexEntry;
import fr.uem.efluid.services.types.PreparedMergeIndexEntry;
import fr.uem.efluid.services.types.RollbackLine;
import fr.uem.efluid.utils.ApplicationException;
import fr.uem.efluid.utils.Associate;

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
@Transactional
@Service
public class CommitService extends AbstractApplicationService {

	private static final String PCKG_ALL = "commits-all";
	private static final String PCKG_AFTER = "commits-part";
	private static final String PCKG_CHERRY_PICK = "commits-cherry-pick";

	private static final Logger LOGGER = LoggerFactory.getLogger(InMemoryManagedRegenerateRepository.class);

	@Autowired
	private CommitRepository commits;

	@Autowired
	private PrepareIndexService diffs;

	@Autowired
	private IndexRepository indexes;

	@Autowired
	private ExportImportService exportImportService;

	@Autowired
	private ApplyDiffService applyDiffService;

	/**
	 * <p>
	 * Export complete commit list, with option to include only one fraction of the index
	 * content
	 * </p>
	 * 
	 * @param startingWithCommit
	 *            optional uuid of a commit which will be the 1st fully exported, will
	 *            previous ones will be exported as ref-only
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
	 * @return
	 */
	public List<CommitEditData> getAvailableCommits() {

		LOGGER.debug("Request for list of available commits");

		return this.commits.findAll().stream().map(CommitEditData::fromEntity).collect(Collectors.toList());
	}

	/**
	 * @param commitUUID
	 * @return
	 */
	public CommitDetails getExistingCommitDetails(UUID commitUUID) {

		LOGGER.debug("Request for details on existing commit {}", commitUUID);

		// Must exist
		assertCommitExists(commitUUID);

		// Load details
		CommitDetails details = CommitDetails.fromEntity(this.commits.findOne(commitUUID));

		// Need to complete HRPayload for index entries
		this.diffs.completeHrPayload(details.getIndex());

		return details;
	}

	/**
	 * <p>
	 * From the prepared commit, rollback in local managed DB everything which was
	 * rejected. Generic enough for compatibility with both local and merge commits
	 * </p>
	 */
	void applyExclusionsFromLocalCommit(
			PilotedCommitPreparation<? extends DiffDisplay<? extends List<? extends PreparedIndexEntry>>> prepared) {

		LOGGER.debug("Process preparation of rollback from prepared commit, if any");

		List<RollbackLine> rollbacked = prepared.getPreparedContent().stream().flatMap(this::streamDiffRollbacks)
				.collect(Collectors.toList());

		if (rollbacked.size() > 0) {

			LOGGER.info("In current commit preparation, a total of {} rollback entries were identified and are going to be applied",
					Integer.valueOf(rollbacked.size()));

			this.applyDiffService.rollbackDiff(rollbacked);
		}
	}

	/**
	 * <p>
	 * Apply the changes from the prepared local diff, and store the commit (including the
	 * index content)
	 * </p>
	 * 
	 * @param prepared
	 *            preparation source for commit. Can be a local or a merge commit
	 *            preparation
	 * @return created commit uuid
	 */
	UUID saveAndApplyPreparedCommit(
			PilotedCommitPreparation<? extends DiffDisplay<? extends List<? extends PreparedIndexEntry>>> prepared) {

		LOGGER.debug("Process apply and saving of a new commit with state {}", prepared.getPreparingState());

		Commit newCommit = CommitEditData.toEntity(prepared.getCommitData());
		newCommit.setCreatedTime(LocalDateTime.now());
		newCommit.setUser(getCurrentUser());
		newCommit.setOriginalUserEmail(newCommit.getUser().getEmail());
		newCommit.setState(prepared.getPreparingState());

		// Prepared commit uuid
		UUID commitUUID = UUID.randomUUID();

		// UUID generate (not done by HBM / DB)
		newCommit.setUuid(commitUUID);

		// Init commit
		this.commits.save(newCommit);

		LOGGER.debug("Processing commit {} : commit initialized, preparing index content", commitUUID);

		List<IndexEntry> entries = prepared.getPreparedContent().stream()
				.flatMap(l -> l.getDiff().stream())
				.filter(PreparedIndexEntry::isSelected)
				.map(PreparedIndexEntry::toEntity)
				.peek(e -> e.setCommit(newCommit))
				.collect(Collectors.toList());

		LOGGER.debug("New commit {} of state {} with comment {} prepared with {} index lines",
				newCommit.getUuid(), prepared.getPreparingState(), newCommit.getComment(), Integer.valueOf(entries.size()));

		// Save index and set back to commit with bi-directional link
		newCommit.setIndex(this.indexes.save(entries));

		// Updated commit link
		this.commits.save(newCommit);

		// For merge : apply (will rollback previous steps if error found)
		if (prepared.getPreparingState() == CommitState.MERGED) {
			LOGGER.info("Processing merge commit {} : now apply all {} modifications prepared from imported values",
					commitUUID, Integer.valueOf(entries.size()));
			this.applyDiffService.applyDiff(entries);
			LOGGER.debug("Processing merge commit {} : diff applied with success", commitUUID);
		}

		return commitUUID;
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

		// Only one package possible for commit import
		CommitPackage commitPckg = (CommitPackage) commitPackages.get(0);

		LOGGER.debug("Import of commits from package {} initiated", commitPckg);

		// #3 Extract local data to merge with
		Map<UUID, Commit> localCommits = this.commits.findAll().stream()
				.collect(Collectors.toMap(Commit::getUuid, v -> v));
		Map<UUID, Commit> mergedCommits = localCommits.values().stream()
				.flatMap(c -> Associate.onFlatmapOf(c.getMergeSources(), c))
				.collect(Collectors.toMap(Associate::getOne, Associate::getTwo));

		List<Commit> toProcess = new ArrayList<>();
		LocalDateTime timeProcessStart = null;

		// Need to be sorted by create time
		Collections.sort(commitPckg.getContent(), Comparator.comparing(Commit::getCreatedTime));

		// #4 Process commits, one by one
		for (Commit imported : commitPckg.getContent()) {

			// Check if already stored localy (in "local" or "merged")
			boolean hasItLocaly = localCommits.containsKey(imported.getUuid()) || mergedCommits.containsKey(imported.getUuid());

			// It's a ref : we MUST have it locally (imported as this or merged)
			if (imported.isRefOnly()) {

				// Impossible situation
				if (!hasItLocaly) {
					throw new ApplicationException(COMMIT_IMPORT_INVALID,
							"Imported package is not compliant : the requested ref commit " + imported.getUuid()
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

		LOGGER.info("Import of commits from package {} done  : now the merge data is ready with {} source commits", commitPckg,
				Integer.valueOf(commitPckg.getContent().size()));

		return result;

	}

	/**
	 * <p>
	 * Complete given diff as a one to rollback
	 * </p>
	 * 
	 * @param entry
	 * @param diffContent
	 * @return
	 */
	private List<RollbackLine> getDiffRollbacks(DictionaryEntry entry, Collection<? extends DiffLine> diffContent) {

		// All "previous" for current diff
		Map<String, IndexEntry> previouses = this.indexes.findAllPreviousIndexEntries(entry,
				diffContent.stream().map(DiffLine::getKeyValue).collect(Collectors.toList()));

		// Completed rollback
		return diffContent.stream()
				.map(current -> new RollbackLine(current, previouses.get(current.getKeyValue())))
				.collect(Collectors.toList());
	}

	/**
	 * <p>
	 * Get rollback specified in one DiffDisplay
	 * </p>
	 * 
	 * @param diff
	 * @return
	 */
	private Stream<RollbackLine> streamDiffRollbacks(DiffDisplay<? extends List<? extends PreparedIndexEntry>> diff) {

		LOGGER.debug("Process identification of rollback on dictionaryEntry {}, if any", diff.getDictionaryEntryUuid());

		return getDiffRollbacks(new DictionaryEntry(diff.getDictionaryEntryUuid()),
				diff.getDiff().stream().filter(PreparedIndexEntry::isRollbacked).collect(Collectors.toList()))
						.stream();
	}

	/**
	 * @param commitUUID
	 */
	private void assertCommitExists(UUID commitUUID) {
		if (!this.commits.exists(commitUUID)) {
			throw new ApplicationException(COMMIT_EXISTS, "Specified commit " + commitUUID + " doesn't exist");
		}
	}

	/**
	 * @param importedSources
	 * @return
	 */
	private static List<MergePreparedDiff> importedCommitIndexes(List<Commit> importedSources) {

		if (importedSources == null || importedSources.isEmpty()) {
			return Lists.newArrayList();
		}

		// Organized by DictionaryEntries
		Map<UUID, List<PreparedMergeIndexEntry>> organized = importedSources.stream()
				.flatMap(c -> c.getIndex().stream())
				.map(PreparedMergeIndexEntry::fromImportedEntity)
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
			throw new ApplicationException(COMMIT_IMPORT_INVALID, "Import of commits can contain only one package file");
		}

		if (!(commitPackages.get(0) instanceof CommitPackage)) {
			throw new ApplicationException(COMMIT_IMPORT_INVALID, "Import of commits doens't contains the expected package");
		}
	}
}
