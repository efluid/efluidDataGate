package fr.uem.efluid.model.shared;

import java.time.LocalDateTime;

import fr.uem.efluid.model.Shared;
import fr.uem.efluid.utils.SharedOutputInputUtils;

/**
 * @author elecomte
 * @since v0.0.1
 * @version 1
 */
public abstract class ExportAwareFunctionalDomain implements Shared {

	/**
	 * @return
	 */
	public abstract String getName();

	/**
	 * @param createdTime
	 *            the createdTime to set
	 */
	public abstract LocalDateTime getCreatedTime();

	/**
	 * @return
	 * @see fr.uem.efluid.model.Shared#serialize()
	 */
	@Override
	public final String serialize() {

		return SharedOutputInputUtils.newJson()
				.with("uid", getUuid())
				.with("cre", getCreatedTime())
				.with("nam", getName())
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
		result = prime * result + ((getUuid() == null) ? 0 : getUuid().hashCode());
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
		if (getClass() != obj.getClass())
			return false;
		ExportAwareFunctionalDomain other = (ExportAwareFunctionalDomain) obj;
		if (this.getUuid() == null) {
			if (other.getUuid() != null)
				return false;
		} else if (!getUuid().equals(other.getUuid()))
			return false;
		return true;
	}

}
