package fr.uem.efluid.system.stubs.entities;

import fr.uem.efluid.system.stubs.ManagedDatabaseAccess;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.Table;
import java.math.BigDecimal;

/**With clob
 * @author elecomte
 * @since v0.0.8
 * @version 1
 */
@Entity
@Table(name = ManagedDatabaseAccess.TABLE_FIVE)
public class SimulatedTableFive {

	@Id
	private String key;

	@Lob
	private byte[] data;

	private BigDecimal simple;

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

	public byte[] getData() {
		return this.data;
	}

	public void setData(byte[] data) {
		this.data = data;
	}

	public BigDecimal getSimple() {
		return this.simple;
	}

	public void setSimple(BigDecimal simple) {
		this.simple = simple;
	}

	/**
	 * @return
	 * @see Object#hashCode()
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
	 * @see Object#equals(Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		SimulatedTableFive _other = (SimulatedTableFive) obj;
		if (this.key == null) {
			if (_other.key != null)
				return false;
		} else if (!this.key.equals(_other.key))
			return false;
		return true;
	}
}
