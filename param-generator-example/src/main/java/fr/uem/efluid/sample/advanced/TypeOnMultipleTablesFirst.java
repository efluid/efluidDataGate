package fr.uem.efluid.sample.advanced;

import fr.uem.efluid.ParameterKey;
import fr.uem.efluid.ParameterTable;
import fr.uem.efluid.ParameterTableSet;
import fr.uem.efluid.ParameterValue;

/**
 * @author elecomte
 * @since v0.0.8
 * @version 1
 */
@ParameterTableSet({
		@ParameterTable(tableName = "T_TABLE_MUTONE_ONE"),
		@ParameterTable(tableName = "T_TABLE_MUTONE_TWO"),
		@ParameterTable(tableName = "T_TABLE_MUTONE_THREE")
})
public class TypeOnMultipleTablesFirst {

	@ParameterKey // Si pas indiqué => Commun à toutes les tables du set
	private Long key;

	@ParameterValue // Si pas indiqué => Commun à toutes les tables du set
	private String valueOnAll;

	@ParameterValue(forTable = "T_TABLE_MUTONE_ONE")
	private String valueA;

	@ParameterValue(forTable = "T_TABLE_MUTONE_ONE")
	private String valueB;

	@ParameterValue(forTable = "T_TABLE_MUTONE_TWO")
	private String valueC;

	@ParameterValue(forTable = "T_TABLE_MUTONE_TWO")
	private String valueD;

	@ParameterValue(forTable = "T_TABLE_MUTONE_THREE")
	private String valueE;

	@ParameterValue(forTable = "T_TABLE_MUTONE_THREE")
	private String valueF;

	/**
	 * @return the key
	 */
	public Long getKey() {
		return this.key;
	}

	/**
	 * @param key
	 *            the key to set
	 */
	public void setKey(Long key) {
		this.key = key;
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
