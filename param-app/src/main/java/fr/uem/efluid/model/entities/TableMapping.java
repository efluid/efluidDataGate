package fr.uem.efluid.model.entities;

import java.time.LocalDateTime;
import java.util.UUID;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

import fr.uem.efluid.model.shared.ExportAwareTableMapping;
import fr.uem.efluid.utils.SharedOutputInputUtils;

/**
 * @author elecomte
 * @since v0.0.1
 * @version 1
 */
@Entity
@Table(name = "mappings")
public class TableMapping extends ExportAwareTableMapping<DictionaryEntry> {

	@Id
	private UUID uuid;

	private String columnFrom;

	@NotNull
	private String tableTo;

	private String columnTo;

	private String mapTable;

	private String mapTableColumnTo;

	private String mapTableColumnFrom;

	@NotNull
	private LocalDateTime createdTime;

	@NotNull
	private LocalDateTime updatedTime;

	private LocalDateTime importedTime;

	@ManyToOne(optional = false)
	private DictionaryEntry dictionaryEntry;

	/**
	 * @param uuid
	 */
	public TableMapping(UUID uuid) {
		super();
		this.uuid = uuid;
	}

	/**
	 * 
	 */
	public TableMapping() {
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
	@Override
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
	@Override
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
	@Override
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
	@Override
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
	 * @return the updatedTime
	 */
	@Override
	public LocalDateTime getUpdatedTime() {
		return this.updatedTime;
	}

	/**
	 * @param updatedTime
	 *            the updatedTime to set
	 */
	public void setUpdatedTime(LocalDateTime updatedTime) {
		this.updatedTime = updatedTime;
	}

	/**
	 * @return the dictionaryEntry
	 */
	@Override
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
	 * @return the mapTable
	 */
	@Override
	public String getMapTable() {
		return this.mapTable;
	}

	/**
	 * @param mapTable
	 *            the mapTable to set
	 */
	public void setMapTable(String mapTable) {
		this.mapTable = mapTable;
	}

	/**
	 * @return the mapTableColumnTo
	 */
	@Override
	public String getMapTableColumnTo() {
		return this.mapTableColumnTo;
	}

	/**
	 * @param mapTableColumnTo
	 *            the mapTableColumnTo to set
	 */
	public void setMapTableColumnTo(String mapTableColumnTo) {
		this.mapTableColumnTo = mapTableColumnTo;
	}

	/**
	 * @return the mapTableColumnFrom
	 */
	@Override
	public String getMapTableColumnFrom() {
		return this.mapTableColumnFrom;
	}

	/**
	 * @param mapTableColumnFrom
	 *            the mapTableColumnFrom to set
	 */
	public void setMapTableColumnFrom(String mapTableColumnFrom) {
		this.mapTableColumnFrom = mapTableColumnFrom;
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
				.applyLdt("upd", v -> setUpdatedTime(v))
				.applyString("cfr", v -> setColumnFrom(v))
				.applyString("cto", v -> setColumnTo(v))
				.applyString("tto", v -> setTableTo(v))
				.applyString("mta", v -> setMapTable(v))
				.applyString("mto", v -> setMapTableColumnTo(v))
				.applyString("mfr", v -> setMapTableColumnFrom(v))
				.applyUUID("dic", v -> setDictionaryEntry(new DictionaryEntry(v)));
	}
}
