package fr.uem.efluid.model;

import java.time.LocalDateTime;
import java.util.UUID;

import fr.uem.efluid.ParameterMapping;
import fr.uem.efluid.model.shared.ExportAwareTableMapping;

/**
 * @author elecomte
 * @since v0.0.1
 * @version 1
 */
@SpecifiedWith(ParameterMapping.class)
public class ParameterMappingDefinition extends ExportAwareTableMapping<ParameterTableDefinition> {

	private UUID uuid;

	private String columnFrom;

	private String tableTo;

	private String columnTo;

	private LocalDateTime createdTime;

	private LocalDateTime updatedTime;

	private String mapTable;

	private String mapTableColumnTo;

	private String mapTableColumnFrom;

	private ParameterTableDefinition dictionaryEntry;

	/**
	 * @param uuid
	 */
	public ParameterMappingDefinition(UUID uuid) {
		super();
		this.uuid = uuid;
	}

	/**
	 * 
	 */
	public ParameterMappingDefinition() {
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
		return null;
	}

	/**
	 * @return the dictionaryEntry
	 */
	@Override
	public ParameterTableDefinition getDictionaryEntry() {
		return this.dictionaryEntry;
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
	 * @param dictionaryEntry
	 *            the dictionaryEntry to set
	 */
	public void setDictionaryEntry(ParameterTableDefinition dictionaryEntry) {
		this.dictionaryEntry = dictionaryEntry;
	}

	/**
	 * @param raw
	 * @see fr.uem.efluid.model.Shared#deserialize(java.lang.String)
	 */
	@Override
	public void deserialize(String raw) {
		// Not implemented
	}

}
