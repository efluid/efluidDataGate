package fr.uem.efluid.services;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import fr.uem.efluid.model.entities.DictionaryEntry;
import fr.uem.efluid.model.repositories.DictionaryRepository;
import fr.uem.efluid.services.types.PilotedCommitPreparation;
import fr.uem.efluid.services.types.PilotedCommitStatus;
import fr.uem.efluid.services.types.PreparedIndexEntry;
import fr.uem.efluid.utils.TechnicalException;

/**
 * <p>
 * Service for Commit preparation, using async execution. <b>Only one execution can be
 * launched.</b>
 * </p>
 * 
 * @author elecomte
 * @since v0.0.1
 * @version 1
 */
@Service
public class PilotableCommitPreparationService {

	private static final Logger LOGGER = LoggerFactory.getLogger(PilotableCommitPreparationService.class);

	@Autowired
	private DataDiffService diffService;

	@Autowired
	private DictionaryRepository dictionary;

	// TODO : use cfg entry.
	private ExecutorService executor = Executors.newFixedThreadPool(4);

	// One active only
	private PilotedCommitPreparation current;

	/**
	 * Start async diff analysis before commit
	 */
	public PilotedCommitPreparation startCommitPreparation(boolean force) {

		// On existing preparation
		if (this.current != null) {

			// Forced restart asked : close current, start a new one
			if (force) {
				LOGGER.info("Request for a new commit preparation - preparation exist already, and "
						+ "force restart asked, so will drop current preparation {}", this.current.getIdentifier());

				// Cancel for any existing reference ...
				this.current.setStatus(PilotedCommitStatus.CANCEL);

				// ... but droping should be enough
				this.current = null;
			}

			// Keep current else
			else {

				LOGGER.info("Request for a new commit preparation - preparation already running / "
						+ "available, so use existing {}", this.current.getIdentifier());

				// Default will provides existing if still running
				return getCurrentCommitPreparation();
			}
		}

		LOGGER.info("Request for a new commit preparation - start a new one");

		this.current = new PilotedCommitPreparation();

		CompletableFuture.runAsync(this::processAllDiff);

		return this.current;
	}

	/**
	 * @return
	 */
	public PilotedCommitPreparation getCurrentCommitPreparation() {
		return this.current;
	}

	/**
	 * 
	 */
	public void completeCommitPreparation() {
		this.current = null;
	}

	/**
	 * <p>
	 * Asynchronous task which is itself a process of asynchronous execution of managed
	 * table diffs (one task for each managed table). Similar to a "git status"
	 * </p>
	 * <p>
	 * Use parallele processes, but not asyncronous by itself : can be launched as a
	 * CompletableFuture in call processes
	 * </p>
	 */
	private void processAllDiff() {

		LOGGER.info("Begin diff process on commit preparation {}", this.current.getIdentifier());
		long startTimeout = System.currentTimeMillis();

		try {
			List<PreparedIndexEntry> fullDiff = this.executor
					.invokeAll(this.dictionary.findAll().stream().map(this::callDiff).collect(Collectors.toList())).stream()
					.map(this::gatherResult)
					.flatMap(DiffCallResult::getDiff)
					.sorted(indexSorter())
					.collect(Collectors.toList());

			// Keep in preparation for commit build
			this.current.setPreparedDiff(fullDiff);

			// Mark preparation as completed
			this.current.setEnd(LocalDateTime.now());
			this.current.setStatus(PilotedCommitStatus.COMMIT_CAN_PREPARE);

			LOGGER.info("Diff process completed on commit preparation {}. Total process duration was {} ms", this.current.getIdentifier(),
					Long.valueOf(System.currentTimeMillis() - startTimeout));

		} catch (InterruptedException e) {
			LOGGER.error("Error will processing diff", e);
			this.current.setErrorDuringPreparation(e);
		}
	}

	/**
	 * <p>
	 * Join future execution and gather exception if any
	 * </p>
	 * 
	 * @param future
	 * @return
	 */
	private DiffCallResult gatherResult(Future<DiffCallResult> future) {
		try {
			return future.get();
		}

		// TODO : better error identification
		catch (InterruptedException | ExecutionException e) {
			LOGGER.error("Error will processing diff", e);
			this.current.setErrorDuringPreparation(e);
			throw new TechnicalException("Aborted on exception ", e);
		}
	}

	/**
	 * <p>
	 * Execution for one table, as a <ttCallable</tt>
	 * </p>
	 * 
	 * @param dict
	 * @return
	 */
	private Callable<DiffCallResult> callDiff(DictionaryEntry dict) {
		return () -> {
			return new DiffCallResult(dict.getUuid(), this.diffService.processDiff(dict));
		};
	}

	/**
	 * <p>
	 * Comparator for IndexEntry : by domain name, then dictionnary name, then by
	 * timestamp
	 * </p>
	 * 
	 * @return
	 */
	private static Comparator<PreparedIndexEntry> indexSorter() {

		return (a, b) -> {
			int dom = a.getDomainName().compareTo(b.getDomainName());

			if (dom != 0) {
				return dom;
			}

			int dic = a.getDictionaryEntryName().compareTo(b.getDictionaryEntryName());

			if (dic != 0) {
				return dic;
			}

			return (int) (a.getTimestamp() - b.getTimestamp());
		};
	}

	/**
	 * <p>
	 * Combined result for easier mapping from Future execution
	 * </p>
	 * 
	 * @author elecomte
	 * @since v0.0.1
	 * @version 1
	 */
	@SuppressWarnings("unused")
	private static final class DiffCallResult {

		private final UUID dictUuid;
		private final Collection<PreparedIndexEntry> diff;

		/**
		 * @param dictUuid
		 * @param diff
		 */
		public DiffCallResult(UUID dictUuid, Collection<PreparedIndexEntry> diff) {
			super();
			this.dictUuid = dictUuid;
			this.diff = diff;
		}

		/**
		 * @return the dictUuid
		 */
		public UUID getDictUuid() {
			return this.dictUuid;
		}

		/**
		 * @return the diff
		 */
		public Stream<PreparedIndexEntry> getDiff() {
			return this.diff.stream();
		}
	}
}
