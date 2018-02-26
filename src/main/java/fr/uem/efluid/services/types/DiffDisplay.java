package fr.uem.efluid.services.types;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import fr.uem.efluid.model.entities.DictionaryEntry;

/**
 * @author elecomte
 * @since v0.0.1
 * @version 1
 */
public class DiffDisplay<T extends List<? extends PreparedIndexEntry>> implements Comparable<DiffDisplay<?>> {

	private UUID dictionaryEntryUuid;

	private UUID domainUuid;

	private String domainName;

	private String dictionaryEntryName;

	private String dictionaryEntryKeyName;

	private String dictionaryEntryTableName;

	private T diff;

	private Map<String, byte[]> diffLobs;

	/**
	 * 
	 */
	public DiffDisplay() {
		super();
	}

	/**
	 * @param dict
	 */
	public DiffDisplay(UUID dictionaryEntryUuid) {
		super();
		this.dictionaryEntryUuid = dictionaryEntryUuid;
	}

	/**
	 * @return the diff
	 */
	public T getDiff() {
		return this.diff;
	}

	/**
	 * @param diff
	 *            the diff to set
	 */
	public void setDiff(T diff) {
		this.diff = diff;
	}

	/**
	 * @return the dictionaryEntryTableName
	 */
	public String getDictionaryEntryTableName() {
		return this.dictionaryEntryTableName;
	}

	/**
	 * @param dictionaryEntryTableName
	 *            the dictionaryEntryTableName to set
	 */
	public void setDictionaryEntryTableName(String dictionaryEntryTableName) {
		this.dictionaryEntryTableName = dictionaryEntryTableName;
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
	 * @return the dictionaryEntryKeyName
	 */
	public String getDictionaryEntryKeyName() {
		return this.dictionaryEntryKeyName;
	}

	/**
	 * @param dictionaryEntryKeyName
	 *            the dictionaryEntryKeyName to set
	 */
	public void setDictionaryEntryKeyName(String dictionaryEntryKeyName) {
		this.dictionaryEntryKeyName = dictionaryEntryKeyName;
	}

	/**
	 * @return the diffLobs
	 */
	public Map<String, byte[]> getDiffLobs() {
		return this.diffLobs;
	}

	/**
	 * @param diffLobs
	 *            the diffLobs to set
	 */
	public void setDiffLobs(Map<String, byte[]> diffLobs) {
		this.diffLobs = diffLobs;
	}

	/**
	 * Can be partially completed (uuid only) : get other dict entry properties from its
	 * entity
	 * 
	 * @param entity
	 */
	public void completeFromEntity(DictionaryEntry entity) {

		setDictionaryEntryName(entity.getParameterName());
		setDictionaryEntryKeyName(entity.getKeyName());
		setDictionaryEntryTableName(entity.getTableName());

		if (entity.getDomain() != null) {
			setDomainName(entity.getDomain().getName());
			setDomainUuid(entity.getDomain().getUuid());
		}
	}

	/**
	 * @param o
	 * @return
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo(DiffDisplay<?> o) {

		int dom = this.getDomainName().compareTo(o.getDomainName());

		if (dom != 0) {
			return dom;
		}

		return this.getDictionaryEntryName().compareTo(o.getDictionaryEntryName());
	}

}
