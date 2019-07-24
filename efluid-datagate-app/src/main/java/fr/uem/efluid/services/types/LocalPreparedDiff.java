package fr.uem.efluid.services.types;

import java.util.UUID;

import fr.uem.efluid.model.entities.DictionaryEntry;

/**
 * @author elecomte
 * @since v0.0.1
 * @version 1
 */
public class LocalPreparedDiff extends DiffDisplay<PreparedIndexEntry> {

	/**
	 * @param dictUuid
	 */
	public LocalPreparedDiff(UUID dictUuid) {
		super(dictUuid);
	}

	/**
	 * @param sum
	 * @return
	 */
	public static LocalPreparedDiff initFromDictionaryEntrySummary(DictionaryEntrySummary sum) {
		LocalPreparedDiff diff = new LocalPreparedDiff(sum.getUuid());
		diff.setDictionaryEntryName(sum.getName());
		return diff;
	}

	/**
	 * @param entity
	 * @return
	 */
	public static LocalPreparedDiff initFromDictionaryEntry(DictionaryEntry entity) {
		LocalPreparedDiff diff = new LocalPreparedDiff(entity.getUuid());
		diff.setDictionaryEntryName(entity.getParameterName());
		return diff;
	}
}
