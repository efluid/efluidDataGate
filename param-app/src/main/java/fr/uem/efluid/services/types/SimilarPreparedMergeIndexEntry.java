package fr.uem.efluid.services.types;

import java.util.List;

/**
 * <p>
 * For rendering when multiple entries share the same content
 * </p>
 * 
 * @author elecomte
 * @since v0.0.1
 * @version 1
 */
public class SimilarPreparedMergeIndexEntry extends PreparedMergeIndexEntry {

	private List<Long> ids;

	private List<String> keyValues;

	private List<PreparedIndexEntry> mines;

	private List<PreparedIndexEntry> theirs;

	/**
	 * 
	 */
	public SimilarPreparedMergeIndexEntry() {
		super();
	}

	/**
	 * @return
	 * @see fr.uem.efluid.services.types.PreparedMergeIndexEntry#getMine()
	 */
	@Override
	public PreparedIndexEntry getMine() {
		return null;
	}

	/**
	 * @return
	 * @see fr.uem.efluid.services.types.PreparedMergeIndexEntry#getTheir()
	 */
	@Override
	public PreparedIndexEntry getTheir() {
		return null;
	}

	/**
	 * @return
	 * @see fr.uem.efluid.services.types.PreparedIndexEntry#getId()
	 */
	@Override
	public Long getId() {
		return null;
	}

	/**
	 * @return
	 * @see fr.uem.efluid.services.types.PreparedIndexEntry#getKeyValue()
	 */
	@Override
	public String getKeyValue() {
		return null;
	}

	/**
	 * @return the ids
	 */
	public List<Long> getIds() {
		return this.ids;
	}

	/**
	 * @param ids
	 *            the ids to set
	 */
	public void setIds(List<Long> ids) {
		this.ids = ids;
	}

	/**
	 * @return the keyValues
	 */
	public List<String> getKeyValues() {
		return this.keyValues;
	}

	/**
	 * @param keyValues
	 *            the keyValues to set
	 */
	public void setKeyValues(List<String> keyValues) {
		this.keyValues = keyValues;
	}

	/**
	 * @return the mines
	 */
	public List<PreparedIndexEntry> getMines() {
		return this.mines;
	}

	/**
	 * @param mines
	 *            the mines to set
	 */
	public void setMines(List<PreparedIndexEntry> mines) {
		this.mines = mines;
	}

	/**
	 * @return the theirs
	 */
	public List<PreparedIndexEntry> getTheirs() {
		return this.theirs;
	}

	/**
	 * @param theirs
	 *            the theirs to set
	 */
	public void setTheirs(List<PreparedIndexEntry> theirs) {
		this.theirs = theirs;
	}

	/**
	 * @return
	 * @see fr.uem.efluid.services.types.PreparedIndexEntry#isDisplayOnly()
	 */
	@Override
	public boolean isDisplayOnly() {
		return true;
	}

}
