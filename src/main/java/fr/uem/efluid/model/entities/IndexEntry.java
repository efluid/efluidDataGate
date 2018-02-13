package fr.uem.efluid.model.entities;

import java.util.UUID;

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

import fr.uem.efluid.model.DiffLine;
import fr.uem.efluid.utils.SharedOutputInputUtils;

/**
 * <p>
 * Define one line in the index/backlog. Associated to one dictionaryEntry (defining the
 * managed table), with one type of action, and indentified for one commit. Payload
 * content describe "one full line of managed table", in one value line. Column names and
 * type protection are "natural", values are always BASE64 encoded.
 * </p>
 * 
 * @author elecomte
 * @since v0.0.1
 * @version 1
 */
@Entity
@Table(name = "index")
public class IndexEntry implements DiffLine {

	@Id
	@GeneratedValue
	private Long id;

	@ManyToOne(optional = false, fetch = FetchType.LAZY)
	private DictionaryEntry dictionaryEntry;

	@NotNull
	@Enumerated(EnumType.STRING)
	private IndexAction action;

	@ManyToOne(optional = false)
	private Commit commit;

	private String payload;

	private String keyValue;

	private long timestamp;

	/**
	 * 
	 */
	public IndexEntry() {
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
	 * @return the dictionaryEntry
	 */
	public DictionaryEntry getDictionaryEntry() {
		return this.dictionaryEntry;
	}

	/**
	 * @param dictionaryEntry
	 *            the dictionaryEntry to set
	 */
	public void setDictionaryEntry(DictionaryEntry dictionaryEntry) {
		this.dictionaryEntry = dictionaryEntry;
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
	 * @return the commit
	 */
	public Commit getCommit() {
		return this.commit;
	}

	/**
	 * @param commit
	 *            the commit to set
	 */
	public void setCommit(Commit commit) {
		this.commit = commit;
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
	 * @return
	 * @see fr.uem.efluid.model.DiffLine#getDictionaryUuid()
	 */
	@Override
	public UUID getDictionaryEntryUuid() {
		return this.dictionaryEntry.getUuid();
	}

	/**
	 * For commit serialize
	 * 
	 * @return
	 */
	String serialize() {

		return SharedOutputInputUtils.newJson()
				.with("dic", getDictionaryEntryUuid())
				.with("act", getAction())
				.with("key", getKeyValue())
				.with("pay", getPayload())
				.with("tim", Long.valueOf(getTimestamp()))
				.toString();
	}

	/**
	 * For commit deserialize
	 * 
	 * @param raw
	 */
	void deserialize(String raw) {
		SharedOutputInputUtils.fromJson(raw)
				.applyUUID("dic", u -> setDictionaryEntry(new DictionaryEntry(u)))
				.applyString("act", a -> setAction(IndexAction.valueOf(a)))
				.applyString("key", k -> setKeyValue(k))
				.applyString("pay", p -> setPayload(p))
				.applyString("tim", t -> setTimestamp(Long.parseLong(t)));
	}

	/**
	 * @return
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((this.dictionaryEntry == null) ? 0 : this.dictionaryEntry.hashCode());
		result = prime * result + ((this.id == null) ? 0 : this.id.hashCode());
		result = prime * result + ((this.keyValue == null) ? 0 : this.keyValue.hashCode());
		return result;
	}

	/**
	 * @param obj
	 * @return
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		IndexEntry other = (IndexEntry) obj;
		if (this.dictionaryEntry == null) {
			if (other.dictionaryEntry != null)
				return false;
		} else if (!this.dictionaryEntry.equals(other.dictionaryEntry))
			return false;
		if (this.id == null) {
			if (other.id != null)
				return false;
		} else if (!this.id.equals(other.id))
			return false;
		if (this.keyValue == null) {
			if (other.keyValue != null)
				return false;
		} else if (!this.keyValue.equals(other.keyValue))
			return false;
		return true;
	}

	/**
	 * @return
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "IdxEntry [<" + this.id + ">, dict:" + getDictionaryEntryUuid() + ", time:" + this.timestamp + ", chg:" + this.action + "@"
				+ this.keyValue + "|" + this.payload + "]";
	}

}
