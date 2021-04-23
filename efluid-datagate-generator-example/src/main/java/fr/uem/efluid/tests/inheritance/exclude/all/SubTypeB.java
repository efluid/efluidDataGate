package fr.uem.efluid.tests.inheritance.exclude.all;

import fr.uem.efluid.*;
import fr.uem.efluid.tests.inheritance.exclude.onChildType.RootType;

import static fr.uem.efluid.ParameterInheritance.ALL;

@ParameterTable(tableName = "T_SUB_B", excludeInherited = @ParameterInheritance(of = ALL.class),
        keys = @ParameterKey(value = "KEYB", type = ColumnType.ATOMIC))
public class SubTypeB extends RootType {

    private int keyb;

    @ParameterLink(withValueName = "LINKEDTYPE_KEY_B")
    private SomeLinkedType linkedType;

    @ParameterValue
    private String property;

    @ParameterValue
    private String second;

}
