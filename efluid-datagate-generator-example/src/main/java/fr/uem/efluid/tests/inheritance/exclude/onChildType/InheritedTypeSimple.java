package fr.uem.efluid.tests.inheritance.exclude.onChildType;

import fr.uem.efluid.ParameterTable;
import fr.uem.efluid.ParameterValue;

@ParameterTable(tableName = "T_INHERITED")
public class InheritedTypeSimple extends RootType {

    @ParameterValue
    private String property;

    @ParameterValue
    private String second;

}
