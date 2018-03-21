package fr.uem.efluid.services;

import static fr.uem.efluid.utils.ErrorType.COMMIT_EXISTS;
import static fr.uem.efluid.utils.ErrorType.COMMIT_IMPORT_INVALID;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
import fr.uem.efluid.model.entities.LobProperty;
import fr.uem.efluid.model.repositories.CommitRepository;
import fr.uem.efluid.model.repositories.DictionaryRepository;
import fr.uem.efluid.model.repositories.FunctionalDomainRepository;
import fr.uem.efluid.model.repositories.IndexRepository;
import fr.uem.efluid.model.repositories.LobPropertyRepository;
import fr.uem.efluid.services.types.CommitDetails;
import fr.uem.efluid.services.types.CommitEditData;
import fr.uem.efluid.services.types.CommitPackage;
import fr.uem.efluid.services.types.DiffDisplay;
import fr.uem.efluid.services.types.ExportFile;
import fr.uem.efluid.services.types.ExportImportResult;
import fr.uem.efluid.services.types.LobPropertyPackage;
import fr.uem.efluid.services.types.MergePreparedDiff;
import fr.uem.efluid.services.types.PilotedCommitPreparation;
import fr.uem.efluid.services.types.PreparedIndexEntry;
import fr.uem.efluid.services.types.PreparedMergeIndexEntry;
import fr.uem.efluid.services.types.RollbackLine;
import fr.uem.efluid.services.types.SharedPackage;
import fr.uem.efluid.utils.ApplicationException;
import fr.uem.efluid.utils.Associate;
import fr.uem.efluid.utils.FormatUtils;

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

	private static final String PCKG_LOBS = "lobs";

	private static final String PCKG_CHERRY_PICK = "commits-cherry-pick";

	private static final Logger LOGGER = LoggerFactory.getLogger(CommitService.class);

	@Autowired
	private CommitRepository commits;

	@Autowired
	private PrepareIndexService diffs;

	@Autowired
	private IndexRepository indexes;

	@Autowired
	private FunctionalDomainRepository domains;

	@Autowired
	private DictionaryRepository dictionary;

	@Autowired
	private LobPropertyRepository lobs;

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
	public ExportImportResult<ExportFile> exportCommits(UUID startingWithCommit) {

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

			Commit startCommit = this.commits.getOne(startingWithCommit);

			// Mark previous ones as "ref only"
			commitsToExport.stream().filter(c -> c.getCreatedTime().isBefore(startCommit.getCreatedTime())).forEach(Commit::setAsRefOnly);
		}

		// Get associated lobs
		List<LobProperty> lobsToExport = loadLobsForCommits(commitsToExport);

		// Then export :
		ExportFile file = this.exportImportService.exportPackages(Arrays.asList(
				new CommitPackage(pckgName, LocalDateTime.now()).initWithContent(commitsToExport),
				new LobPropertyPackage(PCKG_LOBS, LocalDateTime.now()).initWithContent(lobsToExport)));

		ExportImportResult<ExportFile> result = new ExportImportResult<>(file);

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
	public ExportImportResult<ExportFile> exportOneCommit(UUID commitUuid) {

		/*
		 * Export contains all commits. But it is possible to load commit "ref" only for
		 * old commits, and to do a chery pick, with one selected commit
		 */

		LOGGER.debug("Asking for a chery-pick commit export on commit {}", commitUuid);

		Commit exported = this.commits.getOne(commitUuid);

		// Then export :
		ExportFile file = this.exportImportService.exportPackages(Arrays.asList(
				new CommitPackage(PCKG_CHERRY_PICK, LocalDateTime.now()).initWithContent(Collections.singletonList(exported)),
				new LobPropertyPackage(PCKG_LOBS, LocalDateTime.now()).initWithContent(this.lobs.findByCommit(exported))));

		LOGGER.info("Export package for commit {} is ready. Size is {}b", commitUuid, Integer.valueOf(file.getSize()));

		ExportImportResult<ExportFile> result = new ExportImportResult<>(file);

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

		Map<UUID, List<String>> domainNames = this.domains.loadAllDomainNamesByCommitUuids();

		return this.commits.findAll().stream()
				.map(CommitEditData::fromEntity)
				.peek(c -> {
					// Add domain names for each commit (if any)
					List<String> dns = domainNames.get(c.getUuid());
					if (dns != null && dns.size() > 0) {
						c.setDomainNames(dns.stream().collect(Collectors.joining(", ")));
					}
				})
				.collect(Collectors.toList());
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
		CommitDetails details = CommitDetails.fromEntity(this.commits.getOne(commitUUID));

		// Load commit index
		CommitDetails.completeIndex(details, this.indexes.findByCommitUuid(commitUUID));

		Map<UUID, DictionaryEntry> mappedDict = this.dictionary.findAllMappedByUuid();

		// Need to complete DictEnty + HRPayload for index entries
		details.getContent().stream().forEach(d -> {
			DictionaryEntry dict = mappedDict.get(d.getDictionaryEntryUuid());
			d.completeFromEntity(dict);
			this.diffs.completeHrPayload(dict, d.getDiff());
		});

		return details;
	}

	/**
	 * @param encodedLobHash
	 * @return
	 */
	public byte[] getExistingLobData(String encodedLobHash) {

		String decHash = FormatUtils.decodeAsString(encodedLobHash);

		LOGGER.debug("Request for binary content with hash {}", decHash);

		LobProperty lob = this.lobs.findByHash(decHash);

		return lob.getData();
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

			this.applyDiffService.rollbackDiff(rollbacked, prepared.getDiffLobs());
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

		// Prepare used lobs
		List<LobProperty> newLobs = this.diffs.prepareUsedLobsForIndex(entries, prepared.getDiffLobs());

		// Save index and set back to commit with bi-directional link
		newCommit.setIndex(this.indexes.saveAll(entries));

		// Add commit to lobs and save
		newLobs.forEach(l -> l.setCommit(newCommit));
		this.lobs.saveAll(newLobs);

		// Updated commit link
		this.commits.save(newCommit);

		// For merge : apply (will rollback previous steps if error found)
		if (prepared.getPreparingState() == CommitState.MERGED) {
			LOGGER.info("Processing merge commit {} : now apply all {} modifications prepared from imported values",
					commitUUID, Integer.valueOf(entries.size()));
			this.applyDiffService.applyDiff(entries, prepared.getDiffLobs());
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
			ExportFile importFile,
			PilotedCommitPreparation<MergePreparedDiff> currentPreparation) {

		LOGGER.debug("Asking for an import of commit in piloted preparation context {}", currentPreparation.getIdentifier());

		// TODO : dedicated process for Cherry pick

		// #1 Load import
		List<SharedPackage<?>> commitPackages = this.exportImportService.importPackages(importFile);

		// #2 Check package validity
		assertImportPackageIsValid(commitPackages);

		// Get package files - commits
		CommitPackage commitPckg = (CommitPackage) commitPackages.stream().filter(p -> p.getClass() == CommitPackage.class).findFirst()
				.orElseThrow(() -> new ApplicationException(COMMIT_IMPORT_INVALID,
						"Import of commits doens't contains the expected package types"));

		// Get package files - lobs
		LobPropertyPackage lobsPckg = (LobPropertyPackage) commitPackages.stream().filter(p -> p.getClass() == LobPropertyPackage.class)
				.findFirst().orElseThrow(() -> new ApplicationException(COMMIT_IMPORT_INVALID,
						"Import of commits doens't contains the expected package types"));

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

		// #5 Get all lobs
		currentPreparation
				.setDiffLobs(lobsPckg.getContent().stream().collect(Collectors.toConcurrentMap(l -> l.getHash(), l -> l.getData())));

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
				diffContent.stream().map(DiffLine::getKeyValue).collect(Collectors.toList()), null);

		// Completed rollback
		return diffContent.stream()
				.map(current -> new RollbackLine(current, previouses.get(current.getKeyValue())))
				.collect(Collectors.toList());
	}

	/**
	 * @param commitsToExport
	 * @return
	 */
	private List<LobProperty> loadLobsForCommits(List<Commit> commitsToExport) {
		return this.lobs
				.findByCommitUuidIn(commitsToExport.stream().filter(c -> !c.isRefOnly()).map(Commit::getUuid).collect(Collectors.toList()));
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
		if (!this.commits.existsById(commitUUID)) {
			throw new ApplicationException(COMMIT_EXISTS, "Specified commit " + commitUUID + " doesn't exist");
		}
	}

	/**
	 * @param importedSources
	 * @param importedLobs
	 *            Lobs organized by dictionaryEntries
	 * @return
	 */
	private static List<MergePreparedDiff> importedCommitIndexes(List<Commit> importedSources) {

		if (importedSources == null || importedSources.isEmpty()) {
			return new ArrayList<>();
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
	 * Rules for commit package : one commit package + lobs package only
	 * 
	 * @param commitPackages
	 */
	private static void assertImportPackageIsValid(List<SharedPackage<?>> commitPackages) {
		if (commitPackages.size() != 2) {
			throw new ApplicationException(COMMIT_IMPORT_INVALID,
					"Import of commits can contain only commit package file + lobs package file");
		}

		if (commitPackages.stream().noneMatch(p -> p instanceof CommitPackage || p instanceof LobPropertyPackage)) {
			throw new ApplicationException(COMMIT_IMPORT_INVALID, "Import of commits doens't contains the expected package types");
		}
	}
}
