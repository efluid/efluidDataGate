package fr.uem.efluid.tests.inheritance.exclude.onChildType;

import fr.uem.efluid.ParameterInheritance;
import fr.uem.efluid.ParameterTable;
import fr.uem.efluid.ParameterValue;

@ParameterTable(tableName = "T_INHERITED_SUB_MIXED",
        excludeInherited = {
                @ParameterInheritance(of = InheritedTypeSimple.class, fields = {"SECOND"}),
                @ParameterInheritance(of = RootType.class, fields = {"OTHER"})
        }
)
public class InheritedTypeSubChildMixed extends InheritedTypeSimple {

    @ParameterValue(name = "SUB_PROPERTY")
    private String subProperty;

}
