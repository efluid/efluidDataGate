package fr.uem.efluid.services.types;

/**
 * <p>
 * Basic container of running application details, displayed in home page
 * </p>
 * 
 * @author elecomte
 * @since v0.0.1
 * @version 1
 */
public class ApplicationDetails {

	private String dbUrl;
	private long domainsCount;
	private String indexSize;
	private long commitsCount;
	private long dictionaryCount;
	private long lobsCount;

	/**
	 * 
	 */
	public ApplicationDetails() {
		super();
	}

	/**
	 * @return
	 */
	public String getDbUrl() {
		return this.dbUrl;
	}

	/**
	 * @param dbUrl
	 */
	public void setDbUrl(String dbUrl) {
		this.dbUrl = dbUrl;
	}

	/**
	 * @return the dictionaryCount
	 */
	public long getDictionaryCount() {
		return this.dictionaryCount;
	}

	/**
	 * @param dictionaryCount
	 *            the dictionaryCount to set
	 */
	public void setDictionaryCount(long dictionaryCount) {
		this.dictionaryCount = dictionaryCount;
	}

	/**
	 * @return the indexSize
	 */
	public String getIndexSize() {
		return this.indexSize;
	}

	/**
	 * @param indexSize
	 *            the indexSize to set
	 */
	public void setIndexSize(String indexSize) {
		this.indexSize = indexSize;
	}

	/**
	 * @return
	 */
	public long getDomainsCount() {
		return this.domainsCount;
	}

	/**
	 * @param domainsCount
	 */
	public void setDomainsCount(long domainsCount) {
		this.domainsCount = domainsCount;
	}

	/**
	 * @return
	 */
	public long getCommitsCount() {
		return this.commitsCount;
	}

	/**
	 * @param commitsCount
	 */
	public void setCommitsCount(long commitsCount) {
		this.commitsCount = commitsCount;
	}

	/**
	 * @return the lobsCount
	 */
	public long getLobsCount() {
		return this.lobsCount;
	}

	/**
	 * @param lobsCount
	 *            the lobsCount to set
	 */
	public void setLobsCount(long lobsCount) {
		this.lobsCount = lobsCount;
	}
}