package fr.uem.efluid.services;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import fr.uem.efluid.model.DiffLine;
import fr.uem.efluid.model.repositories.ManagedUpdateRepository;
import fr.uem.efluid.services.types.RollbackLine;

/**
 * <p>
 * Where diff content can be applied / rollbacked on a database
 * </p>
 * 
 * @author elecomte
 * @since v0.0.1
 * @version 1
 */
@Service
public class ApplyDiffService {

	@Autowired
	private ManagedUpdateRepository updates;

	/**
	 * @param diffLines
	 */
	public void applyDiff(List<DiffLine> diffLines) {

		this.updates.runAllChanges(diffLines);
	}

	/**
	 * @param rollBackLines
	 */
	public void rollbackDiff(List<RollbackLine> rollBackLines) {

		this.updates.runAllChanges(rollBackLines.stream().map(RollbackLine::toCombinedDiff).collect(Collectors.toList()));
	}
}
