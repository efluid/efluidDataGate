package fr.uem.efluid.stubs;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import fr.uem.efluid.TestUtils;

/**
 * @author elecomte
 * @since v0.0.1
 * @version 1
 */
@Entity
@Table(name = TestUtils.SOURCE_TABLE_NAME)
public class SimulatedSource {

	@Id
	private Long key;

	private String value;

	private String preset;

	private String something;

	/**
	 * 
	 */
	public SimulatedSource() {
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
	 * @return the preset
	 */
	public String getPreset() {
		return this.preset;
	}

	/**
	 * @param preset
	 *            the preset to set
	 */
	public void setPreset(String preset) {
		this.preset = preset;
	}

	/**
	 * @return the something
	 */
	public String getSomething() {
		return this.something;
	}

	/**
	 * @param something
	 *            the something to set
	 */
	public void setSomething(String something) {
		this.something = something;
	}
}
