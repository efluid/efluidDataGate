package fr.uem.efluid.services.types;

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


}
