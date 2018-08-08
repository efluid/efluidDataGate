package fr.uem.efluid.services.types;

import java.util.List;
import java.util.UUID;

import fr.uem.efluid.model.entities.DictionaryEntry;

/**
 * @author elecomte
 * @since v0.0.1
 * @version 1
 */
public class DiffDisplay<T extends PreparedIndexEntry> implements Comparable<DiffDisplay<?>> {

	private UUID dictionaryEntryUuid;

	private String dictionaryEntryName;

	private String dictionaryEntryKeyName;

	private String dictionaryEntryTableName;

	private List<T> diff;

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
	 * Can be partially completed (uuid only) : get other dict entry properties from its
	 * entity
	 * 
	 * @param entity
	 */
	public void completeFromEntity(DictionaryEntry entity) {

		setDictionaryEntryName(entity.getParameterName());
		setDictionaryEntryKeyName(entity.getKeyName());
		setDictionaryEntryTableName(entity.getTableName());
	}

	/**
	 * @param o
	 * @return
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo(DiffDisplay<?> o) {

		return this.getDictionaryEntryName().compareTo(o.getDictionaryEntryName());
	}

}
