package fr.uem.efluid.services.types;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * @author elecomte
 * @since v0.0.1
 * @version 1
 */
public class MergePreparedDiff extends DiffDisplay<List<PreparedMergeIndexEntry>> {

	private Map<String, byte[]> importedLobs;

	/**
	 * @param dictionaryEntryUuid
	 * @param importedDiff
	 */
	public MergePreparedDiff(UUID dictionaryEntryUuid, List<PreparedMergeIndexEntry> importedDiff) {
		super(dictionaryEntryUuid);
		setDiff(importedDiff);
	}

	/**
	 * @return
	 */
	public boolean isDiffNeedAction() {
		return this.getDiff().stream().anyMatch(PreparedMergeIndexEntry::isNeedAction);
	}

	/**
	 * @return the importedLobs
	 */
	public Map<String, byte[]> getImportedLobs() {
		return this.importedLobs;
	}

	/**
	 * @param importedLobs
	 *            the importedLobs to set
	 */
	public void setImportedLobs(Map<String, byte[]> importedLobs) {
		this.importedLobs = importedLobs;
	}

}
