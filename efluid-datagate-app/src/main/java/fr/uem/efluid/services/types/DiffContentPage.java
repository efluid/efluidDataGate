package fr.uem.efluid.services.types;

import java.util.List;

/**
 * <p>
 * For paginated access on Diff display content. Get one page of diff for one dictionary
 * table
 * </p>
 *
 * @author elecomte
 * @version 2
 * @since v0.0.8
 */
public class DiffContentPage {

    private final int pageIndex;

    private final int pageCount;

    private final long totalCount;

    private final List<? extends PreparedIndexEntry> page;

    /**
     * Init from non paginated content, selecting only the required content to render
     *
     * @param pageIndex          selected page to render (first is 0)
     * @param diffDisplayContent content to paginate
     * @param pageSize           lines in one page
     */
    public DiffContentPage(int pageIndex, List<? extends PreparedIndexEntry> diffDisplayContent, int pageSize) {
        super();

        int pageCount = (int) Math.round(Math.ceil((double) diffDisplayContent.size() / pageSize));

        List<? extends PreparedIndexEntry> pageContent;

        // No pagination needed
        if (pageCount == 1) {
            this.page = diffDisplayContent;
        }

        // Paginated
        else if (pageIndex < pageCount - 1) {
            this.page = diffDisplayContent.subList(pageIndex * pageSize,
                    (pageIndex + 1) * pageSize);
        }

        // Special paginated case : last page
        else {
            this.page = diffDisplayContent.subList(pageIndex * pageSize, diffDisplayContent.size());
        }

        this.pageIndex = pageIndex;
        this.pageCount = pageCount;
        this.totalCount = diffDisplayContent.size();
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

}
