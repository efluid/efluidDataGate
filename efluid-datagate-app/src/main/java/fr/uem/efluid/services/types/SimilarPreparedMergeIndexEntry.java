package fr.uem.efluid.services.types;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * <p>
 * For rendering when multiple entries share the same content
 * </p>
 * 
 * @author elecomte
 * @since v0.0.2
 * @version 2
 */
public class SimilarPreparedMergeIndexEntry extends PreparedMergeIndexEntry implements CombinedSimilar<PreparedMergeIndexEntry> {

	private Collection<? extends PreparedMergeIndexEntry> combineds = new ArrayList<>();

	/**
	 * 
	 */
	public SimilarPreparedMergeIndexEntry() {
		super();
	}

	/**
	 * @return
	 * @see fr.uem.efluid.services.types.CombinedSimilar#split()
	 */
	@Override
	public List<PreparedMergeIndexEntry> split() {
		return getCombineds().stream().peek(dest -> {
			dest.setRollbacked(this.isRollbacked());
			dest.setSelected(this.isSelected());
			dest.setNeedAction(this.isNeedAction());
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
	private Collection<? extends PreparedMergeIndexEntry> getCombineds() {
		return this.combineds;
	}

	/**
	 * @param combineds
	 *            the combineds to set
	 */
	private void setCombineds(Collection<? extends PreparedMergeIndexEntry> combineds) {
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
	public static SimilarPreparedMergeIndexEntry fromSimilar(Collection<? extends PreparedMergeIndexEntry> diffItems) {

		SimilarPreparedMergeIndexEntry comb = new SimilarPreparedMergeIndexEntry();

		PreparedIndexEntry first = diffItems.iterator().next();

		// Some properties are from first one
		comb.setAction(first.getAction());
		comb.setCommitUuid(first.getCommitUuid());
		comb.setDictionaryEntryUuid(first.getDictionaryEntryUuid());
		comb.setHrPayload(first.getHrPayload());
		comb.setPayload(first.getPayload());
		comb.setTimestamp(first.getTimestamp());

		// Including key for sorting / ref
		comb.setKeyValue(first.getKeyValue());

		comb.setCombineds(diffItems);

		return comb;
	}

}
