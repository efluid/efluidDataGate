package fr.uem.efluid.services.types;

import java.util.List;
import java.util.stream.Collectors;

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
	 * @return
	 * @see fr.uem.efluid.services.types.Rendered#getRealSize()
	 */
	@Override
	default int getRealSize() {
		return getKeyValues().size();
	}

	/**
	 * @return the ids
	 */
	List<Long> getIds();

	/**
	 * @return the keyValues
	 */
	List<String> getKeyValues();

	/**
	 * @return
	 * @see fr.uem.efluid.services.types.Rendered#getCombinedKey()
	 */
	@Override
	default String getCombinedKey() {
		return getKeyValues().stream().collect(Collectors.joining(","));
	}

	/**
	 * @return a copy of source for similar process : "split" current item to get
	 *         corresponding "real" diff entries
	 */
	List<T> split();
}
