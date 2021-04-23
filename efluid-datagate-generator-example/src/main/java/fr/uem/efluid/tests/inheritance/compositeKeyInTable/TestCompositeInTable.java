package fr.uem.efluid.tests.inheritance.compositeKeyInTable;


import fr.uem.efluid.*;

@ParameterTableSet(value = {
        @ParameterTable(name = "table 1", tableName = "MY_TABLE_1"),
        @ParameterTable(name = "table sub", tableName = "MY_JOIN_TABLE_WITH_OTHER_PROPS", keys = {
                @ParameterKey(value = "SOURCE", type = ColumnType.STRING),
                @ParameterKey(value = "DEST", type = ColumnType.ATOMIC)
        }, values = {
                @ParameterValue("EXT_ONE"),
                @ParameterValue("EXT_TWO")
        }, useAllFields = false)
}, domainName = "Advanced")
public class TestCompositeInTable {

    @ParameterKey
    private String id;

    private String value;

}
