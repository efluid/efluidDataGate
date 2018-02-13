package fr.uem.efluid.services;

import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import fr.uem.efluid.model.DiffLine;
import fr.uem.efluid.model.repositories.ManagedUpdateRepository;
import fr.uem.efluid.services.types.RollbackLine;

/**
 * <p>
 * Where diff content can be applied / rollbacked on a database
 * </p>
 * <p>
 * See {@link ManagedUpdateRepository} for remarks on specific transactional process : use
 * these methods with care.
 * </p>
 * 
 * @author elecomte
 * @since v0.0.1
 * @version 1
 */
@Service
@Transactional // Core DB Transaction, NOT managed DB Transaction
public class ApplyDiffService {

	private static final Logger LOGGER = LoggerFactory.getLogger(ApplyDiffService.class);

	@Autowired
	private ManagedUpdateRepository updates;

	/**
	 * <p>
	 * Due to specific transactional process required on managed DB updated by this
	 * method, always call it with ALL COMBINED DiffLines.
	 * </p>
	 * 
	 * @param diffLines
	 */
	public void applyDiff(List<? extends DiffLine> diffLines) {

		LOGGER.info("Will apply a diff of {} items", Integer.valueOf(diffLines.size()));
		this.updates.runAllChangesAndCommit(diffLines);
	}

	/**
	 * <p>
	 * Due to specific transactional process required on managed DB updated by this
	 * method, always call it with ALL COMBINED DiffLines.
	 * </p>
	 * 
	 * @param rollBackLines
	 */
	public void rollbackDiff(List<RollbackLine> rollBackLines) {

		LOGGER.info("Will apply a rollback of {} items", Integer.valueOf(rollBackLines.size()));
		this.updates.runAllChangesAndCommit(rollBackLines.stream().map(RollbackLine::toCombinedDiff).collect(Collectors.toList()));
	}
}
