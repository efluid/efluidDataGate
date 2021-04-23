package fr.uem.efluid.tests.inheritance.exclude.onKey;

import fr.uem.efluid.ColumnType;
import fr.uem.efluid.ParameterKey;
import fr.uem.efluid.ParameterTable;
import fr.uem.efluid.ParameterTableSet;

@ParameterTableSet(tables = {
        @ParameterTable(tableName = "MY_TYPE", keys = @ParameterKey(value = "KEYONE", type = ColumnType.ATOMIC)),
        @ParameterTable(tableName = "MY_TYPE_CHILD", keys = @ParameterKey(value = "KEYONE", type = ColumnType.STRING))
})
public class InheritedTypeWithSet extends RootType {

    private String property;

}
