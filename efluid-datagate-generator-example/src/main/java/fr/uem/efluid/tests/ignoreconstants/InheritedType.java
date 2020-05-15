package fr.uem.efluid.tests.ignoreconstants;

import fr.uem.efluid.ParameterTable;

@ParameterTable(tableName = "T_INHERITED", useAllFields = true)
public class InheritedType extends RootType {

    private String property;

    public static final String OTHER_CONSTANT = "OTHER";
}
