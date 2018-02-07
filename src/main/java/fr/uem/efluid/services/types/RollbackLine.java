package fr.uem.efluid.services.types;

import fr.uem.efluid.model.DiffLine;

/**
 * @author elecomte
 * @since v0.0.1
 * @version 1
 */
public class RollbackLine {

	private final DiffLine current;

	private final DiffLine previous;

	/**
	 * @param current
	 * @param previous
	 */
	public RollbackLine(DiffLine current, DiffLine previous) {
		super();
		this.current = current;
		this.previous = previous;
	}

	/**
	 * @return the current
	 */
	public DiffLine getCurrent() {
		return this.current;
	}

	/**
	 * @return the previous
	 */
	public DiffLine getPrevious() {
		return this.previous;
	}

}
