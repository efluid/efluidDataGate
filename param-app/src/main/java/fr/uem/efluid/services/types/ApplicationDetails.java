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
	private long indexCount;
	private String indexSize;
	private long commitsCount;
	private long dictionaryCount;
	private long lobsCount;
	private long projectsCount;

	private long domainsCountForProject;

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
	 * @return the domainsCountForProject
	 */
	public long getDomainsCountForProject() {
		return this.domainsCountForProject;
	}

	/**
	 * @param domainsCountForProject
	 *            the domainsCountForProject to set
	 */
	public void setDomainsCountForProject(long domainsCountForProject) {
		this.domainsCountForProject = domainsCountForProject;
	}

	/**
	 * @param dbUrl
	 */
	public void setDbUrl(String dbUrl) {
		this.dbUrl = dbUrl;
	}

	/**
	 * @return the indexCount
	 */
	public long getIndexCount() {
		return this.indexCount;
	}

	/**
	 * @param indexCount
	 *            the indexCount to set
	 */
	public void setIndexCount(long indexCount) {
		this.indexCount = indexCount;
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

	/**
	 * @return the projectsCount
	 */
	public long getProjectsCount() {
		return this.projectsCount;
	}

	/**
	 * @param projectsCount
	 *            the projectsCount to set
	 */
	public void setProjectsCount(long projectsCount) {
		this.projectsCount = projectsCount;
	}
}