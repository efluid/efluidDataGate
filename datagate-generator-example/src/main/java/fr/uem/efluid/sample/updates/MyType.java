package fr.uem.efluid.sample.updates;

import fr.uem.efluid.ParameterIgnored;
import fr.uem.efluid.ParameterTable;

/**
 * @author elecomte
 * @since v0.0.8
 * @version 1
 */
@ParameterTable(keyField = "key")
public class MyType {

	@ParameterIgnored
	private Long id;

	private String key;

	private String value;

	private String otherValue;

	/**
	 * @return the id
	 */
	public Long getId() {
		return this.id;
	}

	/**
	 * @param id
	 *            the id to set
	 */
	public void setId(Long id) {
		this.id = id;
	}

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
	 * @return the otherValue
	 */
	public String getOtherValue() {
		return this.otherValue;
	}

	/**
	 * @param otherValue
	 *            the otherValue to set
	 */
	public void setOtherValue(String otherValue) {
		this.otherValue = otherValue;
	}

}
