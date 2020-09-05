package fr.uem.efluid.tests.inheritance.exclude.onKey;

import fr.uem.efluid.ParameterTable;
import fr.uem.efluid.ParameterTableSet;

@ParameterTableSet(tables = {
        @ParameterTable(tableName = "MY_TYPE", keyField = "keyOne"),
        @ParameterTable(tableName = "MY_TYPE_CHILD", keyField = "keyOne")
})
public class InheritedTypeWithSet extends RootType {

    private String property;

}
