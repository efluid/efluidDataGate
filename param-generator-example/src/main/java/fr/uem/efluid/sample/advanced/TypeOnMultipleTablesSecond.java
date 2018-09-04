package fr.uem.efluid.sample.advanced;

import fr.uem.efluid.ColumnType;
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
	@ParameterTable(tableName="T_TABLE_ONE", keyField="keyOne", useAllFields=false, values = {
			@ParameterValue("valueOnAll"),
			@ParameterValue("valueA"),
			@ParameterValue("valueB")
	}),
	@ParameterTable(tableName="T_TABLE_TWO", keyField="keyTwo", keyType = ColumnType.ATOMIC, useAllFields=false, values = {
			@ParameterValue("valueOnAll"),
			@ParameterValue("valueC"),
			@ParameterValue("valueD")
	}),
	@ParameterTable(tableName="T_TABLE_THREE", keyField="keyThree", useAllFields=false, values = {
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
}
