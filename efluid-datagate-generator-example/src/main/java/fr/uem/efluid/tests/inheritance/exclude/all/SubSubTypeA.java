package fr.uem.efluid.tests.inheritance.exclude.all;

import fr.uem.efluid.*;
import fr.uem.efluid.tests.inheritance.exclude.onChildType.RootType;

import static fr.uem.efluid.ParameterInheritance.ALL;

@ParameterTable(tableName = "T_SUB_SUB_A", excludeInherited = @ParameterInheritance(of = ALL.class))
public class SubSubTypeA extends SubTypeA {

    @ParameterKey
    private int innerkey;

    private String suballowed;

    private String subcard;

}
