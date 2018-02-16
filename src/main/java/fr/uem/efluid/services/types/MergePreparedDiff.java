package fr.uem.efluid.services.types;

import java.util.List;
import java.util.UUID;

/**
 * @author elecomte
 * @since v0.0.1
 * @version 1
 */
public class MergePreparedDiff extends DiffDisplay<List<PreparedMergeIndexEntry>> {

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

}
