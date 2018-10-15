package fr.uem.efluid.system.stubs.entities;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import fr.uem.efluid.system.stubs.ManagedDatabaseAccess;

/**
 * @author elecomte
 * @since v0.0.8
 * @version 1
 */
@Entity
@Table(name = ManagedDatabaseAccess.TABLE_THREE)
public class SimulatedTableThree {

	@Id
	private String key;

	private String value;

	private String other;

	/**
	 * @return the key
	 */
	public String getKey() {
		return this.key;
	}

	/**
	 * @param key
	 *            the key to set
	 */
	public void setKey(String key) {
		this.key = key;
	}

	/**
	 * @return the value
	 */
	public String getValue() {
		return this.value;
	}

	/**
	 * @param value
	 *            the value to set
	 */
	public void setValue(String value) {
		this.value = value;
	}

	/**
	 * @return the other
	 */
	public String getOther() {
		return this.other;
	}

	/**
	 * @param other
	 *            the other to set
	 */
	public void setOther(String other) {
		this.other = other;
	}

	/**
	 * @return
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((this.key == null) ? 0 : this.key.hashCode());
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
		SimulatedTableThree _other = (SimulatedTableThree) obj;
		if (this.key == null) {
			if (_other.key != null)
				return false;
		} else if (!this.key.equals(_other.key))
			return false;
		return true;
	}
}
