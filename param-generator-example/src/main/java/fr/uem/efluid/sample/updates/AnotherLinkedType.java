package fr.uem.efluid.sample.updates;

import fr.uem.efluid.ParameterTable;
import fr.uem.efluid.ParameterValue;

/**
 * @author elecomte
 * @since v0.0.8
 * @version 1
 */
@ParameterTable(keyField = "identifier", tableName = "TANOTHER", filterClause = "cur.\"ENABLED\"=1", useAllFields = false)
public class AnotherLinkedType {

	private String identifier;

	@ParameterValue("FIELD")
	private String fieldValue;

	@ParameterValue
	private int something;

	@ParameterValue
	private int other;

	private boolean enabled;

	/**
	 * @return the identifier
	 */
	public String getIdentifier() {
		return this.identifier;
	}

	/**
	 * @param identifier
	 *            the identifier to set
	 */
	public void setIdentifier(String identifier) {
		this.identifier = identifier;
	}

	/**
	 * @return the fieldValue
	 */
	public String getFieldValue() {
		return this.fieldValue;
	}

	/**
	 * @param fieldValue
	 *            the fieldValue to set
	 */
	public void setFieldValue(String fieldValue) {
		this.fieldValue = fieldValue;
	}

	/**
	 * @return the something
	 */
	public int getSomething() {
		return this.something;
	}

	/**
	 * @param something
	 *            the something to set
	 */
	public void setSomething(int something) {
		this.something = something;
	}

	/**
	 * @return the other
	 */
	public int getOther() {
		return this.other;
	}

	/**
	 * @param other
	 *            the other to set
	 */
	public void setOther(int other) {
		this.other = other;
	}

	/**
	 * @return the enabled
	 */
	public boolean isEnabled() {
		return this.enabled;
	}

	/**
	 * @param enabled
	 *            the enabled to set
	 */
	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}
}
