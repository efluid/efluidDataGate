package fr.uem.efluid.tests.inheritance.exclude.onKey;


import fr.uem.efluid.ParameterKey;
import fr.uem.efluid.ParameterTable;
import fr.uem.efluid.ParameterValue;

@ParameterTable(tableName = "T_ROOT", domainName = "domain")
public class RootType {

    @ParameterKey
    private String keyOne;

    @ParameterKey(notInherited = true)
    private int keyTwo;

    @ParameterValue
    private String value;

    @ParameterValue
    private int other;
}
