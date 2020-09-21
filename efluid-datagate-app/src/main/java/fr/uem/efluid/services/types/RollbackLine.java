package fr.uem.efluid.services.types;

import fr.uem.efluid.model.DiffLine;
import fr.uem.efluid.model.entities.IndexAction;

import java.util.Objects;

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

	/**
	 * <p>
	 * Convert the rollback data spec to a combined diff which can be ran as usual
	 * (managed DB updates are always processed from DiffLine). So use basic rules to
	 * define how the rollback can be applied :
	 * <ul>
	 * <li>If current data is null and previous are not, parameter was deleted =&gt; rollback
	 * is an add, to set back previous values</li>
	 * <li>If current is not null but previous, parameter was added =&gt; rollback is a
	 * delete</li>
	 * <li>If both are present, it was an update =&gt; rollback is an other "reverted"
	 * update</li>
	 * </ul>
	 * </p>
	 * 
	 * @return merged data from rollback to make it as a diffLine
	 */
	public DiffLine toCombinedDiff() {

		long timestamp = System.currentTimeMillis();

		// Rollback on delete => became an add
		if ((this.current == null || this.current.getPayload() == null) && this.previous != null && this.previous.getPayload() != null) {
			return DiffLine.combined(
					this.previous.getDictionaryEntryUuid(),
					this.previous.getKeyValue(),
					this.previous.getPayload(),
					null,
					IndexAction.ADD,
					timestamp);
		}

		// Rollback on add => became an delete
		if ((this.previous == null || this.previous.getPayload() == null) && this.current != null && this.current.getPayload() != null) {
			return DiffLine.combined(
					this.current.getDictionaryEntryUuid(),
					this.current.getKeyValue(),
					this.current.getPayload(),
					null,
					IndexAction.REMOVE,
					timestamp);
		}

		// Other case are update current => previous
		return DiffLine.combined(
				Objects.requireNonNull(this.current).getDictionaryEntryUuid(),
				this.current.getKeyValue(),
				Objects.requireNonNull(this.previous).getPayload(),
				this.previous.getPayload(),
				IndexAction.UPDATE,
				timestamp);
	}

}
