package fr.uem.efluid.tests.inheritance.exclude.onValue;

import fr.uem.efluid.ParameterTable;
import fr.uem.efluid.ParameterValue;

@ParameterTable(tableName = "T_INHERITED_SUB")
public class InheritedTypeSubChild extends InheritedTypeSimple {

    @ParameterValue(name = "SUB_PROPERTY")
    private String subProperty;

}
