package fr.uem.efluid.tests.inheritance.exclude.onChildType;

import fr.uem.efluid.ParameterInheritance;
import fr.uem.efluid.ParameterTable;
import fr.uem.efluid.ParameterValue;

@ParameterTable(tableName = "T_INHERITED_SUB_SPEC",
        excludeInherited = @ParameterInheritance(of = RootType.class, fields = {"VALUE", "OTHER"})
)
public class InheritedTypeSubChildSpecifiedFields extends InheritedTypeSimple {

    @ParameterValue(name = "SUB_PROPERTY")
    private String subProperty;

}
