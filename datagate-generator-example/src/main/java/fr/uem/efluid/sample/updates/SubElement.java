package fr.uem.efluid.sample.updates;

import java.time.LocalDateTime;

import fr.uem.efluid.ParameterIgnored;
import fr.uem.efluid.ParameterKey;
import fr.uem.efluid.ParameterTable;

/**
 * @author elecomte
 * @since v0.0.8
 * @version 1
 */
@ParameterTable
public class SubElement {

	@ParameterKey
	private String key;

	private Integer value;

	private LocalDateTime time;

	@ParameterIgnored
	private transient String ignoredAttribute;

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
	public Integer getValue() {
		return this.value;
	}

	/**
	 * @param value
	 *            the value to set
	 */
	public void setValue(Integer value) {
		this.value = value;
	}

	/**
	 * @return the time
	 */
	public LocalDateTime getTime() {
		return this.time;
	}

	/**
	 * @param time
	 *            the time to set
	 */
	public void setTime(LocalDateTime time) {
		this.time = time;
	}

	/**
	 * @return the ignoredAttribute
	 */
	public String getIgnoredAttribute() {
		return this.ignoredAttribute;
	}

	/**
	 * @param ignoredAttribute
	 *            the ignoredAttribute to set
	 */
	public void setIgnoredAttribute(String ignoredAttribute) {
		this.ignoredAttribute = ignoredAttribute;
	}
}
