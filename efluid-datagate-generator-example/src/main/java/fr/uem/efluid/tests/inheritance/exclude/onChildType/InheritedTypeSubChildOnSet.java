package fr.uem.efluid.tests.inheritance.exclude.onChildType;

import fr.uem.efluid.ParameterInheritance;
import fr.uem.efluid.ParameterTable;
import fr.uem.efluid.ParameterTableSet;
import fr.uem.efluid.ParameterValue;

@ParameterTableSet(
        tables = {
                @ParameterTable(tableName = "T_INHERITED_SUB_1"),
                @ParameterTable(tableName = "T_INHERITED_SUB_2")
        },
        excludeInherited = @ParameterInheritance(of = RootType.class, fields = "VALUE")
)
public class InheritedTypeSubChildOnSet extends InheritedTypeSimple {

    @ParameterValue(name = "SUB_PROPERTY")
    private String subProperty;

}
