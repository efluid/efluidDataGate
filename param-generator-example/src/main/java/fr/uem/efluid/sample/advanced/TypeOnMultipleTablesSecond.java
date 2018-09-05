package fr.uem.efluid.sample.advanced;

import fr.uem.efluid.ColumnType;
import fr.uem.efluid.ParameterTable;
import fr.uem.efluid.ParameterTableSet;
import fr.uem.efluid.ParameterValue;

/**
 * @author elecomte
 * @since v0.0.8
 * @version 1
 */
@ParameterTableSet({
		@ParameterTable(tableName = "T_TABLE_MUTSECOND_ONE", keyField = "keyOne", useAllFields = false, values = {
				@ParameterValue("valueOnAll"),
				@ParameterValue("valueA"),
				@ParameterValue("valueB")
		}),
		@ParameterTable(tableName = "T_TABLE_MUTSECOND_TWO", keyField = "keyTwo", keyType = ColumnType.ATOMIC, useAllFields = false, values = {
				@ParameterValue("valueOnAll"),
				@ParameterValue("valueC"),
				@ParameterValue("valueD")
		}),
		@ParameterTable(tableName = "T_TABLE_MUTSECOND_THREE", keyField = "keyThree", useAllFields = false, values = {
				@ParameterValue("valueOnAll"),
				@ParameterValue("valueE"),
				@ParameterValue("valueF")
		})
})
public class TypeOnMultipleTablesSecond {

	private Long keyOne;

	private Long keyTwo;

	private Long keyThree;

	private String valueOnAll;

	private String valueA;

	private String valueB;

	private String valueC;

	private String valueD;

	private String valueE;

	private String valueF;

	/**
	 * @return the keyOne
	 */
	public Long getKeyOne() {
		return this.keyOne;
	}

	/**
	 * @param keyOne
	 *            the keyOne to set
	 */
	public void setKeyOne(Long keyOne) {
		this.keyOne = keyOne;
	}

	/**
	 * @return the keyTwo
	 */
	public Long getKeyTwo() {
		return this.keyTwo;
	}

	/**
	 * @param keyTwo
	 *            the keyTwo to set
	 */
	public void setKeyTwo(Long keyTwo) {
		this.keyTwo = keyTwo;
	}

	/**
	 * @return the keyThree
	 */
	public Long getKeyThree() {
		return this.keyThree;
	}

	/**
	 * @param keyThree
	 *            the keyThree to set
	 */
	public void setKeyThree(Long keyThree) {
		this.keyThree = keyThree;
	}

	/**
	 * @return the valueOnAll
	 */
	public String getValueOnAll() {
		return this.valueOnAll;
	}

	/**
	 * @param valueOnAll
	 *            the valueOnAll to set
	 */
	public void setValueOnAll(String valueOnAll) {
		this.valueOnAll = valueOnAll;
	}

	/**
	 * @return the valueA
	 */
	public String getValueA() {
		return this.valueA;
	}

	/**
	 * @param valueA
	 *            the valueA to set
	 */
	public void setValueA(String valueA) {
		this.valueA = valueA;
	}

	/**
	 * @return the valueB
	 */
	public String getValueB() {
		return this.valueB;
	}

	/**
	 * @param valueB
	 *            the valueB to set
	 */
	public void setValueB(String valueB) {
		this.valueB = valueB;
	}

	/**
	 * @return the valueC
	 */
	public String getValueC() {
		return this.valueC;
	}

	/**
	 * @param valueC
	 *            the valueC to set
	 */
	public void setValueC(String valueC) {
		this.valueC = valueC;
	}

	/**
	 * @return the valueD
	 */
	public String getValueD() {
		return this.valueD;
	}

	/**
	 * @param valueD
	 *            the valueD to set
	 */
	public void setValueD(String valueD) {
		this.valueD = valueD;
	}

	/**
	 * @return the valueE
	 */
	public String getValueE() {
		return this.valueE;
	}

	/**
	 * @param valueE
	 *            the valueE to set
	 */
	public void setValueE(String valueE) {
		this.valueE = valueE;
	}

	/**
	 * @return the valueF
	 */
	public String getValueF() {
		return this.valueF;
	}

	/**
	 * @param valueF
	 *            the valueF to set
	 */
	public void setValueF(String valueF) {
		this.valueF = valueF;
	}
}
