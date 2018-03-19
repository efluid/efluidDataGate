package fr.uem.efluid.stubs;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

/**
 * @author elecomte
 * @since v0.0.1
 * @version 1
 */
@Entity
@Table(name = TestUtils.SOURCE_CHILD_TABLE_NAME)
public class SimulatedSourceChild {

	@Id
	private Long key;

	private String value;

	@ManyToOne(optional = false, fetch = FetchType.EAGER)
	@JoinColumn(name = "PARENT")
	private SimulatedSource parent;

	/**
	 * 
	 */
	public SimulatedSourceChild() {
		super();
	}

	/**
	 * @return the key
	 */
	public Long getKey() {
		return this.key;
	}

	/**
	 * @param key
	 *            the key to set
	 */
	public void setKey(Long key) {
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
	 * @return the parent
	 */
	public SimulatedSource getParent() {
		return this.parent;
	}

	/**
	 * @param parent
	 *            the parent to set
	 */
	public void setParent(SimulatedSource parent) {
		this.parent = parent;
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
		SimulatedSourceChild other = (SimulatedSourceChild) obj;
		if (this.key == null) {
			if (other.key != null)
				return false;
		} else if (!this.key.equals(other.key))
			return false;
		return true;
	}
}
