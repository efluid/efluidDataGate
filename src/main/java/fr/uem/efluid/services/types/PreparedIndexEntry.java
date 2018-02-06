package fr.uem.efluid.services.types;

import java.util.UUID;

import fr.uem.efluid.model.entities.DictionaryEntry;
import fr.uem.efluid.model.entities.IndexAction;
import fr.uem.efluid.model.entities.IndexEntry;

/**
 * @author elecomte
 * @since v0.0.1
 * @version 1
 */
public class PreparedIndexEntry {

	private Long id;

	private UUID domainUuid;

	private String domainName;

	private UUID dictionaryEntryUuid;

	private String dictionaryEntryName;

	private IndexAction action;

	private String keyValue;

	private String payload;

	private String hrPayload;

	private long timestamp;

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
	 * @return the domainUuid
	 */
	public UUID getDomainUuid() {
		return this.domainUuid;
	}

	/**
	 * @param domainUuid
	 *            the domainUuid to set
	 */
	public void setDomainUuid(UUID domainUuid) {
		this.domainUuid = domainUuid;
	}

	/**
	 * @return the domainName
	 */
	public String getDomainName() {
		return this.domainName;
	}

	/**
	 * @param domainName
	 *            the domainName to set
	 */
	public void setDomainName(String domainName) {
		this.domainName = domainName;
	}

	/**
	 * @return the dictionaryEntryUuid
	 */
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
	 * @return the dictionaryEntryName
	 */
	public String getDictionaryEntryName() {
		return this.dictionaryEntryName;
	}

	/**
	 * @param dictionaryEntryName
	 *            the dictionaryEntryName to set
	 */
	public void setDictionaryEntryName(String dictionaryEntryName) {
		this.dictionaryEntryName = dictionaryEntryName;
	}

	/**
	 * @return the action
	 */
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

		return entry;
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

		data.setAction(existing.getAction());
		data.setDictionaryEntryName(existing.getDictionaryEntry().getParameterName());
		data.setDictionaryEntryUuid(existing.getDictionaryEntry().getUuid());
		data.setDomainName(existing.getDictionaryEntry().getDomain().getName());
		data.setDomainUuid(existing.getDictionaryEntry().getDomain().getUuid());
		data.setPayload(existing.getPayload());
		data.setId(existing.getId());
		data.setKeyValue(existing.getKeyValue());

		return data;
	}
}
