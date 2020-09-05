package fr.uem.efluid.tests.inheritance.exclude.onValue;

import fr.uem.efluid.ParameterTable;
import fr.uem.efluid.ParameterTableSet;

@ParameterTableSet(tables = {
        @ParameterTable(tableName = "MY_TYPE"),
        @ParameterTable(tableName = "MY_TYPE_CHILD")
})
public class InheritedTypeWithSet extends RootType {

    private String property;

}
