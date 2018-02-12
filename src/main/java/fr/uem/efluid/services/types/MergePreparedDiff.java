package fr.uem.efluid.services.types;

import java.util.List;
import java.util.UUID;

import fr.uem.efluid.model.DiffLine;

/**
 * @author elecomte
 * @since v0.0.1
 * @version 1
 */
public class MergePreparedDiff extends DiffDisplay<List<PreparedMergeIndexEntry>> {

	private List<? extends DiffLine> importedDiff;

	/**
	 * @param dictionaryEntryUuid
	 * @param importedDiff
	 */
	public MergePreparedDiff(UUID dictionaryEntryUuid, List<? extends DiffLine> importedDiff) {
		super(dictionaryEntryUuid);
		this.importedDiff = importedDiff;
	}

	/**
	 * @return the importedDiff
	 */
	public List<? extends DiffLine> getImportedDiff() {
		return this.importedDiff;
	}

	/**
	 * @param importedDiff
	 *            the importedDiff to set
	 */
	public void setImportedDiff(List<? extends DiffLine> importedDiff) {
		this.importedDiff = importedDiff;
	}

}
