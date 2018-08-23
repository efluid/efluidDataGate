package fr.uem.efluid.model.shared;

import java.time.LocalDateTime;

import fr.uem.efluid.model.Shared;
import fr.uem.efluid.utils.SharedOutputInputUtils;

/**
 * @author elecomte
 * @since v0.0.1
 * @version 1
 */
public abstract class ExportAwareTableLink<D extends ExportAwareDictionaryEntry<?>> implements Shared {

	/**
	 * @return
	 */
	public abstract String getColumnFrom();

	/**
	 * @param tableTo
	 *            the tableTo to set
	 */
	public abstract String getTableTo();

	/**
	 * @param columnTo
	 *            the columnTo to set
	 */
	public abstract String getColumnTo();

	/**
	 * @return
	 */
	public abstract String getExt1ColumnTo();

	/**
	 * @return
	 */
	public abstract String getExt2ColumnTo();

	/**
	 * @return
	 */
	public abstract String getExt3ColumnTo();

	/**
	 * @return
	 */
	public abstract String getExt4ColumnTo();

	/**
	 * @return
	 */
	public abstract String getExt1ColumnFrom();

	/**
	 * @return
	 */
	public abstract String getExt2ColumnFrom();

	/**
	 * @return
	 */
	public abstract String getExt3ColumnFrom();

	/**
	 * @return
	 */
	public abstract String getExt4ColumnFrom();

	/**
	 * @param createdTime
	 *            the createdTime to set
	 */
	public abstract LocalDateTime getCreatedTime();

	/**
	 * @param updatedTime
	 *            the updatedTime to set
	 */
	public abstract LocalDateTime getUpdatedTime();

	/**
	 * @return the dictionaryEntry
	 */
	public abstract D getDictionaryEntry();

	/**
	 * @return
	 * @see fr.uem.efluid.model.Shared#serialize()
	 */
	@Override
	public final String serialize() {

		return SharedOutputInputUtils.newJson()
				.with("uid", getUuid())
				.with("cre", getCreatedTime())
				.with("upd", getUpdatedTime())
				.with("cfr", getColumnFrom())
				.with("cto", getColumnTo())
				.with("cf1", getExt1ColumnFrom())
				.with("cf2", getExt2ColumnFrom())
				.with("cf3", getExt3ColumnFrom())
				.with("cf4", getExt4ColumnFrom())
				.with("ct1", getExt1ColumnTo())
				.with("ct2", getExt2ColumnTo())
				.with("ct3", getExt3ColumnTo())
				.with("ct4", getExt4ColumnTo())
				.with("tto", getTableTo())
				.with("dic", getDictionaryEntry().getUuid())
				.toString();
	}

	/**
	 * @return
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((this.getColumnFrom() == null) ? 0 : this.getColumnFrom().hashCode());
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
		if (!(obj instanceof ExportAwareTableLink))
			return false;
		ExportAwareTableLink<?> other = (ExportAwareTableLink<?>) obj;
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
