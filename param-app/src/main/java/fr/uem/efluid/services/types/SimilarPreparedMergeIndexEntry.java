package fr.uem.efluid.services.types;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * <p>
 * For rendering when multiple entries share the same content
 * </p>
 * 
 * @author elecomte
 * @since v0.0.1
 * @version 1
 */
public class SimilarPreparedMergeIndexEntry extends PreparedMergeIndexEntry implements CombinedSimilar<PreparedMergeIndexEntry> {

	private List<Long> ids;

	private List<String> keyValues;

	private List<PreparedIndexEntry> mines;

	private List<PreparedIndexEntry> theirs;

	/**
	 * 
	 */
	public SimilarPreparedMergeIndexEntry() {
		super();
	}

	/**
	 * @return the ids
	 */
	@Override
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
	@Override
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
	 * @return the mines
	 */
	public List<PreparedIndexEntry> getMines() {
		return this.mines;
	}

	/**
	 * @param mines
	 *            the mines to set
	 */
	public void setMines(List<PreparedIndexEntry> mines) {
		this.mines = mines;
	}

	/**
	 * @return the theirs
	 */
	public List<PreparedIndexEntry> getTheirs() {
		return this.theirs;
	}

	/**
	 * @param theirs
	 *            the theirs to set
	 */
	public void setTheirs(List<PreparedIndexEntry> theirs) {
		this.theirs = theirs;
	}

	/**
	 * @return
	 * @see fr.uem.efluid.services.types.CombinedSimilar#split()
	 */
	@Override
	public List<PreparedMergeIndexEntry> split() {

		List<PreparedMergeIndexEntry> splited = new ArrayList<>();

		for (int i = 0; i < this.keyValues.size(); i++) {
			PreparedMergeIndexEntry ie = new PreparedMergeIndexEntry();
			copyContent(this, ie);
			ie.setKeyValue(this.keyValues.get(i));
			ie.setId(this.ids.get(i));
			ie.setNeedAction(this.isNeedAction());
			ie.setMine(this.mines.get(0));
			ie.setTheir(this.theirs.get(0));
			splited.add(ie);
		}

		return splited;
	}

	/**
	 * <p>
	 * Produces the similar when required
	 * </p>
	 * 
	 * @param diffItems
	 * @return
	 */
	public static SimilarPreparedMergeIndexEntry fromSimilar(Collection<? extends PreparedMergeIndexEntry> mergeItems) {

		SimilarPreparedMergeIndexEntry comb = new SimilarPreparedMergeIndexEntry();

		PreparedIndexEntry first = mergeItems.iterator().next();

		// Some properties are from first one
		comb.setAction(first.getAction());
		comb.setCommitUuid(first.getCommitUuid());
		comb.setDictionaryEntryUuid(first.getDictionaryEntryUuid());
		comb.setHrPayload(first.getHrPayload());
		comb.setPayload(first.getPayload());
		comb.setTimestamp(first.getTimestamp());

		// Including key for sorting / ref
		comb.setKeyValue(first.getKeyValue());

		// Then keep identified combined
		mergeItems.stream().forEach(d -> {
			comb.getIds().add(d.getId());
			comb.getKeyValues().add(d.getKeyValue());
			comb.getMines().add(d.getMine());
			comb.getTheirs().add(d.getTheir());
		});

		return comb;
	}
}
