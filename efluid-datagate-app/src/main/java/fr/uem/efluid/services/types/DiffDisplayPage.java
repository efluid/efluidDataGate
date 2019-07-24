package fr.uem.efluid.services.types;

import java.util.List;

/**
 * <p>
 * For paginated access on Diff display content. Get one page of diff for one dictionary
 * table
 * </p>
 * 
 * @author elecomte
 * @since v0.0.8
 * @version 1
 */
public class DiffDisplayPage {

	private final int pageIndex;

	private final int pageCount;

	private final long totalCount;

	private final List<? extends PreparedIndexEntry> page;

	private final String search;

	/**
	 * @param pageIndex
	 * @param pageCount
	 * @param totalCount
	 * @param page
	 * @param search
	 */
	public DiffDisplayPage(int pageIndex, int pageCount, long totalCount, List<? extends PreparedIndexEntry> page, String search) {
		super();
		this.pageIndex = pageIndex;
		this.pageCount = pageCount;
		this.totalCount = totalCount;
		this.page = page;
		this.search = search;
	}

	/**
	 * @return the pageIndex
	 */
	public int getPageIndex() {
		return this.pageIndex;
	}

	/**
	 * @return the pageCount
	 */
	public int getPageCount() {
		return this.pageCount;
	}

	/**
	 * @return the totalCount
	 */
	public long getTotalCount() {
		return this.totalCount;
	}

	/**
	 * @return the page
	 */
	public List<? extends PreparedIndexEntry> getPage() {
		return this.page;
	}

	/**
	 * @return the search
	 */
	public String getSearch() {
		return this.search;
	}

}
