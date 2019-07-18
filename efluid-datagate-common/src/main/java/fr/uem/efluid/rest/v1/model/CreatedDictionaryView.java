package fr.uem.efluid.rest.v1.model;

/**
 * @author elecomte
 * @since v0.0.1
 * @version 1
 */
public class CreatedDictionaryView {

	private long addedDictionaryEntryCount;
	private long addedDomainCount;
	private long addedLinkCount;

	private long updatedDictionaryEntryCount;
	private long updatedDomainCount;
	private long updatedLinkCount;

	/**
	 * 
	 */
	public CreatedDictionaryView() {
		super();
	}

	/**
	 * @return the addedDictionaryEntryCount
	 */
	public long getAddedDictionaryEntryCount() {
		return this.addedDictionaryEntryCount;
	}

	/**
	 * @param addedDictionaryEntryCount
	 *            the addedDictionaryEntryCount to set
	 */
	public void setAddedDictionaryEntryCount(long addedDictionaryEntryCount) {
		this.addedDictionaryEntryCount = addedDictionaryEntryCount;
	}

	/**
	 * @return the addedDomainCount
	 */
	public long getAddedDomainCount() {
		return this.addedDomainCount;
	}

	/**
	 * @param addedDomainCount
	 *            the addedDomainCount to set
	 */
	public void setAddedDomainCount(long addedDomainCount) {
		this.addedDomainCount = addedDomainCount;
	}

	/**
	 * @return the addedLinkCount
	 */
	public long getAddedLinkCount() {
		return this.addedLinkCount;
	}

	/**
	 * @param addedLinkCount
	 *            the addedLinkCount to set
	 */
	public void setAddedLinkCount(long addedLinkCount) {
		this.addedLinkCount = addedLinkCount;
	}

	/**
	 * @return the updatedDictionaryEntryCount
	 */
	public long getUpdatedDictionaryEntryCount() {
		return this.updatedDictionaryEntryCount;
	}

	/**
	 * @param updatedDictionaryEntryCount
	 *            the updatedDictionaryEntryCount to set
	 */
	public void setUpdatedDictionaryEntryCount(long updatedDictionaryEntryCount) {
		this.updatedDictionaryEntryCount = updatedDictionaryEntryCount;
	}

	/**
	 * @return the updatedDomainCount
	 */
	public long getUpdatedDomainCount() {
		return this.updatedDomainCount;
	}

	/**
	 * @param updatedDomainCount
	 *            the updatedDomainCount to set
	 */
	public void setUpdatedDomainCount(long updatedDomainCount) {
		this.updatedDomainCount = updatedDomainCount;
	}

	/**
	 * @return the updatedLinkCount
	 */
	public long getUpdatedLinkCount() {
		return this.updatedLinkCount;
	}

	/**
	 * @param updatedLinkCount
	 *            the updatedLinkCount to set
	 */
	public void setUpdatedLinkCount(long updatedLinkCount) {
		this.updatedLinkCount = updatedLinkCount;
	}

	/**
	 * @return
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "addedDictionaryEntryCount=" + this.addedDictionaryEntryCount + ", addedDomainCount="
				+ this.addedDomainCount + ", addedLinkCount=" + this.addedLinkCount + ", updatedDictionaryEntryCount="
				+ this.updatedDictionaryEntryCount + ", updatedDomainCount=" + this.updatedDomainCount + ", updatedLinkCount="
				+ this.updatedLinkCount;
	}

}
