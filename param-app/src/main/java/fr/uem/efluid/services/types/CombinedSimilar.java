package fr.uem.efluid.services.types;

import java.util.List;

/**
 * @author elecomte
 * @since v0.0.7
 * @version 1
 */
public interface CombinedSimilar<T extends PreparedIndexEntry> extends Rendered {

	/**
	 * @return
	 * @see fr.uem.efluid.services.types.Rendered#isDisplayOnly()
	 */
	@Override
	default boolean isDisplayOnly() {
		return true;
	}

	/**
	 * @return the keyValues for the combined items
	 */
	List<String> getKeyValues();

	/**
	 * @return a copy of source for similar process : "split" current item to get
	 *         corresponding "real" diff entries
	 */
	List<T> split();
}
