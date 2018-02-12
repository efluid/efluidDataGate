package fr.uem.efluid.services.types;

import java.util.UUID;

import fr.uem.efluid.model.entities.DictionaryEntry;

/**
 * @author elecomte
 * @since v0.0.1
 * @version 1
 */
public class DiffDisplay<T> implements Comparable<DiffDisplay<?>> {

	private UUID dictionaryEntryUuid;

	private UUID domainUuid;

	private String domainName;

	private String dictionaryEntryName;

	private T diff;

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
	 * Can be partially completed (uuid only) : get other dict entry properties from its
	 * entity
	 * 
	 * @param entity
	 */
	public void completeFromEntity(DictionaryEntry entity) {

		this.setDictionaryEntryName(entity.getParameterName());

		if (entity.getDomain() != null) {
			this.setDomainName(entity.getDomain().getName());
			this.setDomainUuid(entity.getDomain().getUuid());
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
