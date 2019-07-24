package fr.uem.efluid.model.shared;

import java.time.LocalDateTime;

import fr.uem.efluid.utils.SharedOutputInputUtils;

/**
 * @author elecomte
 * @since v0.0.1
 * @version 1
 */
public abstract class ExportAwareTableLink<D extends ExportAwareDictionaryEntry<?>> implements CompositeRefSupport<D> {

	public abstract LocalDateTime getCreatedTime();

	public abstract LocalDateTime getUpdatedTime();

	/**
	 * @return the columnFrom for indexed position
	 */
	public String getColumnFrom(int index) {

		switch (index) {
		case 0:
			return getColumnFrom();

		case 1:
			return getExt1ColumnFrom();

		case 2:
			return getExt2ColumnFrom();

		case 3:
			return getExt3ColumnFrom();

		case 4:
		default:
			return getExt4ColumnFrom();
		}
	}

	/**
	 * @return the columnTo for indexed position
	 */
	public String getColumnTo(int index) {

		switch (index) {
		case 0:
			return getColumnTo();

		case 1:
			return getExt1ColumnTo();

		case 2:
			return getExt2ColumnTo();

		case 3:
			return getExt3ColumnTo();

		case 4:
		default:
			return getExt4ColumnTo();
		}
	}

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
				.with("nam", getName())
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
