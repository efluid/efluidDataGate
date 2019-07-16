package fr.uem.efluid.sample.updates;

import fr.uem.efluid.ParameterIgnored;
import fr.uem.efluid.ParameterKey;
import fr.uem.efluid.ParameterTable;

/**
 * @author elecomte
 * @since v0.0.8
 * @version 1
 */
@ParameterTable(tableName = "T_WITH_COMPOSITE")
public class TabWithCompositeKey {

	@ParameterIgnored
	private int id;

	@ParameterKey("BIZ_KEY_ONE")
	private Long bizKeyOne;

	@ParameterKey("BIZ_KEY_TWO")
	private Long bizKeyTwo;

	@ParameterKey("BIZ_KEY_THREE")
	private Long bizKeyThree;

	private String valueSomething;

	private String valueOther;

	/**
	 * @return the id
	 */
	public int getId() {
		return this.id;
	}

	/**
	 * @param id
	 *            the id to set
	 */
	public void setId(int id) {
		this.id = id;
	}

	/**
	 * @return the bizKeyOne
	 */
	public Long getBizKeyOne() {
		return this.bizKeyOne;
	}

	/**
	 * @param bizKeyOne
	 *            the bizKeyOne to set
	 */
	public void setBizKeyOne(Long bizKeyOne) {
		this.bizKeyOne = bizKeyOne;
	}

	/**
	 * @return the bizKeyTwo
	 */
	public Long getBizKeyTwo() {
		return this.bizKeyTwo;
	}

	/**
	 * @param bizKeyTwo
	 *            the bizKeyTwo to set
	 */
	public void setBizKeyTwo(Long bizKeyTwo) {
		this.bizKeyTwo = bizKeyTwo;
	}

	/**
	 * @return the bizKeyThree
	 */
	public Long getBizKeyThree() {
		return this.bizKeyThree;
	}

	/**
	 * @param bizKeyThree
	 *            the bizKeyThree to set
	 */
	public void setBizKeyThree(Long bizKeyThree) {
		this.bizKeyThree = bizKeyThree;
	}

	/**
	 * @return the valueSomething
	 */
	public String getValueSomething() {
		return this.valueSomething;
	}

	/**
	 * @param valueSomething
	 *            the valueSomething to set
	 */
	public void setValueSomething(String valueSomething) {
		this.valueSomething = valueSomething;
	}

	/**
	 * @return the valueOther
	 */
	public String getValueOther() {
		return this.valueOther;
	}

	/**
	 * @param valueOther
	 *            the valueOther to set
	 */
	public void setValueOther(String valueOther) {
		this.valueOther = valueOther;
	}
}
