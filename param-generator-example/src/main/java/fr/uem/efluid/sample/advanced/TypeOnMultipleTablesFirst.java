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
	@ParameterTable(tableName="T_TABLE_ONE"),
	@ParameterTable(tableName="T_TABLE_TWO"),
	@ParameterTable(tableName="T_TABLE_THREE")
})
public class TypeOnMultipleTablesFirst {

	@ParameterKey // Si pas indiqué => Commun à toutes les tables du set
	private Long key;
	
	@ParameterValue // Si pas indiqué => Commun à toutes les tables du set
	private String valueOnAll;
	
	@ParameterValue(forTable="T_TABLE_ONE")
	private String valueA;

	@ParameterValue(forTable="T_TABLE_ONE")
	private String valueB;

	@ParameterValue(forTable="T_TABLE_TWO")
	private String valueC;

	@ParameterValue(forTable="T_TABLE_TWO")
	private String valueD;

	@ParameterValue(forTable="T_TABLE_THREE")
	private String valueE;

	@ParameterValue(forTable="T_TABLE_THREE")
	private String valueF;
	
	
}
