package fr.uem.efluid.sample.updates;

import fr.uem.efluid.ParameterIgnored;

/**
 * @author elecomte
 * @since v0.0.8
 * @version 1
 */
@ParameterIgnored
public class LastSubType extends MyType {

	private String subProperty;

	/**
	 * @return the subProperty
	 */
	public String getSubProperty() {
		return this.subProperty;
	}

	/**
	 * @param subProperty the subProperty to set
	 */
	public void setSubProperty(String subProperty) {
		this.subProperty = subProperty;
	}
	
	
}
