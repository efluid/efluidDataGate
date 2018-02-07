package fr.uem.efluid.model.repositories;

import java.util.List;

import fr.uem.efluid.model.DiffLine;

/**
 * @author elecomte
 * @since v0.0.1
 * @version 1
 */
public interface ManagedUpdateRepository {

	/**
	 * <p>
	 * Should be batched, and made with prepared statment
	 * </p>
	 * 
	 * @param entry
	 * @param lines
	 */
	void runAllChanges(List<DiffLine> lines);
}
