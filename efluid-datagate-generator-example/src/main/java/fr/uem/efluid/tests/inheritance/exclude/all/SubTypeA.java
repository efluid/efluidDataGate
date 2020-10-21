package fr.uem.efluid.tests.inheritance.exclude.all;

import fr.uem.efluid.ParameterLink;
import fr.uem.efluid.ParameterTable;
import fr.uem.efluid.ParameterValue;
import fr.uem.efluid.tests.inheritance.exclude.onChildType.RootType;

@ParameterTable(tableName = "T_SUB_A")
public class SubTypeA extends RootType {

    @ParameterLink(withValueName = "LINKEDTYPE_KEY_A")
    private SomeLinkedType linkedType;

    @ParameterValue
    private String allowed;

    @ParameterValue
    private String card;

}
