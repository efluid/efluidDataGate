package fr.uem.efluid.tests.inheritance.exclude.all;

import fr.uem.efluid.ParameterLink;
import fr.uem.efluid.ParameterTable;
import fr.uem.efluid.ParameterValue;
import fr.uem.efluid.tests.inheritance.exclude.onChildType.RootType;

@ParameterTable(tableName = "T_SUB_SUB_B")
public class SubSubTypeB extends SubTypeB {

    @ParameterValue
    private String bprop;

    @ParameterValue
    private String bsec;

}
