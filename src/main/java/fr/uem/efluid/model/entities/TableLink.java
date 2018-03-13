package fr.uem.efluid.model.entities;

import java.time.LocalDateTime;
import java.util.UUID;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

import fr.uem.efluid.model.Shared;
import fr.uem.efluid.utils.SharedOutputInputUtils;

/**
 * @author elecomte
 * @since v0.0.1
 * @version 1
 */
@Entity
@Table(name = "link")
public class TableLink implements Shared {

	@Id
	private UUID uuid;

	private String columnFrom;

	@NotNull
	private String tableTo;

	private String columnTo;

	@NotNull
	private LocalDateTime createdTime;

	private LocalDateTime importedTime;

	@ManyToOne(optional = false)
	private DictionaryEntry dictionaryEntry;

	/**
	 * @param uuid
	 */
	public TableLink(UUID uuid) {
		super();
		this.uuid = uuid;
	}

	/**
	 * 
	 */
	public TableLink() {
		super();
	}

	/**
	 * @return the uuid
	 */
	@Override
	public UUID getUuid() {
		return this.uuid;
	}

	/**
	 * @param uuid
	 *            the uuid to set
	 */
	public void setUuid(UUID uuid) {
		this.uuid = uuid;
	}

	/**
	 * @return the columnFrom
	 */
	public String getColumnFrom() {
		return this.columnFrom;
	}

	/**
	 * @param columnFrom
	 *            the columnFrom to set
	 */
	public void setColumnFrom(String columnFrom) {
		this.columnFrom = columnFrom;
	}

	/**
	 * @return the tableTo
	 */
	public String getTableTo() {
		return this.tableTo;
	}

	/**
	 * @param tableTo
	 *            the tableTo to set
	 */
	public void setTableTo(String tableTo) {
		this.tableTo = tableTo;
	}

	/**
	 * @return the columnTo
	 */
	public String getColumnTo() {
		return this.columnTo;
	}

	/**
	 * @param columnTo
	 *            the columnTo to set
	 */
	public void setColumnTo(String columnTo) {
		this.columnTo = columnTo;
	}

	/**
	 * @return the createdTime
	 */
	public LocalDateTime getCreatedTime() {
		return this.createdTime;
	}

	/**
	 * @param createdTime
	 *            the createdTime to set
	 */
	public void setCreatedTime(LocalDateTime createdTime) {
		this.createdTime = createdTime;
	}

	/**
	 * @return the importedTime
	 */
	@Override
	public LocalDateTime getImportedTime() {
		return this.importedTime;
	}

	/**
	 * @param importedTime
	 *            the importedTime to set
	 */
	public void setImportedTime(LocalDateTime importedTime) {
		this.importedTime = importedTime;
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
	 * @return
	 * @see fr.uem.efluid.model.Shared#serialize()
	 */
	@Override
	public String serialize() {

		return SharedOutputInputUtils.newJson()
				.with("uid", getUuid())
				.with("cre", getCreatedTime())
				.with("cfr", getColumnFrom())
				.with("cto", getColumnTo())
				.with("tto", getTableTo())
				.with("dic", getDictionaryEntry().getUuid())
				.toString();
	}

	/**
	 * @param raw
	 * @see fr.uem.efluid.model.Shared#deserialize(java.lang.String)
	 */
	@Override
	public void deserialize(String raw) {

		SharedOutputInputUtils.fromJson(raw)
				.applyUUID("uid", v -> setUuid(v))
				.applyLdt("cre", v -> setCreatedTime(v))
				.applyString("cfr", v -> setColumnFrom(v))
				.applyString("cto", v -> setColumnTo(v))
				.applyString("tto", v -> setTableTo(v))
				.applyUUID("dic", v -> setDictionaryEntry(new DictionaryEntry(v)));
	}

	/**
	 * @return
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((this.getColumnFrom ()== null) ? 0 : this.getColumnFrom().hashCode());
		result = prime * result + ((this.getDictionaryEntry() == null) ? 0 : this.getDictionaryEntry().hashCode());
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
		if (!(obj instanceof TableLink))
			return false;
		TableLink other = (TableLink) obj;
		if (this.getColumnFrom() == null) {
			if (other.getColumnFrom() != null)
				return false;
		} else if (!this.getColumnFrom().equals(other.getColumnFrom()))
			return false;
		if (this.getDictionaryEntry() == null) {
			if (other.getDictionaryEntry() != null)
				return false;
		} else if (!this.getDictionaryEntry().equals(other.getDictionaryEntry()))
			return false;
		return true;
	}

}
