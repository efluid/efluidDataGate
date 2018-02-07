package fr.uem.efluid.services;

import java.util.List;

import org.springframework.stereotype.Service;

import fr.uem.efluid.model.DiffLine;
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

	
	
	/**
	 * @param diffLines
	 */
	public void applyDiff(List<DiffLine> diffLines) {

	}

	/**
	 * @param rollBackLines
	 */
	public void rollbackDiff(List<RollbackLine> rollBackLines) {

	}
}
