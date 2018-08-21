package fr.uem.efluid.sample.updates;

import java.util.Collection;

import fr.uem.efluid.ParameterMapping;
import fr.uem.efluid.ParameterValue;

/**
 * @author elecomte
 * @since v0.0.8
 * @version 1
 */
public class MyOtherSubType extends MyType {

	private String customKey;

	private Long otherAttribute;

	@ParameterValue
	@ParameterMapping(mapTableName = "t_oth_links")
	private Collection<AnotherLinkedType> linkedTypes;

	/**
	 * @return the customKey
	 */
	public String getCustomKey() {
		return this.customKey;
	}

	/**
	 * @param customKey
	 *            the customKey to set
	 */
	public void setCustomKey(String customKey) {
		this.customKey = customKey;
	}

	/**
	 * @return the otherAttribute
	 */
	public Long getOtherAttribute() {
		return this.otherAttribute;
	}

	/**
	 * @param otherAttribute
	 *            the otherAttribute to set
	 */
	public void setOtherAttribute(Long otherAttribute) {
		this.otherAttribute = otherAttribute;
	}
}
