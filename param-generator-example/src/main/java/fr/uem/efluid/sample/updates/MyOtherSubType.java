package fr.uem.efluid.sample.updates;

import java.util.Collection;

import fr.uem.efluid.ParameterMapping;

/**
 * @author elecomte
 * @since v0.0.8
 * @version 1
 */
public class MyOtherSubType extends MyType {

	private String customKey;

	private Long otherAttribute;

	@ParameterMapping(mapTableName = "T_OTH_LINKS")
	private Collection<AnotherLinkedType> linkedTypes;

	@ParameterMapping(mapTableName = "T_OTH_LAST_LINKS", fromColumn = "ID", toColumn = "ID", mapColumnFrom = "SUB_ID",
			mapColumnTo = "LIN_ID")
	private Collection<LastLinkedType> lastLinkedTypes;

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
