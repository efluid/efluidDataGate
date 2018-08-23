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

	@NotNull
	private String name;

	@NotNull
	private String columnTo;

	@NotNull
	private String mapTable;

	@NotNull
	private String mapTableColumnTo;

	@NotNull
	private String mapTableColumnFrom;

	private String whereClause;

	private String mapTableExt1ColumnTo;

	private String mapTableExt2ColumnTo;

	private String mapTableExt3ColumnTo;

	private String mapTableExt4ColumnTo;

	private String mapTableExt1ColumnFrom;

	private String mapTableExt2ColumnFrom;

	private String mapTableExt3ColumnFrom;

	private String mapTableExt4ColumnFrom;

	private String ext1ColumnTo;

	private String ext2ColumnTo;

	private String ext3ColumnTo;

	private String ext4ColumnTo;

	private String ext1ColumnFrom;

	private String ext2ColumnFrom;

	private String ext3ColumnFrom;

	private String ext4ColumnFrom;

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
	 * @return the whereClause
	 */
	@Override
	public String getWhereClause() {
		return this.whereClause;
	}

	/**
	 * @param whereClause
	 *            the whereClause to set
	 */
	public void setWhereClause(String whereClause) {
		this.whereClause = whereClause;
	}

	/**
	 * @return the mapTableExt1ColumnTo
	 */
	@Override
	public String getMapTableExt1ColumnTo() {
		return this.mapTableExt1ColumnTo;
	}

	/**
	 * @param mapTableExt1ColumnTo
	 *            the mapTableExt1ColumnTo to set
	 */
	public void setMapTableExt1ColumnTo(String mapTableExt1ColumnTo) {
		this.mapTableExt1ColumnTo = mapTableExt1ColumnTo;
	}

	/**
	 * @return the mapTableExt2ColumnTo
	 */
	@Override
	public String getMapTableExt2ColumnTo() {
		return this.mapTableExt2ColumnTo;
	}

	/**
	 * @param mapTableExt2ColumnTo
	 *            the mapTableExt2ColumnTo to set
	 */
	public void setMapTableExt2ColumnTo(String mapTableExt2ColumnTo) {
		this.mapTableExt2ColumnTo = mapTableExt2ColumnTo;
	}

	/**
	 * @return the mapTableExt3ColumnTo
	 */
	@Override
	public String getMapTableExt3ColumnTo() {
		return this.mapTableExt3ColumnTo;
	}

	/**
	 * @param mapTableExt3ColumnTo
	 *            the mapTableExt3ColumnTo to set
	 */
	public void setMapTableExt3ColumnTo(String mapTableExt3ColumnTo) {
		this.mapTableExt3ColumnTo = mapTableExt3ColumnTo;
	}

	/**
	 * @return the mapTableExt4ColumnTo
	 */
	@Override
	public String getMapTableExt4ColumnTo() {
		return this.mapTableExt4ColumnTo;
	}

	/**
	 * @param mapTableExt4ColumnTo
	 *            the mapTableExt4ColumnTo to set
	 */
	public void setMapTableExt4ColumnTo(String mapTableExt4ColumnTo) {
		this.mapTableExt4ColumnTo = mapTableExt4ColumnTo;
	}

	/**
	 * @return the mapTableExt1ColumnFrom
	 */
	@Override
	public String getMapTableExt1ColumnFrom() {
		return this.mapTableExt1ColumnFrom;
	}

	/**
	 * @param mapTableExt1ColumnFrom
	 *            the mapTableExt1ColumnFrom to set
	 */
	public void setMapTableExt1ColumnFrom(String mapTableExt1ColumnFrom) {
		this.mapTableExt1ColumnFrom = mapTableExt1ColumnFrom;
	}

	/**
	 * @return the mapTableExt2ColumnFrom
	 */
	@Override
	public String getMapTableExt2ColumnFrom() {
		return this.mapTableExt2ColumnFrom;
	}

	/**
	 * @param mapTableExt2ColumnFrom
	 *            the mapTableExt2ColumnFrom to set
	 */
	public void setMapTableExt2ColumnFrom(String mapTableExt2ColumnFrom) {
		this.mapTableExt2ColumnFrom = mapTableExt2ColumnFrom;
	}

	/**
	 * @return the mapTableExt3ColumnFrom
	 */
	@Override
	public String getMapTableExt3ColumnFrom() {
		return this.mapTableExt3ColumnFrom;
	}

	/**
	 * @param mapTableExt3ColumnFrom
	 *            the mapTableExt3ColumnFrom to set
	 */
	public void setMapTableExt3ColumnFrom(String mapTableExt3ColumnFrom) {
		this.mapTableExt3ColumnFrom = mapTableExt3ColumnFrom;
	}

	/**
	 * @return the mapTableExt4ColumnFrom
	 */
	@Override
	public String getMapTableExt4ColumnFrom() {
		return this.mapTableExt4ColumnFrom;
	}

	/**
	 * @param mapTableExt4ColumnFrom
	 *            the mapTableExt4ColumnFrom to set
	 */
	public void setMapTableExt4ColumnFrom(String mapTableExt4ColumnFrom) {
		this.mapTableExt4ColumnFrom = mapTableExt4ColumnFrom;
	}

	/**
	 * @return the ext1ColumnTo
	 */
	@Override
	public String getExt1ColumnTo() {
		return this.ext1ColumnTo;
	}

	/**
	 * @param ext1ColumnTo
	 *            the ext1ColumnTo to set
	 */
	public void setExt1ColumnTo(String ext1ColumnTo) {
		this.ext1ColumnTo = ext1ColumnTo;
	}

	/**
	 * @return the ext2ColumnTo
	 */
	@Override
	public String getExt2ColumnTo() {
		return this.ext2ColumnTo;
	}

	/**
	 * @param ext2ColumnTo
	 *            the ext2ColumnTo to set
	 */
	public void setExt2ColumnTo(String ext2ColumnTo) {
		this.ext2ColumnTo = ext2ColumnTo;
	}

	/**
	 * @return the ext3ColumnTo
	 */
	@Override
	public String getExt3ColumnTo() {
		return this.ext3ColumnTo;
	}

	/**
	 * @param ext3ColumnTo
	 *            the ext3ColumnTo to set
	 */
	public void setExt3ColumnTo(String ext3ColumnTo) {
		this.ext3ColumnTo = ext3ColumnTo;
	}

	/**
	 * @return the ext4ColumnTo
	 */
	@Override
	public String getExt4ColumnTo() {
		return this.ext4ColumnTo;
	}

	/**
	 * @param ext4ColumnTo
	 *            the ext4ColumnTo to set
	 */
	public void setExt4ColumnTo(String ext4ColumnTo) {
		this.ext4ColumnTo = ext4ColumnTo;
	}

	/**
	 * @return the ext1ColumnFrom
	 */
	@Override
	public String getExt1ColumnFrom() {
		return this.ext1ColumnFrom;
	}

	/**
	 * @param ext1ColumnFrom
	 *            the ext1ColumnFrom to set
	 */
	public void setExt1ColumnFrom(String ext1ColumnFrom) {
		this.ext1ColumnFrom = ext1ColumnFrom;
	}

	/**
	 * @return the ext2ColumnFrom
	 */
	@Override
	public String getExt2ColumnFrom() {
		return this.ext2ColumnFrom;
	}

	/**
	 * @param ext2ColumnFrom
	 *            the ext2ColumnFrom to set
	 */
	public void setExt2ColumnFrom(String ext2ColumnFrom) {
		this.ext2ColumnFrom = ext2ColumnFrom;
	}

	/**
	 * @return the ext3ColumnFrom
	 */
	@Override
	public String getExt3ColumnFrom() {
		return this.ext3ColumnFrom;
	}

	/**
	 * @param ext3ColumnFrom
	 *            the ext3ColumnFrom to set
	 */
	public void setExt3ColumnFrom(String ext3ColumnFrom) {
		this.ext3ColumnFrom = ext3ColumnFrom;
	}

	/**
	 * @return the ext4ColumnFrom
	 */
	@Override
	public String getExt4ColumnFrom() {
		return this.ext4ColumnFrom;
	}

	/**
	 * @param ext4ColumnFrom
	 *            the ext4ColumnFrom to set
	 */
	public void setExt4ColumnFrom(String ext4ColumnFrom) {
		this.ext4ColumnFrom = ext4ColumnFrom;
	}

	/**
	 * @return the name
	 */
	@Override
	public String getName() {
		return this.name;
	}

	/**
	 * @param name
	 *            the name to set
	 */
	public void setName(String name) {
		this.name = name;
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
				.applyString("nam", v -> setName(v))
				.applyString("cfr", v -> setColumnFrom(v))
				.applyString("cto", v -> setColumnTo(v))
				.applyString("tto", v -> setTableTo(v))
				.applyString("mta", v -> setMapTable(v))
				.applyString("mto", v -> setMapTableColumnTo(v))
				.applyString("mfr", v -> setMapTableColumnFrom(v))
				.applyString("cf1", v -> setExt1ColumnFrom(v))
				.applyString("cf2", v -> setExt2ColumnFrom(v))
				.applyString("cf3", v -> setExt3ColumnFrom(v))
				.applyString("cf4", v -> setExt4ColumnFrom(v))
				.applyString("ct1", v -> setExt1ColumnTo(v))
				.applyString("ct2", v -> setExt2ColumnTo(v))
				.applyString("ct3", v -> setExt3ColumnTo(v))
				.applyString("ct4", v -> setExt4ColumnTo(v))
				.applyString("mf1", v -> setMapTableExt1ColumnFrom(v))
				.applyString("mf2", v -> setMapTableExt2ColumnFrom(v))
				.applyString("mf3", v -> setMapTableExt3ColumnFrom(v))
				.applyString("mf4", v -> setMapTableExt4ColumnFrom(v))
				.applyString("mt1", v -> setMapTableExt1ColumnTo(v))
				.applyString("mt2", v -> setMapTableExt2ColumnTo(v))
				.applyString("mt3", v -> setMapTableExt3ColumnTo(v))
				.applyString("mt4", v -> setMapTableExt4ColumnTo(v))
				.applyString("whe", v -> setWhereClause(v))
				.applyUUID("dic", v -> setDictionaryEntry(new DictionaryEntry(v)));
	}
}
