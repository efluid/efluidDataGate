package fr.uem.efluid.services.types;

import fr.uem.efluid.model.DiffLine;
import fr.uem.efluid.model.entities.IndexEntry;

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

	private boolean needAction;

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
	 * @return the needAction
	 */
	public boolean isNeedAction() {
		return this.needAction;
	}

	/**
	 * @param needAction
	 *            the needAction to set
	 */
	public void setNeedAction(boolean needAction) {
		this.needAction = needAction;
	}

	/**
	 * <p>
	 * For merge resolution : apply given diff as resolution (change current modification)
	 * </p>
	 * 
	 * @param combined
	 * @param hrPayload
	 */
	public void applyResolution(DiffLine combined, String hrPayload) {

		setAction(combined.getAction());
		setPayload(combined.getPayload());
		setHrPayload(hrPayload);
	}

	/**
	 * Used when reading an imported index content
	 * 
	 * @param partial
	 * @param dict
	 * @return
	 */
	public static PreparedMergeIndexEntry fromImportedEntity(IndexEntry existing) {

		PreparedMergeIndexEntry data = new PreparedMergeIndexEntry();

		completeFromExistingEntity(data, existing);

		return data;
	}

	/**
	 * Used when reading an imported index content
	 * 
	 * @param combined
	 * @param hrPayload
	 * @return
	 */
	public static PreparedMergeIndexEntry fromExistingTheir(PreparedIndexEntry their) {

		PreparedMergeIndexEntry merge = new PreparedMergeIndexEntry();

		merge.setDictionaryEntryUuid(their.getDictionaryEntryUuid());
		merge.setKeyValue(their.getKeyValue());
		merge.setTheir(their);
		merge.setMine(their);

		return merge;
	}

}
