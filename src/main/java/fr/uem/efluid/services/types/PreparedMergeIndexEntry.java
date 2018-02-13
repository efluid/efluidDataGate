package fr.uem.efluid.services.types;

import fr.uem.efluid.model.DiffLine;
import fr.uem.efluid.model.entities.DictionaryEntry;

/**
 * <p>
 * If result is null after prepare, then this entry is totaly IGNORED
 * </p>
 * 
 * @author elecomte
 * @since v0.0.1
 * @version 1
 */
public class PreparedMergeIndexEntry extends PreparedIndexEntry {

	private PreparedIndexEntry mine;

	private PreparedIndexEntry their;

	/**
	 * 
	 */
	public PreparedMergeIndexEntry() {
		super();
	}

	/**
	 * @return the mine
	 */
	public PreparedIndexEntry getMine() {
		return this.mine;
	}

	/**
	 * @param mine
	 *            the mine to set
	 */
	public void setMine(PreparedIndexEntry mine) {
		this.mine = mine;
	}

	/**
	 * @return the their
	 */
	public PreparedIndexEntry getTheir() {
		return this.their;
	}

	/**
	 * @param their
	 *            the their to set
	 */
	public void setTheir(PreparedIndexEntry their) {
		this.their = their;
	}

	/**
	 * <p>
	 * For combining process : need to recreate a combined DiffLine as a complete
	 * PreparedIndexEntry for clean rendering and further saving process
	 * </p>
	 * 
	 * @param combined
	 * @param dict
	 * @param keyValue
	 * @param timestamp
	 * @return
	 */
	public static PreparedMergeIndexEntry fromImport(DiffLine combined, DictionaryEntry dict, String keyValue, long timestamp) {

		PreparedMergeIndexEntry data = new PreparedMergeIndexEntry();

		data.setAction(combined.getAction());

		data.setDictionaryEntryName(dict.getParameterName());
		data.setDictionaryEntryUuid(dict.getUuid());
		data.setDomainName(dict.getDomain().getName());
		data.setDomainUuid(dict.getDomain().getUuid());

		data.setPayload(combined.getPayload());
		data.setKeyValue(keyValue);
		data.setTimestamp(timestamp);

		return data;
	}


}
