package fr.uem.efluid.services.types;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import fr.uem.efluid.model.entities.DictionaryEntry;

/**
 * @author elecomte
 * @since v0.0.1
 * @version 1
 */
public class DiffDisplay<T extends PreparedIndexEntry> implements Comparable<DiffDisplay<?>> {

	private UUID dictionaryEntryUuid;

	private UUID domainUuid;

	private String domainName;

	private String dictionaryEntryName;

	private String dictionaryEntryDisplayKeyName;

	private String dictionaryEntryTableName;

	private List<T> diff;

	private List<DiffRemark<?>> remarks;

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
	public List<T> getDiff() {
		return this.diff;
	}

	/**
	 * @return real size of diff, when some items are combined
	 */
	public long getRealDiffSize() {
		return this.diff.stream().mapToInt(Rendered::getRealSize).sum();
	}

	/**
	 * @param diff
	 *            the diff to set
	 */
	public void setDiff(List<T> diff) {
		this.diff = diff;
	}

	/**
	 * @return the remarks
	 */
	public List<DiffRemark<?>> getRemarks() {
		return this.remarks;
	}

	/**
	 * @param remark
	 *            the remark to add on managed remarks. Inits the holder list on demand
	 */
	public void addRemark(DiffRemark<?> remark) {
		if (this.remarks == null) {
			this.remarks = new ArrayList<>();
		}
		this.remarks.add(remark);
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
	 * @return the dictionaryEntryDisplayKeyName
	 */
	public String getDictionaryEntryDisplayKeyName() {
		return this.dictionaryEntryDisplayKeyName;
	}

	/**
	 * @param dictionaryEntryDisplayKeyName
	 *            the dictionaryEntryDisplayKeyName to set
	 */
	public void setDictionaryEntryDisplayKeyName(String dictionaryEntryDisplayKeyName) {
		this.dictionaryEntryDisplayKeyName = dictionaryEntryDisplayKeyName;
	}

	/**
	 * @return
	 */
	public boolean isHasRemarks() {
		return this.remarks != null && this.remarks.size() > 0;
	}

	/**
	 * @return
	 */
	public boolean isHasContent() {
		return this.diff != null && this.diff.size() > 0;
	}

	/**
	 * Can be partially completed (uuid only) : get other dict entry properties from its
	 * entity
	 * 
	 * @param entity
	 */
	public void completeFromEntity(DictionaryEntry entity) {

		setDictionaryEntryName(entity.getParameterName());
		setDictionaryEntryDisplayKeyName(entity.keyNames().collect(Collectors.joining("/")));
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
