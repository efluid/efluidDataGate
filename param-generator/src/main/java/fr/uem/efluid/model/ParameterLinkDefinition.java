package fr.uem.efluid.model;

import java.time.LocalDateTime;
import java.util.UUID;

import fr.uem.efluid.ParameterLink;
import fr.uem.efluid.model.shared.ExportAwareTableLink;

/**
 * @author elecomte
 * @since v0.0.1
 * @version 1
 */
@SpecifiedWith(ParameterLink.class)
public class ParameterLinkDefinition extends ExportAwareTableLink<ParameterTableDefinition> {

	private UUID uuid;

	private String columnFrom;

	private String tableTo;

	private String columnTo;

	private LocalDateTime createdTime;

	private ParameterTableDefinition dictionaryEntry;

	/**
	 * @param uuid
	 */
	public ParameterLinkDefinition(UUID uuid) {
		super();
		this.uuid = uuid;
	}

	/**
	 * 
	 */
	public ParameterLinkDefinition() {
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
