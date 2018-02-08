package fr.uem.efluid.services.types;

import java.util.UUID;

import fr.uem.efluid.model.DiffLine;
import fr.uem.efluid.model.entities.IndexAction;

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
	 * <li>If current data is null and previous are not, parameter was deleted => rollback
	 * is an add, to set back previous values</li>
	 * <li>If current is not null but previous, parameter was added => rollback is a
	 * delete</li>
	 * <li>If both are present, it was an update => rollback is an other "reverted"
	 * update</li>
	 * </ul>
	 * </p>
	 * 
	 * @return merged data from rollback to make it as a diffLine
	 */
	public DiffLine toCombinedDiff() {

		// Rollback on delete => became an add
		if ((this.current == null || this.current.getPayload() == null) && this.previous != null && this.previous.getPayload() != null) {
			return new CombinedDiffLine(
					this.previous.getDictionaryEntryUuid(),
					this.previous.getKeyValue(),
					this.previous.getPayload(),
					IndexAction.ADD);
		}

		// Rollback on add => became an delete
		if ((this.previous == null || this.previous.getPayload() == null) && this.current != null && this.current.getPayload() != null) {
			return new CombinedDiffLine(
					this.current.getDictionaryEntryUuid(),
					this.current.getKeyValue(),
					this.current.getPayload(),
					IndexAction.REMOVE);
		}

		// Other case are update current => previous
		return new CombinedDiffLine(
				this.current.getDictionaryEntryUuid(),
				this.current.getKeyValue(),
				this.previous.getPayload(),
				IndexAction.UPDATE);
	}

	/**
	 * @author elecomte
	 * @since v0.0.1
	 * @version 1
	 */
	private static class CombinedDiffLine implements DiffLine {

		private final UUID dictionaryEntryUuid;

		private final String keyValue;

		private final String payload;

		private final IndexAction action;

		/**
		 * @param dictionaryEntryUuid
		 * @param keyValue
		 * @param payload
		 * @param action
		 */
		CombinedDiffLine(UUID dictionaryEntryUuid, String keyValue, String payload, IndexAction action) {
			super();
			this.dictionaryEntryUuid = dictionaryEntryUuid;
			this.keyValue = keyValue;
			this.payload = payload;
			this.action = action;
		}

		/**
		 * @return the dictionaryEntryUuid
		 */
		@Override
		public UUID getDictionaryEntryUuid() {
			return this.dictionaryEntryUuid;
		}

		/**
		 * @return the keyValue
		 */
		@Override
		public String getKeyValue() {
			return this.keyValue;
		}

		/**
		 * @return the payload
		 */
		@Override
		public String getPayload() {
			return this.payload;
		}

		/**
		 * @return the action
		 */
		@Override
		public IndexAction getAction() {
			return this.action;
		}

		/**
		 * @return
		 * @see java.lang.Object#toString()
		 */
		@Override
		public String toString() {
			return "Rollback [dict:" + getDictionaryEntryUuid() + ", chg:" + this.action + "@" + this.keyValue + "|" + this.payload + "]";
		}
	}
}
