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

	private DiffLine mine;

	private DiffLine their;

	/**
	 * 
	 */
	public PreparedMergeIndexEntry() {
		super();
	}

	/**
	 * @return the mine
	 */
	public DiffLine getMine() {
		return this.mine;
	}

	/**
	 * @param mine
	 *            the mine to set
	 */
	public void setMine(DiffLine mine) {
		this.mine = mine;
	}

	/**
	 * @return the their
	 */
	public DiffLine getTheir() {
		return this.their;
	}

	/**
	 * @param their
	 *            the their to set
	 */
	public void setTheir(DiffLine their) {
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
