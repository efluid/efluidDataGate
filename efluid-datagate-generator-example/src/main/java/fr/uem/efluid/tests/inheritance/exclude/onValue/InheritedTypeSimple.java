package fr.uem.efluid.tests.inheritance.exclude.onValue;

import fr.uem.efluid.ParameterTable;
import fr.uem.efluid.ParameterValue;

@ParameterTable(tableName = "T_INHERITED")
public class InheritedTypeSimple extends RootType {

    @ParameterValue(notInherited = true)
    private String property;

}
