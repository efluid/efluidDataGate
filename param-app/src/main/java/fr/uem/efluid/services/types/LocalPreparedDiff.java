package fr.uem.efluid.services.types;

import java.util.List;
import java.util.UUID;

import fr.uem.efluid.model.entities.DictionaryEntry;

/**
 * @author elecomte
 * @since v0.0.1
 * @version 1
 */
public class LocalPreparedDiff extends DiffDisplay<List<PreparedIndexEntry>> {

	/**
	 * @param dict
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
		diff.setDomainName(sum.getDomainName());
		diff.setDomainUuid(sum.getDomainUuid());
		return diff;
	}

	/**
	 * @param entity
	 * @return
	 */
	public static LocalPreparedDiff initFromDictionaryEntry(DictionaryEntry entity) {
		LocalPreparedDiff diff = new LocalPreparedDiff(entity.getUuid());
		diff.setDictionaryEntryName(entity.getParameterName());
		if (entity.getDomain() != null) {
			diff.setDomainName(entity.getDomain().getName());
			diff.setDomainUuid(entity.getDomain().getUuid());
		}
		return diff;
	}
}
