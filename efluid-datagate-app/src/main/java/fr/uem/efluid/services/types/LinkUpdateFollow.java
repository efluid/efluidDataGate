package fr.uem.efluid.services.types;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

import fr.uem.efluid.model.entities.TableLink;

/**
 * <p>
 * Intermediate type for easy management of composite keys in TableLink update. We want to
 * identify the columns which are used as "from" in an associated link. So we need to :
 * <ul>
 * <li>Get all the from columns from the tablelink</li>
 * <li>Map the tableLink to column name for easy access during the data update</li>
 * <li>Get the corresponding index in link for the "from" column to get the corresponding
 * "to"</li>
 * </ul>
 * </p>
 * <p>
 * All this complexity is related to composite key support
 * </p>
 * <p>
 * The same item is supposed to be also used for link "to" columns
 * </p>
 * 
 * @author elecomte
 * @since v0.0.8
 * @version 1
 */
public class LinkUpdateFollow {

	private final AtomicInteger index;
	private final TableLink link;
	private final String column;

	/**
	 * <p>
	 * Should build only with <code>flatMapFromColumn</code>
	 * </p>
	 * 
	 * @param link
	 * @param column
	 */
	private LinkUpdateFollow(AtomicInteger index, TableLink link, String column) {
		this.index = index;
		this.link = link;
		this.column = column;
	}

	/**
	 * <p>
	 * For flat map + follow item init from selectionned "from" or "to" column names
	 * </p>
	 * 
	 * @param link
	 *            current TableLink
	 * @param col
	 *            selected "to" or "from" column name stream
	 * @return
	 */
	public static Stream<LinkUpdateFollow> flatMapFromColumn(TableLink link, Stream<String> col) {
		AtomicInteger commonIndex = new AtomicInteger(0);
		return col.map(c -> new LinkUpdateFollow(commonIndex, link, c));
	}

	/**
	 * @return the index
	 */
	public int getIndexAndIncr() {
		return this.index.getAndIncrement();
	}

	/**
	 * @return the link
	 */
	public TableLink getLink() {
		return this.link;
	}

	/**
	 * @return the column
	 */
	public String getColumn() {
		return this.column;
	}
}