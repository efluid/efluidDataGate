package fr.uem.efluid.services.types;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * @author elecomte
 * @since v0.0.6
 * @version 1
 */
public class SimilarPreparedIndexEntry extends PreparedIndexEntry {

	private List<Long> ids = new ArrayList<>();

	private List<String> keyValues = new ArrayList<>();

	/**
	 * 
	 */
	public SimilarPreparedIndexEntry() {
		super();
	}

	/**
	 * @return
	 * @see fr.uem.efluid.services.types.PreparedIndexEntry#getId()
	 */
	@Override
	public Long getId() {
		return null;
	}

	/**
	 * @return
	 * @see fr.uem.efluid.services.types.PreparedIndexEntry#getKeyValue()
	 */
	@Override
	public String getKeyValue() {
		return null;
	}

	/**
	 * @return the ids
	 */
	public List<Long> getIds() {
		return this.ids;
	}

	/**
	 * @param ids
	 *            the ids to set
	 */
	public void setIds(List<Long> ids) {
		this.ids = ids;
	}

	/**
	 * @return the keyValues
	 */
	public List<String> getKeyValues() {
		return this.keyValues;
	}

	/**
	 * @param keyValues
	 *            the keyValues to set
	 */
	public void setKeyValues(List<String> keyValues) {
		this.keyValues = keyValues;
	}

	/**
	 * @return
	 * @see fr.uem.efluid.services.types.PreparedIndexEntry#isDisplayOnly()
	 */
	@Override
	public boolean isDisplayOnly() {
		return true;
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
		comb.setAction(first.getAction());
		comb.setCommitUuid(first.getCommitUuid());
		comb.setDictionaryEntryUuid(first.getDictionaryEntryUuid());
		comb.setHrPayload(first.getHrPayload());
		comb.setPayload(first.getPayload());
		comb.setTimestamp(first.getTimestamp());

		// Then keep identified combined
		diffItems.stream().forEach(d -> {
			comb.getIds().add(d.getId());
			comb.getKeyValues().add(d.getKeyValue());
		});

		return comb;
	}
}
