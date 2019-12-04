package fr.uem.efluid.cucumber.stubs.entities;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import fr.uem.efluid.cucumber.stubs.ManagedDatabaseAccess;

import java.util.Objects;

/**
 * @author elecomte
 * @since v0.0.8
 * @version 1
 */
@Entity
@Table(name = ManagedDatabaseAccess.TABLE_TWO)
public class SimulatedTableTwo {

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

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		SimulatedTableTwo that = (SimulatedTableTwo) o;
		return Objects.equals(key, that.key) &&
				Objects.equals(value, that.value) &&
				Objects.equals(other, that.other);
	}

	@Override
	public int hashCode() {
		return Objects.hash(key, value, other);
	}
}
