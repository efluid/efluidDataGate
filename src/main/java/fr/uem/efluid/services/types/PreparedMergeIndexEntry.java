package fr.uem.efluid.services.types;

/**
 * @author elecomte
 * @since v0.0.1
 * @version 1
 */
public class PreparedMergeIndexEntry {

	private PreparedIndexEntry mine;

	private PreparedIndexEntry their;

	private PreparedIndexEntry result;

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
	 * @return the result
	 */
	public PreparedIndexEntry getResult() {
		return this.result;
	}

	/**
	 * @param result
	 *            the result to set
	 */
	public void setResult(PreparedIndexEntry result) {
		this.result = result;
	}

}
