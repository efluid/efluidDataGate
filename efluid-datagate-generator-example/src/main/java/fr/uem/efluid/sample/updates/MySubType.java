package fr.uem.efluid.sample.updates;

import fr.uem.efluid.ParameterLink;

/**
 * @author elecomte
 * @since v0.0.8
 * @version 1
 */
public class MySubType extends MyType {

	@ParameterLink
	private SubElement subElement;

	/**
	 * @return the subElement
	 */
	public SubElement getSubElement() {
		return this.subElement;
	}

	/**
	 * @param subElement the subElement to set
	 */
	public void setSubElement(SubElement subElement) {
		this.subElement = subElement;
	}
	
	
}
