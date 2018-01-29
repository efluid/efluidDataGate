package fr.uem.efluid.services.types;

import java.util.UUID;

import fr.uem.efluid.model.entities.IndexAction;

/**
 * @author elecomte
 * @since v0.0.1
 * @version 1
 */
public class IndexEntrySummary {

	private final Long entryId;
	private final String parameterKey;
	private final UUID domainUuid;
	private final String dictionaryEntryName;
	private final String dictionaryEntryTable;
	private final IndexAction action;
	private final String payloadFrom;
	private final String payloadTo;

	/**
	 * @param entryId
	 * @param parameterKey
	 * @param domainUuid
	 * @param dictionaryEntryName
	 * @param dictionaryEntryTable
	 * @param action
	 * @param payloadFrom
	 * @param payloadTo
	 */
	public IndexEntrySummary(Long entryId, String parameterKey, UUID domainUuid, String dictionaryEntryName, String dictionaryEntryTable,
			IndexAction action, String payloadFrom, String payloadTo) {
		super();
		this.entryId = entryId;
		this.parameterKey = parameterKey;
		this.domainUuid = domainUuid;
		this.dictionaryEntryName = dictionaryEntryName;
		this.dictionaryEntryTable = dictionaryEntryTable;
		this.action = action;
		this.payloadFrom = payloadFrom;
		this.payloadTo = payloadTo;
	}

	/**
	 * @return the entryId
	 */
	public Long getEntryId() {
		return this.entryId;
	}

	/**
	 * @return the parameterKey
	 */
	public String getParameterKey() {
		return this.parameterKey;
	}

	/**
	 * @return the domainUuid
	 */
	public UUID getDomainUuid() {
		return this.domainUuid;
	}

	/**
	 * @return the dictionaryEntryName
	 */
	public String getDictionaryEntryName() {
		return this.dictionaryEntryName;
	}

	/**
	 * @return the dictionaryEntryTable
	 */
	public String getDictionaryEntryTable() {
		return this.dictionaryEntryTable;
	}

	/**
	 * @return the action
	 */
	public IndexAction getAction() {
		return this.action;
	}

	/**
	 * @return the payloadFrom
	 */
	public String getPayloadFrom() {
		return this.payloadFrom;
	}

	/**
	 * @return the payloadTo
	 */
	public String getPayloadTo() {
		return this.payloadTo;
	}

}
