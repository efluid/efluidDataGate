package fr.uem.efluid.sample.updates;

import fr.uem.efluid.ParameterCompositeValue;
import fr.uem.efluid.ParameterIgnored;
import fr.uem.efluid.ParameterKey;
import fr.uem.efluid.ParameterLink;
import fr.uem.efluid.ParameterTable;

/**
 * @author elecomte
 * @since v0.0.8
 * @version 1
 */
@ParameterTable
public class TabWithRefOnCompositeKey {

	@ParameterKey
	private String localKey;

	@ParameterLink(toColumn = { "BIZ_KEY_ONE", "BIZ_KEY_TWO", "BIZ_KEY_THREE" })
	@ParameterCompositeValue({ "REF_BIZ_KEY_ONE", "REF_BIZ_KEY_TWO", "REF_BIZ_KEY_THREE" })
	private TabWithCompositeKey referenced;

	// Automatically mapped as value
	private String value;

	// Automatically mapped as value
	private String other;

	@ParameterIgnored
	private String something;

	/**
	 * @return the localKey
	 */
	public String getLocalKey() {
		return this.localKey;
	}

	/**
	 * @param localKey
	 *            the localKey to set
	 */
	public void setLocalKey(String localKey) {
		this.localKey = localKey;
	}

	/**
	 * @return the referenced
	 */
	public TabWithCompositeKey getReferenced() {
		return this.referenced;
	}

	/**
	 * @param referenced
	 *            the referenced to set
	 */
	public void setReferenced(TabWithCompositeKey referenced) {
		this.referenced = referenced;
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
