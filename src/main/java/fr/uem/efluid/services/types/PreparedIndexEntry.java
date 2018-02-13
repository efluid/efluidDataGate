package fr.uem.efluid.services.types;

import java.util.UUID;

import fr.uem.efluid.model.DiffLine;
import fr.uem.efluid.model.entities.DictionaryEntry;
import fr.uem.efluid.model.entities.IndexAction;
import fr.uem.efluid.model.entities.IndexEntry;

/**
 * @author elecomte
 * @since v0.0.1
 * @version 1
 */
public class PreparedIndexEntry implements DiffLine {

	private Long id;

	private UUID dictionaryEntryUuid;

	private IndexAction action;

	private String keyValue;

	private String payload;

	private String hrPayload;

	private long timestamp;

	private boolean selected;

	private boolean rollbacked;

	private UUID commitUuid;

	/**
	 * 
	 */
	public PreparedIndexEntry() {
		super();
	}

	/**
	 * @return the id
	 */
	public Long getId() {
		return this.id;
	}

	/**
	 * @param id
	 *            the id to set
	 */
	public void setId(Long id) {
		this.id = id;
	}

	/**
	 * @return the timestamp
	 */
	public long getTimestamp() {
		return this.timestamp;
	}

	/**
	 * @param timestamp
	 *            the timestamp to set
	 */
	public void setTimestamp(long timestamp) {
		this.timestamp = timestamp;
	}

	/**
	 * @return the dictionaryEntryUuid
	 */
	@Override
	public UUID getDictionaryEntryUuid() {
		return this.dictionaryEntryUuid;
	}

	/**
	 * @param dictionaryEntryUuid
	 *            the dictionaryEntryUuid to set
	 */
	public void setDictionaryEntryUuid(UUID dictionaryEntryUuid) {
		this.dictionaryEntryUuid = dictionaryEntryUuid;
	}

	/**
	 * @return the action
	 */
	@Override
	public IndexAction getAction() {
		return this.action;
	}

	/**
	 * @param action
	 *            the action to set
	 */
	public void setAction(IndexAction action) {
		this.action = action;
	}

	/**
	 * @return the keyValue
	 */
	@Override
	public String getKeyValue() {
		return this.keyValue;
	}

	/**
	 * @param keyValue
	 *            the keyValue to set
	 */
	public void setKeyValue(String keyValue) {
		this.keyValue = keyValue;
	}

	/**
	 * @return the payload
	 */
	@Override
	public String getPayload() {
		return this.payload;
	}

	/**
	 * @param payload
	 *            the payload to set
	 */
	public void setPayload(String payload) {
		this.payload = payload;
	}

	/**
	 * @return the hrPayload
	 */
	public String getHrPayload() {
		return this.hrPayload;
	}

	/**
	 * @param hrPayload
	 *            the hrPayload to set
	 */
	public void setHrPayload(String hrPayload) {
		this.hrPayload = hrPayload;
	}

	/**
	 * @return the selected
	 */
	public boolean isSelected() {
		return this.selected;
	}

	/**
	 * @param selected
	 *            the selected to set
	 */
	public void setSelected(boolean selected) {
		this.selected = selected;
	}

	/**
	 * @return the rollbacked
	 */
	public boolean isRollbacked() {
		return this.rollbacked;
	}

	/**
	 * @param rollbacked
	 *            the rollbacked to set
	 */
	public void setRollbacked(boolean rollbacked) {
		this.rollbacked = rollbacked;
	}

	/**
	 * @return the commitUuid
	 */
	public UUID getCommitUuid() {
		return this.commitUuid;
	}

	/**
	 * @param commitUuid
	 *            the commitUuid to set
	 */
	public void setCommitUuid(UUID commitUuid) {
		this.commitUuid = commitUuid;
	}

	/**
	 * Used only when creating a new one as index are immutable
	 * 
	 * @param data
	 * @return
	 */
	public static IndexEntry toEntity(PreparedIndexEntry data) {

		IndexEntry entry = new IndexEntry();

		entry.setAction(data.getAction());
		entry.setDictionaryEntry(new DictionaryEntry(data.getDictionaryEntryUuid()));
		entry.setKeyValue(data.getKeyValue());
		entry.setPayload(data.getPayload());
		entry.setTimestamp(data.getTimestamp()); // Can have one ?

		return entry;
	}

	/**
	 * <p>
	 * For combining process : minimal rendering, with support of hr payload for rendering
	 * </p>
	 * 
	 * @param combined
	 * @param hrPayload
	 * @return
	 */
	public static PreparedIndexEntry fromCombined(DiffLine combined, String hrPayload) {

		PreparedIndexEntry data = new PreparedIndexEntry();

		data.setAction(combined.getAction());
		data.setDictionaryEntryUuid(combined.getDictionaryEntryUuid());
		data.setPayload(combined.getPayload());
		data.setKeyValue(combined.getKeyValue());

		return data;
	}

	/**
	 * Used when reading an index content
	 * 
	 * @param partial
	 * @param dict
	 * @return
	 */
	public static PreparedIndexEntry fromExistingEntity(IndexEntry existing) {

		PreparedIndexEntry data = new PreparedIndexEntry();

		completeFromExistingEntity(data, existing);

		return data;
	}

	/**
	 * Used when reading an index content
	 * 
	 * @param partial
	 * @param dict
	 * @return
	 */
	protected static void completeFromExistingEntity(PreparedIndexEntry data, IndexEntry existing) {

		data.setAction(existing.getAction());

		if (existing.getDictionaryEntry() != null) {
			data.setDictionaryEntryUuid(existing.getDictionaryEntry().getUuid());
		}

		data.setPayload(existing.getPayload());
		data.setId(existing.getId());
		data.setKeyValue(existing.getKeyValue());
		data.setCommitUuid(existing.getCommit() != null ? existing.getCommit().getUuid() : null);
		data.setTimestamp(existing.getTimestamp());
	}
}
