package fr.uem.efluid.sample.updates;

import fr.uem.efluid.ParameterTable;
import fr.uem.efluid.ParameterValue;

/**
 * @author elecomte
 * @since v0.0.8
 * @version 1
 */
@ParameterTable(tableName = "TANOTHER_LAST", keyField = "identifier")
public class LastLinkedType extends AnotherLinkedType {

	private long id;

	@ParameterValue
	private int anotherOther;

	/**
	 * @return the id
	 */
	public long getId() {
		return this.id;
	}

	/**
	 * @param id
	 *            the id to set
	 */
	public void setId(long id) {
		this.id = id;
	}

	/**
	 * @return the anotherOther
	 */
	public int getAnotherOther() {
		return this.anotherOther;
	}

	/**
	 * @param anotherOther
	 *            the anotherOther to set
	 */
	public void setAnotherOther(int anotherOther) {
		this.anotherOther = anotherOther;
	}

}
