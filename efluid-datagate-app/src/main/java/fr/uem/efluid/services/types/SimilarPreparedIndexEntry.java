package fr.uem.efluid.services.types;

import fr.uem.efluid.model.entities.IndexEntry;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author elecomte
 * @since v0.0.2
 * @version 2
 */
public class SimilarPreparedIndexEntry extends PreparedIndexEntry implements CombinedSimilar<PreparedIndexEntry> {

	private Collection<? extends PreparedIndexEntry> combineds = new ArrayList<>();

	/**
	 * 
	 */
	public SimilarPreparedIndexEntry() {
		super();
	}

	/**
	 * @return
	 * @see fr.uem.efluid.services.types.CombinedSimilar#split()
	 */
	@Override
	public List<PreparedIndexEntry> split() {
		return getCombineds().stream().peek(dest -> {
			dest.setRollbacked(this.isRollbacked());
			dest.setSelected(this.isSelected());
		}).collect(Collectors.toList());
	}

	/**
	 * @return
	 * @see fr.uem.efluid.services.types.CombinedSimilar#getKeyValues()
	 */
	@Override
	public List<String> getKeyValues() {
		return this.combineds.stream().map(PreparedIndexEntry::getKeyValue).collect(Collectors.toList());
	}

	/**
	 * @return
	 * @see fr.uem.efluid.services.types.Rendered#getRealSize()
	 */
	@Override
	public int getRealSize() {
		return this.combineds.size();
	}

	/**
	 * @return
	 * @see fr.uem.efluid.services.types.PreparedIndexEntry#getCombinedKey()
	 */
	@Override
	public String getCombinedKey() {
		return this.combineds.stream().map(PreparedIndexEntry::getKeyValue).collect(Collectors.joining(", "));
	}

	/**
	 * @return the combineds
	 */
	private Collection<? extends PreparedIndexEntry> getCombineds() {
		return this.combineds;
	}

	/**
	 * @param combineds
	 *            the combineds to set
	 */
	private void setCombineds(Collection<? extends PreparedIndexEntry> combineds) {
		this.combineds = combineds;
	}

	/**
	 * <p>
	 * Produces the similar when required
	 * </p>
	 * 
	 * @param diffItems
	 * @return
	 */
	public static SimilarPreparedIndexEntry fromSimilar(Collection<? extends PreparedIndexEntry> diffItems) {

		SimilarPreparedIndexEntry comb = new SimilarPreparedIndexEntry();

		PreparedIndexEntry first = diffItems.iterator().next();

		// Some properties are from first one
		copyFromEntry(comb, first);

		comb.setCombineds(diffItems);

		return comb;
	}

}
